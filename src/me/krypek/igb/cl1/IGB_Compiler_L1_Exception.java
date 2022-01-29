package me.krypek.igb.cl1;

public class IGB_Compiler_L1_Exception extends RuntimeException {
	private static final long serialVersionUID = -8751110668161661362L;

	static String[] paths;

	IGB_Compiler_L1_Exception(String str) { super( str); }

	IGB_Compiler_L1_Exception(int index, String str) { super("File: \"" + paths[index] + "\"  " + str); }

	IGB_Compiler_L1_Exception(int index, String str, Exception e) { super("File: \"" + paths[index] + "\"  " + str, e); }
}
