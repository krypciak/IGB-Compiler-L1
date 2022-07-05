package me.krypek.igb.cl1.datatypes;

import static me.krypek.igb.cl1.datatypes.InstType.Pointer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.krypek.igb.cl1.IGB_CL1_Exception;
import me.krypek.utils.Utils;

public record IGB_L1(int startline, Instruction[] code, String name, String path) implements Serializable {

	public IGB_Binary compile(L1Syntax syntax, HashMap<String, Integer> pointers, int pointerCount) {
		final int clen = code.length;
		final int binlen = clen - pointerCount;
		int[][] bin = new int[binlen][8];

		int pointerCountVerify = 0;
		for (int i = 0; i < clen; i++) {
			Instruction inst = code[i];
			if(inst.type == Pointer) {
				pointerCountVerify++;
				continue;
			}

			bin[i - pointerCountVerify] = inst.compile(syntax, pointers);
		}
		assert pointerCountVerify == pointerCount;

		return new IGB_Binary(startline, bin, name);
	}

	private static int i;

	public static IGB_L1 stringToL1(String path, String name, String contents) {
		String[] lines = contents.split("\n");
		final int startline;
		{
			String[] split = lines[0].split(" ");
			if(split.length != 2)
				throw new IGB_CL1_Exception(new File(path), "Startline has to be set.");
			if(!split[0].toLowerCase().equals("startline"))
				throw new IGB_CL1_Exception(new File(path), "Has to start with \"Startline\".");
			startline = Utils.parseIntError(split[1]);
		}

		List<Instruction> instList = new ArrayList<>();
		for (i = 1; i < lines.length; i++) {
			String line = lines[i];
			if(line.isBlank())
				continue;

			instList.add(Instruction.stringToInstruction(line, str -> new IGB_CL1_Exception(new File(path), i, "Unknown instruction: \"" + str + "\".")));
		}
		if(name.endsWith(".igb_l1_readable")) {
			name = name.substring(0, name.length() - ".igb_l1_readable".length());
		}
		return new IGB_L1(startline, instList.toArray(Instruction[]::new), name, path);
	}

	public void serialize(String path) { Utils.serialize(this, path); }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(code.length * 80);
		sb.append("Startline ");
		sb.append(startline);

		for (Instruction inst : code) {
			sb.append('\n');
			sb.append(inst.toString());

		}
		return sb.toString();
	}

	public void writeReadable(String path) { Utils.writeIntoFile(path, this.toString()); }
}
