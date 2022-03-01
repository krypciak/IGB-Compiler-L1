package me.krypek.igb.cl1;

import java.io.Serializable;

import me.krypek.utils.Utils;

public class IGB_L1 implements Serializable {
	private static final long serialVersionUID = 6013593729041470734L;

	public final Instruction[] code;
	public final int startline;

	public IGB_L1(int startline, Instruction[] code) {
		this.startline = startline;
		this.code = code;
	}

	@Override
	public String toString() { return "Startline " + startline + "\n" + Utils.arrayToString(code, "\n"); }
}
