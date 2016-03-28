package com.amplifino.obelix.sequences;

import java.util.stream.LongStream;

import com.amplifino.obelix.segments.AbstractSegment;
import com.amplifino.obelix.space.ByteSpace;

public final class LongSequenceSpace extends AbstractSegment implements LongSequence {
	
	private static final short TYPETAG  = 514;
	private final ByteSpace longSpace;
	
	LongSequenceSpace(ByteSpace space) {
		super(space, TYPETAG);
		this.longSpace = space.shift(AbstractSegment.HEADERBYTES);
	}

	@Override
	public long get(long key) {
		return longSpace.getLong(key << 3);
	}
	
	@Override
	public LongStream domain() {
		return LongStream.range(0, longSpace.capacity() >>> 3);
	}

	@Override
	public LongSequence put(long key, long value) {
		longSpace.putLong(key << 3, value);
		return this;
	}
	
}
