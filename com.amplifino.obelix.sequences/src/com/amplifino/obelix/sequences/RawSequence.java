package com.amplifino.obelix.sequences;

import java.util.stream.LongStream;

public final class RawSequence implements Sequence<byte[]> {

	private final RawSequenceSpace space;
	private final long id;
	
	public RawSequence(RawSequenceSpace space, long id) {
		this.space = space;
		this.id = id;
	}
	
	@Override
	public byte[] get(long key) {
		return space.get(id, key);
	}

	@Override
	public LongStream domain() {
		return LongStream.range(0, Long.MAX_VALUE);
	}

	public long id() {
		return id;
	}

	@Override
	public Sequence<byte[]> put(long index, byte[] value) {
		space.put(id, index, value);
		return this;
	}
		
}
