package com.amplifino.obelix.sequences;

import java.util.stream.LongStream;

public final class DoubleSequence implements Sequence<Double> {

	private final SequenceSpace<Double> space;
	private final long id;
	
	public DoubleSequence(SequenceSpace<Double> space, long id) {
		this.space = space;
		this.id = id;
	}
	
	@Override
	public Double get(long key) {
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
	public Sequence<Double> put(long index, Double value) {
		space.put(id, index, value);
		return this;
	}
	
	
}
