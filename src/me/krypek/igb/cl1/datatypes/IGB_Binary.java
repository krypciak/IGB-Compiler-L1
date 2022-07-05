package me.krypek.igb.cl1.datatypes;

import java.io.Serializable;

import me.krypek.utils.Utils;

import static me.krypek.igb.cl1.IGB_MA.INVALID_INT;

public record IGB_Binary(int startline, int[][] bin, String name) implements Serializable {

	private void addSeperator(StringBuilder sb, int i) {
		sb.append('\t');
		sb.append("--");
		String numStr = String.valueOf(startline + i);
		sb.append(numStr);
		final int iterA = 50 - numStr.length();
		for (int x = 0; x < iterA; x++) { sb.append('-'); }
		sb.append('\n');
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(30 + bin.length * 8 * 2 + bin.length);
		sb.append(startline + " (" + bin.length + ") ---->\n");

		for (int i = 0; i < bin.length; i++) {
			if(i % 3 == 0)
				addSeperator(sb, i);

			for (int val : bin[i]) {
				if(val == INVALID_INT)
					break;
				sb.append('\t');
				sb.append(val);
			}
			sb.append('\n');

			if(i == bin.length - 1)
				addSeperator(sb, i + 1);
		}
		sb.append('\n');
		return sb.toString();
	}

	public void serialize(String path) { Utils.serialize(this, path); }

	public void writeReadable(String path) { Utils.writeIntoFile(path, this.toString()); }
}
