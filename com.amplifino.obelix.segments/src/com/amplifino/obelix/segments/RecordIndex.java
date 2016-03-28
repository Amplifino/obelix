package com.amplifino.obelix.segments;

import java.util.Optional;
import java.util.stream.LongStream;

import com.amplifino.obelix.guards.LongGuards;

interface RecordIndex extends Segment {
	long create(int size, LongGuards guards);
	boolean replace(long key, byte[] payload);
	void remove(long key);
	int spareBits();
	Optional<byte[]> get(long key);
	LongStream keys();
	boolean contains(long key);
}
