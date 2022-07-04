package me.krypek.igb.cl1;

import static me.krypek.igb.cl1.datatypes.InstType.Pointer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import me.krypek.freeargparser.ArgType;
import me.krypek.freeargparser.ParsedData;
import me.krypek.freeargparser.ParserBuilder;
import me.krypek.igb.cl1.datatypes.IGB_Binary;
import me.krypek.igb.cl1.datatypes.IGB_L1;
import me.krypek.igb.cl1.datatypes.InstType;
import me.krypek.igb.cl1.datatypes.Instruction;
import me.krypek.igb.cl1.datatypes.L1Syntax;
import me.krypek.igb.cl1.datatypes.SyntaxArg;
import me.krypek.utils.LinkedHashMapCollector;
import me.krypek.utils.Pair;
import me.krypek.utils.Utils;

public class IGB_CL1 {

	public static void main(String[] args) {
		//@f:off
		ParsedData data = new ParserBuilder()
				.add("cp", "codepath", 		true,  false, ArgType.StringArray, 	"Array of code paths.")
				.add("op", "outputpath", 	false, false, ArgType.String, 		"Output directory path.")
				.add("ro", "readableOutput",false, false, ArgType.None, 		"If selected, will save readable binaries insted.")
				.add("q",  "quiet",  		false, false, ArgType.None, 		"If selected, won't print out output.")
				.add("ps", "printSyntax", 	false, false, ArgType.Boolean, 		"If selected, will print out syntax.")
				.parse(args);
		//@f:on

		final String[] filePaths = data.getStringArray("cp");
		final String fileOutput = data.getStringOrDef("op", null);
		final boolean readableOutput = data.has("ro");
		final boolean quiet = data.has("q");
		final boolean printSyntax = data.has("ps");

		IGB_CL1 cl1 = new IGB_CL1(quiet, printSyntax);

		IGB_L1[] l1s = Stream.of(filePaths).map(path -> {
			String contents = Utils.readFromFile(path, "\n");
			if(contents == null)
				throw new IGB_CL1_Exception("Error reading file: \"" + path + "\".");
			return IGB_L1.stringToL1(path, Utils.getFileName(path), contents);
		}).toArray(IGB_L1[]::new);

		IGB_Binary[] bins = cl1.compile(l1s);

	}

	private record L1SyntaxTEMP(InstType type, SyntaxArg[] syntax) {}

	private L1Syntax syntax;
	private final boolean quiet, printSyntax;

	public IGB_CL1(boolean quiet, boolean printSyntax) {
		this.quiet = quiet;
		this.printSyntax = printSyntax;
	}

	public IGB_Binary[] compile(IGB_L1[] l1s) {
		final int len = l1s.length;
		IGB_Binary[] bins = new IGB_Binary[len];

		if(!quiet)
			System.out.println("\n-----IGB CL1----------------------------------------------------------\n");

		if(syntax == null)
			syntax = getSyntax(printSyntax);

		Pair<int[], HashMap<String, Integer>> pair = getAllPointers(l1s);
		HashMap<String, Integer> pointers = pair.getSecond();

		int[] pointerCounts = pair.getFirst();

		for (int i = 0; i < len; i++)
			bins[i] = l1s[i].compile(syntax, pointers, pointerCounts[i]);

		if(!quiet)
			Stream.of(bins).forEach(System.out::println);

		return bins;
	}

	public Pair<int[], HashMap<String, Integer>> getAllPointers(IGB_L1[] l1s) {
		final int len = l1s.length;
		int[] pointerCounts = new int[len];
		HashMap<String, Integer> allPointers = new HashMap<>();

		for (int i = 0; i < len; i++) {
			IGB_L1 l1 = l1s[i];
			Instruction[] code = l1.code();
			HashMap<String, Integer> pointers = new HashMap<>();
			for (int x = 0; x < code.length; x++) {
				Instruction inst = code[x];
				if(inst.type != Pointer)
					continue;
				String pointerName = inst.getPointerName();
				if(allPointers.containsKey(pointerName) || pointers.containsKey(pointerName))
					throw new IGB_CL1_Exception("Line: " + (x + 1) + "  Pointer: \"" + pointerName + "\" already exists.");

				pointers.put(inst.getPointerName(), x - allPointers.size() - pointers.size() - 1 + l1.startline());
			}
			pointerCounts[i] = pointers.size();
			allPointers.putAll(pointers);
		}

		if(!quiet)
			System.out.println(pointersToString(allPointers));

		return new Pair<>(pointerCounts, allPointers);
	}

