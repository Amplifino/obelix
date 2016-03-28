package com.amplifino.obelix.sequences;


public interface SequenceSpace<T> {

	T get(long sequenceId, long key);
	SequenceSpace<T> put(long sequenceId, long key , T value);

}
