package me.krypek.igb.cl1;

import static me.krypek.igb.cl1.InstType.Add;
import static me.krypek.igb.cl1.InstType.Cell;
import static me.krypek.igb.cl1.InstType.Copy;
import static me.krypek.igb.cl1.InstType.Device;
import static me.krypek.igb.cl1.InstType.If;
import static me.krypek.igb.cl1.InstType.Init;
import static me.krypek.igb.cl1.InstType.Math;
import static me.krypek.igb.cl1.InstType.Pixel;
import static me.krypek.igb.cl1.InstType.Pointer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.krypek.freeargparser.ArgType;
import me.krypek.freeargparser.ParsedData;
import me.krypek.freeargparser.ParserBuilder;
import me.krypek.utils.Pair;
import me.krypek.utils.Utils;

public class IGB_Compiler_L1 {

	public static void main(String[] args) {

		//@f:off
		ParsedData data = new ParserBuilder()
				.add("cp", "codepath", 		true,  false, ArgType.StringArray, 	"Array of code paths.")
				.add("op", "outputpath", 	false, false, ArgType.String, 		"Output directory path.")
				.add("ro", "readableOutput",false, false, ArgType.None, 		"If selected, will save readable binaries insted.")
				.add("ms", "memorySize", 	false, false, ArgType.Int, 			"If not -1, program outputs parsed code directly into PES.")
				.add("q",  "quiet",  		false, false, ArgType.None, 		"If selected, won't print out output.")
				.parse(args);
		//@f:on

		final String[] filePaths = data.getStringArray("cp");
		final String fileOutput = data.getStringOrDef("op", "");
		final boolean readableOutput = data.has("ro");
		final int memorySize = data.getIntOrDef("ms", -1);
		final boolean quiet = data.has("q");

		String[][] inputs = new String[filePaths.length][];
		for (int i = 0; i < inputs.length; i++)
			inputs[i] = Utils.readFromFileToArray(filePaths[i]);

		int[][][] compiled = new IGB_Compiler_L1().compile(inputs, filePaths, memorySize);

		File outputFile = new File(fileOutput);
		if(!fileOutput.equals("")) {

			if(outputFile.isDirectory())
				outputFile.mkdirs();
			else
				outputFile.getParentFile().mkdirs();
		}
		if(memorySize == -1) {

			if(readableOutput)
				for (int i = 0; i < compiled.length; i++) {
					try {
						String out = intArrToString(compiled[i]);

						String name = new File(filePaths[i]).getName();
						String path = outputFile.getAbsolutePath() + "/" + Utils.getFileNameWithoutExtension(name) + ".igb_bin_readble";

						new File(path).getParentFile().mkdirs();

						PrintWriter pw = new PrintWriter(path);
						pw.println(out);
						pw.close();

						if(!quiet) {
							System.out.println(path + "\n" + out);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			else {
				for (int i = 0; i < compiled.length; i++) {
					String name = new File(filePaths[i]).getName();
					String path = outputFile.getAbsolutePath() + "/" + Utils.getFileNameWithoutExtension(name) + ".igb_bin";

					new File(path).getParentFile().mkdirs();
					Utils.serialize(compiled[i], path);
				}
			}
		} else {
			String name = new File(filePaths[0]).getName();
			String path = outputFile.getAbsolutePath() + "/" + (name.endsWith(".igb_l1") ? Utils.getFileNameWithoutExtension(name) + ".igb_pes" : name);

			new File(path).getParentFile().mkdirs();
			Utils.serialize(compiled[0], path);
		}
	}

	private static String intArrToString(int[][] arr) {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < arr.length; x++) {
			int len = arr[x].length;
			for (int h = 0; h < len; h++) {
				int val = arr[x][h];
				if(val != IGNORE_INT) {
					sb.append(val);
					if(h != len - 1)
						sb.append(" ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private int fileIndex;

	private final static int MULTIPLIER = 1000;
	public final static int IGNORE_INT = Integer.MIN_VALUE + 47328;

	private String[][] syntax;

	public IGB_Compiler_L1() { syntax = getSyntax(); }

	public int[][][] compile(String[][] inputs, String[] paths, int memorySize) {
		IGB_L1[] igb_l1_Arr = new IGB_L1[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			fileIndex = i;
			String[] in = inputs[i];
			List<Instruction> list = new ArrayList<>();
			if(!in[0].toLowerCase().startsWith("startline"))
				throw new IGB_Compiler_L1_Exception("Code file has to start with \"Startline *startline*\".");
			Integer startline = Utils.parseInt(in[0].substring(in[0].indexOf(' ') + 1));
			if(startline == null)
				throw new IGB_Compiler_L1_Exception("After \"Startline \" you need to put an valid integer.");
			for (int x = 1; x < in.length; x++) {
				Instruction inst = stringToInstruction(inputs[i][x]);
				if(inst != null)
					list.add(inst);
			}
			igb_l1_Arr[i] = new IGB_L1(startline, list.toArray(Instruction[]::new));
		}

		return compile(igb_l1_Arr, paths, memorySize);
	}

	public Instruction stringToInstruction(String str) {
		if(str.isBlank())
			return null;

		if(str.startsWith(":")) {
			Instruction inst = new Instruction(Pointer, 0);
			inst.argS[0] = str.substring(1);
			return inst;
		}
		String[] sp = str.split(" ");
		InstType type = switch (sp[0].toLowerCase()) {
		case "if" -> If;
		case "init" -> Init;
		case "copy" -> Copy;
		case "add" -> Add;
		case "cell" -> Cell;
		case "pixel" -> Pixel;
		case "device" -> Device;
		case "math" -> Math;
		default -> throw new IGB_Compiler_L1_Exception(fileIndex, "");
		};

		Instruction inst = new Instruction(type, sp.length - 1);
		for (int i = 1; i < sp.length; i++) {
			String arg = sp[i];
			if(arg.equals("n"))
				inst.argB[i - 1] = false;
			else if(arg.equals("c"))
				inst.argB[i - 1] = true;
			else {
				Double val = Utils.parseDouble(sp[i]);
				if(val == null)
					inst.argS[i - 1] = sp[i];
				else
					inst.argD[i - 1] = val;
			}
		}
		return inst;
	}

	public int[][][] compile(IGB_L1[] igb_l1_Arr, String[] paths, int memorySize) {
		IGB_Compiler_L1_Exception.paths = paths;
		Pair<Integer[], HashMap<String, Integer>> pair = getAllPointers(igb_l1_Arr);
		HashMap<String, Integer> pointers = pair.getSecond();
		Integer[] pointerCounts = pair.getFirst();
		InstType[] typeSyntax = new InstType[syntax.length];
		for (int i = 0; i < syntax.length; i++)
			typeSyntax[i] = InstType.valueOf(syntax[i][0].substring(0, syntax[i][0].indexOf('|')));
		final int[][][] ret;
		final int[][] pes;

		if(memorySize == -1) {
			ret = new int[igb_l1_Arr.length][][];
			pes = null;
		} else {
			ret = null;
			pes = new int[memorySize][8];
		}

		for (int i = 0; i < igb_l1_Arr.length; i++) {
			IGB_L1 igb = igb_l1_Arr[i];
			int startline = igb.startline;
			if(ret != null) {
				ret[i] = new int[igb.code.length + 1 - pointerCounts[i]][];
				ret[i][0] = new int[] { startline };
			}
			int pointerCount = 0;
			instFor: for (int x = 0; x < igb.code.length; x++) {
				Instruction ins = igb.code[x];

				if(ins.type == Pointer) {
					pointerCount++;
					continue;
				}

				syntaxFor: for (int h = 0; h < syntax.length; h++) {
					if(ins.type != typeSyntax[h])
						continue;
					boolean prevCell = false;

					int[] potential = new int[8];
					if(ret != null)
						Arrays.fill(potential, IGNORE_INT);
					if(ins.toString().equals("Pixel Cache Raw 0.0")) {
						System.out.println();
					}
					for (int j = 1; j < syntax[h].length; j++) {
						int _j = j - 1;

						String syntaxS = syntax[h][j];

						if(syntaxS.equals("@")) {
							if(ins.argB[_j] == null)
								continue syntaxFor;
							if(ins.argB[_j]) {
								potential[j] = 1;
								prevCell = true;
							} else {
								potential[j] = 0;
								prevCell = false;
							}

						} else if(syntaxS.equals("d")) {
							if(ins.argD[_j] == null)
								continue syntaxFor;
							potential[j] = (int) (prevCell ? ins.argD[_j] : ins.argD[_j] * MULTIPLIER);
							prevCell = false;

						} else if(syntaxS.equals("i")) {
							if(ins.argD[_j] == null)
								continue syntaxFor;

							potential[j] = (int) (double) ins.argD[_j];
							prevCell = false;

						} else if(syntaxS.equals("P")) {
							if(ins.argS[_j] != null) {
								if(ins.argS[_j].charAt(0) != ':')
									throw new IGB_Compiler_L1_Exception(i, "Line: " + (x + 2) + "  Pointer name has to start with \':\'.");

								String pointerName = ins.argS[_j].substring(1);
								int pointerCell = pointers.getOrDefault(pointerName, -1);
								if(pointerCell == -1)
									throw new IGB_Compiler_L1_Exception(i, "Line: " + (x + 2) + "  Pointer: \"" + pointerName + "\" doesn't exist.");
								potential[j] = pointerCell;
							} else if(ins.argD[_j] != null)
								potential[j] = (int) (double) ins.argD[_j];

						} else if(syntaxS.substring(0, syntaxS.indexOf('|')).equals(ins.argS[_j])) {
							potential[j] = Utils.parseInt(syntaxS.substring(syntaxS.indexOf('|') + 1));
							prevCell = false;
						} else
							continue syntaxFor;

						if(j == syntax[h].length - 1) {
							potential[0] = ins.type.ordinal();

							if(ret == null) {
								pes[startline + x - pointerCount] = potential;
							} else
								ret[i][x + 1 - pointerCount] = potential;
							continue instFor;
						}
					}
					throw new IGB_Compiler_L1_Exception(i, "Syntax error at line " + (x + 2) + ", \"" + ins + "\".");
				}
			}
		}
		if(ret == null) {
			return new int[][][] { pes };
		} else
			return ret;
	}

	private String[][] getSyntax() {
		InputStream is = getClass().getClassLoader().getResourceAsStream("commandSet.txt");
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			List<String> list = new ArrayList<>();
			r.lines().forEach((str) -> {
				if(!str.isBlank())
					list.add(str);
			});
			String[][] syntax = Utils.splitListToArray(list, " ");
			r.close();
			return syntax;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Pair<Integer[], HashMap<String, Integer>> getAllPointers(IGB_L1[] igb_l1) {
		HashMap<String, Integer> pointers = new HashMap<>();
		Integer[] pointerCounts = new Integer[igb_l1.length];
		for (int i = 0; i < igb_l1.length; i++) {
			Pair<Integer, HashMap<String, Integer>> pair = getPointers(igb_l1[i].code, igb_l1[i].startline, i);
			pointerCounts[i] = pair.getFirst();
			pointers.putAll(pair.getSecond());
		}
		return new Pair<>(pointerCounts, pointers);
	}

	private Pair<Integer, HashMap<String, Integer>> getPointers(Instruction[] input, int startline, int file) {
		HashMap<String, Integer> pointers = new HashMap<>();
		int pointerCount = 0;
		for (int i = 0; i < input.length; i++)
			if(input[i].type == Pointer) {
				if(pointers.containsKey(input[i].argS[0]))
					throw new IGB_Compiler_L1_Exception(file, "Line: " + (i + 2) + "  Pointer: \"" + input[i].argS[0] + "\" already exists.");
				pointerCount++;
				pointers.put(input[i].argS[0], calcPointerLine(i, pointers.size(), startline));
			}

		return new Pair<>(pointerCount, pointers);
	}

	private int calcPointerLine(int line, int mapSize, int startline) { return line - mapSize - 1 + startline; }
}
