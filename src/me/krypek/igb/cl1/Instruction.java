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
			else if(argD[i] != null)
				sb.append(argD[i]);
			else
				throw new IllegalArgumentException(i + "\t" + argS[i] + ", " + argB[i] + ", " + argD[i]);
		}
		return sb.toString();
	}

	public InstType type;

	private int argLen;

	public String[] argS = new String[6];
	public Double[] argD = new Double[7];
	public Boolean[] argB = new Boolean[6];

	public Instruction(InstType type, int argLen) {
		this.type = type;
		this.argLen = argLen;
	}

	public static Instruction If(String operation, int cell1, boolean isCell, int val2, String pointerToJump) {
		Instruction i = new Instruction(If, 5);
		i.argS[0] = operation;
		i.argD[1] = (double) cell1;
		i.argB[2] = isCell;
		i.argD[3] = (double) val2;
		i.argS[4] = pointerToJump;
		return i;
	}

	public static Instruction Init(double val, int cellToWrite) {
		Instruction i = new Instruction(Init, 2);
		i.argD[0] = val;
		i.argD[1] = (double) cellToWrite;
		return i;
	}

	public static Instruction Copy(int cell, int cellToWrite) {
		Instruction i = new Instruction(Copy, 2);
		i.argD[0] = (double) cell;
		i.argD[1] = (double) cellToWrite;
		return i;
	}

	public static Instruction Add(int cell1, boolean isCell, double val2, int cellToWrite) {
		Instruction i = new Instruction(Add, 4);
		i.argD[0] = (double) cell1;
		i.argB[1] = isCell;
		i.argD[2] = val2;
		i.argD[3] = (double) cellToWrite;
		return i;
	}

	public static Instruction Cell_Jump(int cell) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[0] = "Jump";
		i.argD[1] = (double) cell;
		return i;
	}

	public static Instruction Cell_Jump(String pointer) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[0] = "Jump";
		i.argS[1] = pointer;
		return i;
	}

	public static Instruction Cell_Call(int cell) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[0] = "Call";
		i.argD[1] = (double) cell;
		return i;
	}

	public static Instruction Cell_Call(String pointer) {
		Instruction i = new Instruction(Cell, 2);
		i.argS[0] = "Call";
		i.argS[1] = pointer;
		return i;
	}

	public static Instruction Cell_Return() {
		Instruction i = new Instruction(Cell, 1);
		i.argS[0] = "Return";
		return i;
	}

	public static Instruction Pixel_Cache(boolean isCell1, double val1, boolean isCell2, double val2, boolean isCell3, double val3) {
		Instruction i = new Instruction(Pixel, 7);
		i.argS[0] = "Cache";
		i.argB[1] = isCell1;
		i.argD[2] = val1;
		i.argB[3] = isCell2;
		i.argD[4] = val2;
		i.argB[5] = isCell3;
		i.argD[6] = val3;
		return i;
	}

	public static Instruction Pixel_Cache_Raw(int raw) {
		Instruction i = new Instruction(Pixel, 3);
		i.argS[0] = "Cache";
		i.argS[1] = "Raw";
		i.argD[2] = (double) raw;
		return i;
	}

	public static Instruction Pixel_Cache(int cell) {
		Instruction i = new Instruction(Pixel, 2);
		i.argS[0] = "Cache";
		i.argD[1] = (double) cell;
		return i;
	}

	public static Instruction Pixel(boolean isCell1, int x, boolean isCell2, double y) {
		Instruction i = new Instruction(Pixel, 4);
		i.argB[0] = isCell1;
		i.argD[1] = (double) x;
		i.argB[2] = isCell2;
		i.argD[3] = y;
		return i;
	}

	public static Instruction Pixel_Get(boolean isCell1, int x, boolean isCell2, double y, int cellToWrite) {
		Instruction i = new Instruction(Pixel, 5);
		i.argB[0] = isCell1;
		i.argD[1] = (double) x;
		i.argB[2] = isCell2;
		i.argD[3] = y;
		i.argD[4] = (double) cellToWrite;
		return i;
	}

	public static Instruction Device_Wait(int ticks) {
		Instruction i = new Instruction(Device, 2);
		i.argS[0] = "CoreWait";
		i.argD[1] = (double) ticks;
		return i;
	}

	public static Instruction Device_ScreenUpdate() {
		Instruction i = new Instruction(Device, 1);
		i.argS[0] = "ScreenUpdate";
		return i;
	}

	public static Instruction Device_Log(boolean isCell, int val) {
		Instruction i = new Instruction(Device, 3);
		i.argS[0] = "Log";
		i.argB[1] = isCell;
		i.argD[2] = (double) val;
		return i;
	}

	public static Instruction Math(String operation, int cell1, boolean isCell, double val2, int cellToWrite) {
		if(operation.equals("+"))
			return Add(cell1, isCell, val2, cellToWrite);

		Instruction i = new Instruction(Math, 5);
		i.argS[0] = operation;
		i.argD[1] = (double) cell1;
		i.argB[2] = isCell;
		i.argD[3] = val2;
		i.argD[4] = (double) cellToWrite;
		return i;
	}

	public static Instruction Math(char ope, int cell1, boolean isCell, double val2, int cellToWrite) {
		if(ope == '+')
			return Add(cell1, isCell, val2, cellToWrite);

		Instruction i = new Instruction(Math, 5);
		i.argS[0] = ope + "";
		i.argD[1] = (double) cell1;
		i.argB[2] = isCell;
		i.argD[3] = val2;
		i.argD[4] = (double) cellToWrite;
		return i;
	}

	public static Instruction Math_Random(double min, double max, int cellToWrite) {
		Instruction i = new Instruction(Math, 4);
		i.argS[0] = "R";
		i.argD[1] = min;
		i.argD[2] = max;
		i.argD[3] = (double) cellToWrite;
		return i;
	}

	/**
	 * ram[cellToWrite] = ram[ram[cell1]]
	 */

	public static Instruction Math_CC(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.argS[0] = "CC";
		i.argD[1] = (double) cell1;
		i.argD[2] = (double) cellToWrite;
		return i;
	}

	/**
	 * ram[ram[cellToWrite]] = ram[cell1]
	 */

	public static Instruction Math_CW(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.argS[0] = "CW";
		i.argD[1] = (double) cell1;
		i.argD[2] = (double) cellToWrite;
		return i;
	}

	public static Instruction Math_Sqrt(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.argS[0] = "sqrt";
		i.argD[1] = (double) cell1;
		i.argD[2] = (double) cellToWrite;
		return i;
	}

	public static Instruction Pointer(String name) {
		Instruction i = new Instruction(Pointer, 0);
		i.argS[0] = name;
		return i;
	}
}
