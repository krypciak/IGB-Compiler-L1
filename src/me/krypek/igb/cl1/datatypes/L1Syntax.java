package me.krypek.igb.cl1.datatypes;

import java.util.Arrays;
import java.util.Map;

import me.krypek.igb.cl1.IGB_CL1_Exception;

public record L1Syntax(SyntaxArg[][][] syntax) {

	public int[] match(Instruction inst, Map<String, Integer> pointers) {
		InstArg[] args = inst.arg;
		final int typeOrdinal = inst.type.ordinal();
		SyntaxArg[][] syntax2 = syntax[typeOrdinal];
		int[] bin = new int[8];
		bin[0] = typeOrdinal;
		int len = 1;
		loop1: for (int i = 0; i < syntax2.length; i++) {
			SyntaxArg[] syntax1 = syntax2[i];
			if(args.length != syntax1.length)
				continue;
			for (int x = 0; x < syntax1.length; x++) {
				final int x_1 = x - 1;
				InstArg prevArg = x_1 < 0 ? null : args[x_1];

				int match = syntax1[x].match(prevArg, args[x], pointers);
				if(match == SyntaxArg.INVALID_INT)
					continue loop1;
				bin[x + 1] = match;
				len++;
			}
			return Arrays.copyOf(bin, len);
		}
		throw new IGB_CL1_Exception("Syntax error: " + inst);
	}

}
