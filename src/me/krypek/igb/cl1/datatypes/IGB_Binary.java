package me.krypek.igb.cl1.datatypes;

import java.io.Serializable;

import me.krypek.utils.Utils;

public record IGB_Binary(int startline, int[][] bin, String name) implements Serializable {

	private static String seperator = "\t----------------------------------------------------------\n";

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(30 + bin.length * 8 * 2 + bin.length);
		sb.append(startline + " ---->\n");

		for (int i = 0; i < bin.length; i++) {
			if(i % 3 == 0)
				sb.append(seperator);

			for (int val : bin[i]) { sb.append('\t'); sb.append(val); }
			sb.append('\n');

			if(i == bin.length - 1)
				sb.append(seperator);
		}
		sb.append('\n');
		return sb.toString();
	}

	public void serialize(String path) { Utils.serialize(this, path); }

	public void writeReadable(String path) { Utils.writeIntoFile(path, this.toString()); }
}
