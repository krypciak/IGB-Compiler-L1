package me.krypek.igb.cl1.datatypes;

import java.util.HashMap;

import me.krypek.igb.cl1.IGB_CL1_Exception;
import me.krypek.utils.Utils;

import static me.krypek.igb.cl1.IGB_MA.INVALID_INT;

public interface SyntaxArg {

	public int match(InstArg prevArg, InstArg arg, HashMap<String, Integer> pointers);

	public static SyntaxArg get(String arg) {
		return switch (arg.toLowerCase()) {
			case "p" -> new SyntaxArgPointer();
			case "d" -> new SyntaxArgDouble();
			case "i" -> new SyntaxArgInteger();
			case "@" -> new SyntaxArgBool();
			default -> {
				final int index = arg.indexOf('|');
				assert index != -1;
				String str = arg.substring(0, index);
				int val = Utils.parseIntError(arg.substring(index + 1));
				yield new SyntaxArgString(str, val);
			}
		};
	}

	static record SyntaxArgString(String str, int val) implements SyntaxArg {
		public int match(InstArg prevArg, InstArg arg, HashMap<String, Integer> pointers) {
			return arg.isString() && arg.str().equals(str) ? val : INVALID_INT;
		}

		@Override
		public String toString() { return str + "|" + val; }

	}

	static record SyntaxArgPointer() implements SyntaxArg {
		public int match(InstArg prevArg, InstArg arg, HashMap<String, Integer> pointers) {
			if(arg.isValue()) {
				assert arg.val() % 1 == 0;
				return (int) arg.val();
			} else if(arg.isString()) {
				String pointerName = arg.str();
				int line = pointers.getOrDefault(pointerName, INVALID_INT);
				if(line == INVALID_INT)
					throw new IGB_CL1_Exception("Pointer: \"" + pointerName + "\" doesn't exist.");
				return line;
			} else
				return INVALID_INT;
		}

		@Override
		public String toString() { return "P"; }
	}

	static record SyntaxArgDouble() implements SyntaxArg {

		public int match(InstArg prevArg, InstArg arg, HashMap<String, Integer> pointers) {
			if(arg.isValue()) {
				if(prevArg == null || (prevArg.isBool() && !prevArg.bool()))
					return (int) (arg.val() * 1000);
				else {
					assert arg.val() % 1 == 0;
					return (int) arg.val();
				}
			} else
				return INVALID_INT;
		}

		@Override
		public String toString() { return "d"; }
	}

	static record SyntaxArgInteger() implements SyntaxArg {

		public int match(InstArg prevArg, InstArg arg, HashMap<String, Integer> pointers) {
			if(arg.isValue()) {
				assert arg.val() % 1 == 0;
				return (int) arg.val();
			}
			return INVALID_INT;
		}

		@Override
		public String toString() { return "i"; }
	}

	static record SyntaxArgBool() implements SyntaxArg {

		public int match(InstArg prevArg, InstArg arg, HashMap<String, Integer> pointers) { return arg.isBool() ? (arg.bool() ? 1 : 0) : INVALID_INT; }

		@Override
		public String toString() { return "@"; }
	}

}
