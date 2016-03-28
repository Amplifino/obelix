package com.amplifino.obelix.sequences;

import com.amplifino.obelix.sets.LongToLongFullFunction;


public interface LongSequence extends LongToLongFullFunction {
	
	LongSequence put(long key, long value);
}
