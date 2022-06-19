package me.krypek.igb.cl1;

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

public class IGB_CL1 {

	public static void main(String[] args) throws FileNotFoundException {

		/*
		 * int[][][] compiled1 = new IGB_CL1().compile(new IGB_L1[] { new IGB_L1(321,
		 * new Instruction[] { Instruction.Init(10, 1) }) }, new String[] { "imposter"
		 * }, -1); System.out.println(Arrays.deepToString(compiled1));
		 */

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
		final String fileOutput = data.getStringOrDef("op", null);
		final boolean readableOutput = data.has("ro");
		final int memorySize = data.getIntOrDef("ms", -1);
		final boolean quiet = data.has("q");

		String[][] inputs = new String[filePaths.length][];
		for (int i = 0; i < inputs.length; i++)
			inputs[i] = Utils.readFromFileToArray(filePaths[i]);

		int[][][] compiled = new IGB_CL1().compile(inputs, filePaths, memorySize);

		if(fileOutput != null) {
			File outputFile = new File(fileOutput);
			if(!fileOutput.equals("")) {
				if(outputFile.isDirectory())
					outputFile.mkdirs();
				else
					outputFile.getParentFile().mkdirs();
			}
			if(memorySize == -1) {
				if(readableOutput) {
					for (int i = 0; i < compiled.length; i++) {
						String out = intArrToString(compiled[i]);

						String name = new File(filePaths[i]).getName();
						String path = outputFile.getAbsolutePath() + "/" + Utils.getFileNameWithoutExtension(name) + ".igb_bin_readble";

						new File(path).getParentFile().mkdirs();

						PrintWriter pw = new PrintWriter(path);
						pw.println(out);
						pw.close();
					}
				} else {
					for (int i = 0; i < compiled.length; i++) {
						String name = new File(filePaths[i]).getName();
						String path = outputFile.getAbsolutePath() + "/" + Utils.getFileNameWithoutExtension(name) + ".igb_bin";

						new File(path).getParentFile().mkdirs();
						Utils.serialize(compiled[i], path);
					}
				}
			} else {
				String name = new File(filePaths[0]).getName();
				String path = outputFile.getAbsolutePath() + "/"
						+ (name.endsWith(".igb_l1") ? Utils.getFileNameWithoutExtension(name) + ".igb_pes" : name);

				new File(path).getParentFile().mkdirs();
				Utils.serialize(compiled[0], path);

			}
		}
		if(!quiet) {
			for (int i = 0; i < compiled.length; i++) {
				String out = intArrToString(compiled[i]);
				String name = new File(filePaths[i]).getName();
				System.out.println(name + ":\n" + out);
			}
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

	private final static int MULTIPLIER = 1000;
	public final static int IGNORE_INT = Integer.MIN_VALUE + 47328;

	private String[][] syntax;

	public IGB_CL1() { syntax = getSyntax(); }

	private static int temp;

	public int[][][] compile(String[][] inputs, String[] paths, IGB_L1[] l1A, String[] names, int pesSize) {
		assert inputs.length == paths.length;
		assert l1A.length == names.length;

		IGB_L1[] l1_Arr = new IGB_L1[inputs.length + l1A.length];
		String[] names1 = new String[l1_Arr.length];
		for (int i = 0; i < inputs.length; i++) {
			String[] in = inputs[i];
			List<Instruction> list = new ArrayList<>();
			if(!in[0].toLowerCase().startsWith("startline"))
				throw new IGB_Compiler_L1_Exception("Code file has to start with \"Startline *startline*\".");
			Integer startline = Utils.parseInt(in[0].substring(in[0].indexOf(' ') + 1));
			if(startline == null)
				throw new IGB_Compiler_L1_Exception("After \"Startline \" you need to put an valid integer.");
			for (int x = 1; x < in.length; x++) {
				temp = x + 1;
				Instruction inst = Instruction.stringToInstruction(inputs[i][x], str -> {
					return new IGB_Compiler_L1_Exception("Syntax error at line " + temp + ": " + str);
				});
				if(inst != null)
					list.add(inst);
			}
			l1_Arr[i] = new IGB_L1(startline, list.toArray(Instruction[]::new));
			names1[i] = new File(paths[i]).getName();
		}
		for (int i = inputs.length; i < l1_Arr.length; i++) {
			l1_Arr[i] = l1A[i - inputs.length];
			names1[i] = names[i - inputs.length];

		}

		return compile(l1_Arr, paths, pesSize);
	}

	public int[][][] compile(String[][] inputs, String[] paths, int pesSize) {
		IGB_L1[] igb_l1_Arr = new IGB_L1[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			String[] in = inputs[i];
			List<Instruction> list = new ArrayList<>();
			if(!in[0].toLowerCase().startsWith("startline"))
				throw new IGB_Compiler_L1_Exception("Code file has to start with \"Startline *startline*\".");
			Integer startline = Utils.parseInt(in[0].substring(in[0].indexOf(' ') + 1));
			if(startline == null)
				throw new IGB_Compiler_L1_Exception("After \"Startline \" you need to put an valid integer.");
			for (int x = 1; x < in.length; x++) {
				temp = x + 1;
				Instruction inst = Instruction.stringToInstruction(inputs[i][x], str -> {
					return new IGB_Compiler_L1_Exception("Syntax error at line " + temp + ": " + str);
				});
				if(inst != null)
					list.add(inst);
			}
			igb_l1_Arr[i] = new IGB_L1(startline, list.toArray(Instruction[]::new));
		}

		return compile(igb_l1_Arr, paths, pesSize);
	}

	public int[][][] compile(IGB_L1[] igb_l1_Arr, String[] paths, int pesSize) {
		IGB_Compiler_L1_Exception.paths = paths;
		Pair<Integer[], HashMap<String, Integer>> pair = getAllPointers(igb_l1_Arr);
		HashMap<String, Integer> pointers = pair.getSecond();
		Integer[] pointerCounts = pair.getFirst();
		InstType[] typeSyntax = new InstType[syntax.length];
		for (int i = 0; i < syntax.length; i++)
			typeSyntax[i] = InstType.valueOf(syntax[i][0].substring(0, syntax[i][0].indexOf('|')));
		final int[][][] ret;
		final int[][] pes;

		if(pesSize == -1) {
			ret = new int[igb_l1_Arr.length][][];
			pes = null;
		} else {
			ret = null;
			pes = new int[pesSize][];
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
					if(ins.type != typeSyntax[h] || ins.arg.length + 1 != syntax[h].length)
						continue;

					boolean prevCell = false;

					int[] potential = new int[8];
					if(ret != null)
						Arrays.fill(potential, IGNORE_INT);

					for (int j = 1; j < syntax[h].length; j++) {
						int _j = j - 1;

						String syntaxS = syntax[h][j];

						if(syntaxS.equals("@")) {
							if(!ins.arg[_j].isBool())
								continue syntaxFor;
							if(ins.arg[_j].bool()) {
								potential[j] = 1;
								prevCell = true;
							} else {
								potential[j] = 0;
								prevCell = false;
							}

						} else if(syntaxS.equals("d")) {
							if(!ins.arg[_j].isValue())
								continue syntaxFor;
							potential[j] = (int) (prevCell ? ins.arg[_j].val() : ins.arg[_j].val() * MULTIPLIER);
							prevCell = false;

						} else if(syntaxS.equals("i")) {
							if(!ins.arg[_j].isValue())
								continue syntaxFor;

							potential[j] = (int) ins.arg[_j].val();
							prevCell = false;

						} else if(syntaxS.equals("P")) {
							if(ins.arg[_j].isString()) {
								if(ins.arg[_j].str().charAt(0) != ':')
									throw new IGB_Compiler_L1_Exception(i, "Line: " + (x + 2) + "  Pointer name has to start with \':\'.");

								String pointerName = ins.arg[_j].str();
								int pointerCell = pointers.getOrDefault(pointerName, -1);
								if(pointerCell == -1)
									throw new IGB_Compiler_L1_Exception(i, "Line: " + (x + 2) + "  Pointer: \"" + pointerName + "\" doesn't exist.");
								potential[j] = pointerCell;
							} else if(ins.arg[_j].isValue())
								potential[j] = (int) ins.arg[_j].val();

						} else if(ins.arg[_j].isString() && syntaxS.substring(0, syntaxS.indexOf('|')).equals(ins.arg[_j].str())) {
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
				if(pointers.containsKey(input[i].arg[0].str()))
					throw new IGB_Compiler_L1_Exception(file, "Line: " + (i + 2) + "  Pointer: \"" + input[i].arg[0].str() + "\" already exists.");
				pointerCount++;
				pointers.put(input[i].arg[0].str(), calcPointerLine(i, pointers.size(), startline));
			}

		return new Pair<>(pointerCount, pointers);
	}

	private int calcPointerLine(int line, int mapSize, int startline) { return line - mapSize - 1 + startline; }

	public static int getMCRGBValue(int r, int g, int b) { return (r << 16) + (g << 8) + b; }
	
	public static double getMCRGBValueD(int r, int g, int b) { return (r << 16) + (g << 8) + b; }
}
