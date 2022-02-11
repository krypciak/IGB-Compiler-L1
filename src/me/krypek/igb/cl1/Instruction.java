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

public class Instruction {

	@Override
	public String toString() {
		if(argLen == 0)
			return ":" + argS[0];
		StringBuilder sb = new StringBuilder(type.toString());
		for (int i = 0; i < argLen; i++) {
			sb.append(" ");
			if(i < 2 && argS[i] != null)
				sb.append(argS[i]);
			else if(i < 6 && argB[i] != null)
				sb.append(argB[i] ? 'c' : 'n');
			else
				sb.append(argD[i]);
		}
		return sb.toString();
	}

	public InstType type;

	private int argLen;

	public String[] argS = new String[7];
	public Double[] argD = new Double[8];
	public Boolean[] argB = new Boolean[7];

	public Instruction(InstType type, int argLen) {
		this.type = type;
		this.argLen = argLen;
	}

	public static Instruction If(String operation, int cell1, boolean isCell, int val2, String pointerToJump) {
		Instruction i = new Instruction(If, 5);
		i.argS[1] = operation;
		i.argD[2] = (double) cell1;
		i.argB[3] = isCell;
		i.argD[4] = (double) val2;
		i.argS[5] = pointerToJump;
		return i;
	}

	public static Instruction Init(double val, int cellToWrite) {
		Instruction i = new Instruction(Init, 2);
		i.argD[1] = val;
		i.argD[2] = (double) cellToWrite;
		return i;
	}

	public static Instruction Copy(int cell, int cellToWrite) {
		Instruction i = new Instruction(Copy, 2);
		i.argD[1] = (double) cell;
		i.argD[2] = (double) cellToWrite;
		return i;
	}

	public static Instruction Add(int cell1, boolean isCell, double val2, int cellToWrite) {
		Instruction i = new Instruction(Add, 4);
		i.argD[1] = (double) cell1;
		i.argB[2] = isCell;
		i.argD[3] = val2;
		i.argD[4] = (double) cellToWrite;
		return i;
	}

	public static Instruction Cell_Jump(int cell) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[1] = "Jump";
		i.argD[2] = (double) cell;
		return i;
	}
	
	public static Instruction Cell_Jump(String pointer) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[1] = "Jump";
		i.argS[2] = pointer;
		return i;
	}

	public static Instruction Cell_Call(int cell) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[1] = "Call";
		i.argD[2] = (double) cell;
		return i;
	}
	
	public static Instruction Cell_Call(String pointer) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[1] = "Call";
		i.argS[2] = pointer;
		return i;
	}

	public static Instruction Cell_Return() {
		Instruction i = new Instruction(Cell, 1);
		i.argS[1] = "Return";
		return i;
	}

	public static Instruction Pixel_Cache(boolean isCell1, double val1, boolean isCell2, double val2, boolean isCell3, double val3) {
		Instruction i = new Instruction(Pixel, 7);
		i.argS[1] = "Cache";
		i.argB[2] = isCell1;
		i.argD[3] = val1;
		i.argB[4] = isCell2;
		i.argD[5] = val2;
		i.argB[6] = isCell3;
		i.argD[7] = val3;
		return i;
	}

	public static Instruction Pixel_Cache_Raw(int raw) {
		Instruction i = new Instruction(Pixel, 3);
		i.argS[1] = "Cache";
		i.argS[2] = "Raw";
		i.argD[3] = (double) raw;
		return i;
	}

	public static Instruction Pixel_Cache(int cell) {
		Instruction i = new Instruction(Pixel, 2);
		i.argS[1] = "Cache";
		i.argD[2] = (double) cell;
		return i;
	}

	public static Instruction Pixel(boolean isCell1, int x, boolean isCell2, double y) {
		Instruction i = new Instruction(Pixel, 4);
		i.argB[1] = isCell1;
		i.argD[2] = (double) x;
		i.argB[3] = isCell2;
		i.argD[4] = y;
		return i;
	}

	public static Instruction Pixel_Get(boolean isCell1, int x, boolean isCell2, double y, int cellToWrite) {
		Instruction i = new Instruction(Pixel, 5);
		i.argB[1] = isCell1;
		i.argD[2] = (double) x;
		i.argB[3] = isCell2;
		i.argD[4] = y;
		i.argD[5] = (double) cellToWrite;
		return i;
	}

	public static Instruction Device_Wait(int ticks) {
		Instruction i = new Instruction(Device, 2);
		i.argS[1] = "CoreWait";
		i.argD[2] = (double) ticks;
		return i;
	}

	public static Instruction Device_ScreenUpdate() {
		Instruction i = new Instruction(Device, 1);
		i.argS[1] = "ScreenUpdate";
		return i;
	}

	public static Instruction Device_Log(boolean isCell, int val) {
		Instruction i = new Instruction(Device, 3);
		i.argS[1] = "Log";
		i.argB[2] = isCell;
		i.argD[3] = (double) val;
		return i;
	}

	public static Instruction Math(String operation, int cell1, boolean isCell, double val2, int cellToWrite) {
		if(operation.equals("+"))
			return Add(cell1, isCell, val2, cellToWrite);

		Instruction i = new Instruction(Math, 5);
		i.argS[1] = operation;
		i.argD[2] = (double) cell1;
		i.argB[3] = isCell;
		i.argD[4] = val2;
		i.argD[5] = (double) cellToWrite;
		return i;
	}

	public static Instruction Math_Random(double min, double max, int cellToWrite) {
		Instruction i = new Instruction(Math, 4);
		i.argS[1] = "R";
		i.argD[2] = min;
		i.argD[3] = max;
		i.argD[4] = (double) cellToWrite;
		return i;
	}

	/**
	 * ram[cellToWrite] = ram[ram[cell1]]
	 */

	public static Instruction Math_CC(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.argS[1] = "CC";
		i.argD[2] = (double) cell1;
		i.argD[3] = (double) cellToWrite;
		return i;
	}

	/**
	 * ram[ram[cellToWrite]] = ram[cell1]
	 */

	public static Instruction Math_CW(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.argS[1] = "CW";
		i.argD[2] = (double) cell1;
		i.argD[3] = (double) cellToWrite;
		return i;
	}

	public static Instruction Math_Sqrt(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.argS[1] = "sqrt";
		i.argD[2] = (double) cell1;
		i.argD[3] = (double) cellToWrite;
		return i;
	}

	public static Instruction Pointer(String name) {
		Instruction i = new Instruction(Pointer, 0);
		i.argS[1] = name;
		return i;
	}
}