	private String pointersToString(HashMap<String, Integer> pointers) {
		StringBuilder sb = new StringBuilder(pointers.size() * 30);
		sb.append("Pointers: {");
		Iterator<Map.Entry<String, Integer>> iterator = pointers.entrySet().iterator();

		for (int i = 0; iterator.hasNext(); i++) {
			Map.Entry<String, Integer> entry = iterator.next();
			if(i % 5 == 0)
				sb.append('\n');

			sb.append('\t');
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(entry.getValue());
		}
		sb.append("\n}\n");
		return sb.toString();
	}

	private L1Syntax getSyntax(boolean printSyntax) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("commandSet.txt");
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			//@f:off
			SyntaxArg[][][] syntax = r.lines()
				// removes empty lines
				.filter(str -> !str.isBlank())
				// extracts L1SyntaxTEMP out of every line; returns Stream<L1SyntaxTEMP>
				.map(IGB_CL1::processSyntaxLine) 
				// collects Stream<L1SyntaxTEMP> to LinkedHashMap<InstType, List<L1SyntaxTEMP>>
				.collect(LinkedHashMapCollector.get(L1SyntaxTEMP::type, Utils::listOf, (s, a) -> { s.addAll(a); return s; }))
				// grabs the ordered entry set Set<L1SyntaxTEMP>
				.entrySet().stream()
				// sorts it to make sure it's in order of InstType
				.sorted((o1, o2) -> o1.getKey().ordinal()- o2.getKey().ordinal())
				// returns Stream<SyntaxArg[][]>
				.map(entry -> entry.getValue().stream().map(L1SyntaxTEMP::syntax).toArray(SyntaxArg[][]::new))
				.toArray(SyntaxArg[][][]::new);
			//@f:on

			r.close();
			assert syntax.length == 8;

			if(!quiet && printSyntax)
				System.out.println(syntaxToString(syntax));

			return new L1Syntax(syntax);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String syntaxToString(SyntaxArg[][][] syntax) {
		StringBuilder sb = new StringBuilder(syntax.length * 40);
		sb.append("Syntax: {\n");
		for (int i = 0; i < syntax.length; i++) {
			sb.append('\t');
			sb.append(InstType.values()[i]);
			sb.append(":");
			for (int x = 0; x < syntax[i].length; x++) {
				sb.append('\t');
				if(x != 0)
					sb.append('\t');

				final int len = syntax[i][x].length;
				for (int h = 0; h < len; h++) {
					sb.append(syntax[i][x][h]);
					if(h != len - 1)
						sb.append(' ');
				}
				sb.append('\n');
			}
			sb.append('\n');
		}
		sb.append("}\n");
		return sb.toString();
	}

	private static L1SyntaxTEMP processSyntaxLine(String str) {
		final int index0 = str.indexOf(' ');
		assert index0 != -1;
		String typeStrAll = str.substring(0, index0);
		str = str.substring(index0 + 1);

		final int index1 = typeStrAll.indexOf('|');
		assert index1 != -1;
		String typeStr = typeStrAll.substring(0, index1);

		InstType type = InstType.valueOf(typeStr);
		assert type.ordinal() == Utils.parseIntError(typeStrAll.substring(index1 + 1));

		SyntaxArg[] sargA = Stream.of(str.split(" ")).map(str1 -> SyntaxArg.get(str1)).toArray(SyntaxArg[]::new);

		return new L1SyntaxTEMP(type, sargA);
	}

	public static int getMCRGBValue(int r, int g, int b) { return (r << 16) + (g << 8) + b; }

	public static double getMCRGBValueD(int r, int g, int b) { return (r << 16) + (g << 8) + b; }
}
