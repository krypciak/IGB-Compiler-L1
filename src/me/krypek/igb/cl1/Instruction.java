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

import java.io.Serializable;

public class Instruction implements Serializable {
	private static final long serialVersionUID = -3326258133861457917L;

	@Override
	public String toString() {
		if(type == Pointer)
			return arg[0].str();
		StringBuilder sb = new StringBuilder(type.toString());

		for (int i = 0; i < argLen; i++) { sb.append(" "); sb.append(arg[i].toString()); }
		return sb.toString();
	}

	public InstType type;

	private final int argLen;

	public final InstArg[] arg;

	public Instruction(InstType type, int argLen) {
		this.type = type;
		this.argLen = argLen;
		arg = new InstArg[argLen];
	}

	public static InstArg get(double val) { return new InstArgVal(val); }

	public static InstArg get(int val) { return new InstArgVal((int) val); }

	public static InstArg get(boolean bool) { return new InstArgBool(bool); }

	public static InstArg get(String str) { return new InstArgStr(str); }

	public static Instruction If(String operation, int cell1, boolean isCell, double val2, String pointerToJump) {
		Instruction i = new Instruction(If, 5);
		i.arg[0] = get(operation);
		i.arg[1] = get(cell1);
		i.arg[2] = get(isCell);
		i.arg[3] = get(val2);
		i.arg[4] = get(pointerToJump);
		return i;
	}

	public static Instruction Init(double val, int cellToWrite) {
		Instruction i = new Instruction(Init, 2);
		i.arg[0] = get(val);
		i.arg[1] = get(cellToWrite);
		return i;
	}

	public static Instruction Copy(int cell, int cellToWrite) {
		Instruction i = new Instruction(Copy, 2);
		i.arg[0] = get(cell);
		i.arg[1] = get(cellToWrite);
		return i;
	}

	public static Instruction Add(int cell1, boolean isCell, double val2, int cellToWrite) {
		Instruction i = new Instruction(Add, 4);
		i.arg[0] = get(cell1);
		i.arg[1] = get(isCell);
		i.arg[2] = get(val2);
		i.arg[3] = get(cellToWrite);
		return i;
	}

	public static Instruction Cell_Jump(int cell) {
		Instruction i = new Instruction(Cell, 2);
		i.arg[0] = get("Jump");
		i.arg[1] = get(cell);
		return i;
	}

	public static Instruction Cell_Jump(String pointer) {
		Instruction i = new Instruction(Cell, 2);
		i.arg[0] = get("Jump");
		i.arg[1] = get(pointer);
		return i;
	}

	public static Instruction Cell_Call(int cell) {
		Instruction i = new Instruction(Cell, 2);
		i.arg[0] = get("Call");
		i.arg[1] = get(cell);
		return i;
	}

	public static Instruction Cell_Call(String pointer) {
		Instruction i = new Instruction(Cell, 2);
		i.arg[0] = get("Call");
		i.arg[1] = get(pointer);
		return i;
	}

	public static Instruction Cell_Return() {
		Instruction i = new Instruction(Cell, 1);
		i.arg[0] = get("Return");
		return i;
	}

	public static Instruction Pixel_Cache(boolean isCell1, double val1, boolean isCell2, double val2, boolean isCell3, double val3) {
		if(!isCell1 && !isCell2 && !isCell3) {
			return Pixel_Cache_Raw(IGB_CL1.getMCRGBValue((int) val1, (int) val2, (int) val3));
		}
		Instruction i = new Instruction(Pixel, 7);
		i.arg[0] = get("Cache");
		i.arg[1] = get(isCell1);
		i.arg[2] = get(val1);
		i.arg[3] = get(isCell2);
		i.arg[4] = get(val2);
		i.arg[5] = get(isCell3);
		i.arg[6] = get(val3);
		return i;
	}

