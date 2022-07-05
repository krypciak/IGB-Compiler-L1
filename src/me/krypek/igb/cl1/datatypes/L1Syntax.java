package me.krypek.igb.cl1.datatypes;

import static me.krypek.igb.cl1.IGB_MA.INVALID_INT;

import java.util.Map;

import me.krypek.igb.cl1.IGB_CL1_Exception;

public record L1Syntax(SyntaxArg[][][] syntax) {

	public int[] match(Instruction inst, Map<String, Integer> pointers) {
		InstArg[] args = inst.arg;
		final int typeOrdinal = inst.type.ordinal();
		SyntaxArg[][] syntax2 = syntax[typeOrdinal];
		int[] bin = { typeOrdinal, INVALID_INT, INVALID_INT, INVALID_INT, INVALID_INT, INVALID_INT, INVALID_INT, INVALID_INT };

		loop1: for (int i = 0; i < syntax2.length; i++) {
			SyntaxArg[] syntax1 = syntax2[i];
			if(args.length != syntax1.length)
				continue;
			for (int x = 0; x < syntax1.length; x++) {
				final int x_1 = x - 1;
				InstArg prevArg = x_1 < 0 ? null : args[x_1];

				int match = syntax1[x].match(prevArg, args[x], pointers);
				if(match == INVALID_INT)
					continue loop1;
				bin[x + 1] = match;
			}
			return bin;
		}
		throw new IGB_CL1_Exception("Syntax error: " + inst);
	}

}
