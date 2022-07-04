package me.krypek.igb.cl1.datatypes;

import java.io.Serializable;

public class InstArg {
	public String str() { return ((InstArgStr) this).str; }

	public double val() { return ((InstArgVal) this).value; }

	public boolean bool() { return ((InstArgBool) this).bool; }

	public void set(String str) { ((InstArgStr) this).str = str; }

	public void set(double val) { ((InstArgVal) this).value = val; }

	public void set(boolean bool) { ((InstArgBool) this).bool = bool; }

	public boolean isBool() { return this instanceof InstArgBool; }

	public boolean isValue() { return this instanceof InstArgVal; }

	public boolean isString() { return this instanceof InstArgStr; }
}

@SuppressWarnings("serial")
class InstArgVal extends InstArg implements Serializable {
	public double value;

	public InstArgVal(double value) { this.value = value; }

	@Override
	public String toString() { return String.valueOf(value); }
}

@SuppressWarnings("serial")
class InstArgStr extends InstArg implements Serializable {
	public String str;

	public InstArgStr(String str) { this.str = str; }

	@Override
	public String toString() { return str; }
}

@SuppressWarnings("serial")
class InstArgBool extends InstArg implements Serializable {
	public boolean bool;

	public InstArgBool(boolean boo) { this.bool = boo; }

	@Override
	public String toString() { return bool ? "c" : "n"; }
}
