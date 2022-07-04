package me.krypek.igb.cl1;

import java.io.File;

public class IGB_CL1_Exception extends RuntimeException {
	private static final long serialVersionUID = -8751110668161661362L;

	public IGB_CL1_Exception(File file, String message) { super("File: \"" + file.getAbsolutePath() + "\"  " + message); }

	public IGB_CL1_Exception(File file, int line, String message) { super("File: \"" + file.getAbsolutePath() + "\"  Line: " + line + "  " + message); }

	public IGB_CL1_Exception(String str) { super(str); }
}
