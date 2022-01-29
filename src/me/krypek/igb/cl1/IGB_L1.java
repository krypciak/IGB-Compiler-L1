package me.krypek.igb.cl1;

import java.util.Arrays;

public class IGB_L1 {
	public final Instruction[] code;
	public final int startline;

	public IGB_L1(int startline, Instruction[] code) {
		this.startline = startline;
		this.code = code;
	}

	@Override
	public String toString() { return startline + ", " + Arrays.toString(code); }
}