	public static Instruction Pixel_Cache_Raw(int raw) {
		Instruction i = new Instruction(Pixel, 3);
		i.arg[0] = get("Cache");
		i.arg[1] = get("Raw");
		i.arg[2] = get(raw);
		return i;
	}

	public static Instruction Pixel_Cache(int cell) {
		Instruction i = new Instruction(Pixel, 2);
		i.arg[0] = get("Cache");
		i.arg[1] = get(cell);
		return i;
	}

	public static Instruction Pixel(boolean isCell1, int x, boolean isCell2, double y) {
		Instruction i = new Instruction(Pixel, 4);
		i.arg[0] = get(isCell1);
		i.arg[1] = get(x);
		i.arg[2] = get(isCell2);
		i.arg[3] = get(y);
		return i;
	}

	public static Instruction Pixel_Get(boolean isCell1, int x, boolean isCell2, double y, int cellToWrite) {
		Instruction i = new Instruction(Pixel, 5);
		i.arg[0] = get(isCell1);
		i.arg[1] = get(x);
		i.arg[2] = get(isCell2);
		i.arg[3] = get(y);
		i.arg[4] = get(cellToWrite);
		return i;
	}

	public static Instruction Device_Wait(int ticks) {
		Instruction i = new Instruction(Device, 2);
		i.arg[0] = get("CoreWait");
		i.arg[1] = get(ticks);
		return i;
	}

	public static Instruction Device_ScreenUpdate() {
		Instruction i = new Instruction(Device, 1);
		i.arg[0] = get("ScreenUpdate");
		return i;
	}

	public static Instruction Device_Log(boolean isCell, double val) {
		Instruction i = new Instruction(Device, 3);
		i.arg[0] = get("Log");
		i.arg[1] = get(isCell);
		i.arg[2] = get(val);
		return i;
	}

	public static Instruction Math(String operation, int cell1, boolean isCell, double val2, int cellToWrite) {
		if(operation.equals("+"))
			return Add(cell1, isCell, val2, cellToWrite);

		Instruction i = new Instruction(Math, 5);
		i.arg[0] = get(operation);
		i.arg[1] = get(cell1);
		i.arg[2] = get(isCell);
		i.arg[3] = get(val2);
		i.arg[4] = get(cellToWrite);
		return i;
	}

	public static Instruction Math(char ope, int cell1, boolean isCell, double val2, int cellToWrite) {
		if(ope == '+')
			return Add(cell1, isCell, val2, cellToWrite);

		Instruction i = new Instruction(Math, 5);
		i.arg[0] = get(ope + "");
		i.arg[1] = get(cell1);
		i.arg[2] = get(isCell);
		i.arg[3] = get(val2);
		i.arg[4] = get(cellToWrite);
		return i;
	}

	public static Instruction Math_Random(double min, double max, int cellToWrite) {
		Instruction i = new Instruction(Math, 4);
		i.arg[0] = get("R");
		i.arg[1] = get(min);
		i.arg[2] = get(max);
		i.arg[3] = get(cellToWrite);
		return i;
	}

	/**
	 * ram[cellToWrite] = ram[ram[cell1]]
	 */

	public static Instruction Math_CC(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.arg[0] = get("CC");
		i.arg[1] = get(cell1);
		i.arg[2] = get(cellToWrite);
		return i;
	}

	/**
	 * ram[ram[cellToWrite]] = ram[cell1]
	 */

	public static Instruction Math_CW(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.arg[0] = get("CW");
		i.arg[1] = get(cell1);
		i.arg[2] = get(cellToWrite);
		return i;
	}

	public static Instruction Math_Sqrt(int cell1, int cellToWrite) {
		Instruction i = new Instruction(Math, 3);
		i.arg[0] = get("sqrt");
		i.arg[1] = get(cell1);
		i.arg[2] = get(cellToWrite);
		return i;
	}

	public static Instruction Pointer(String name) {
		Instruction i = new Instruction(Pointer, 1);
		i.arg[0] = get(name);

		return i;
	}
}
