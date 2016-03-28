package com.amplifino.obelix.space;

import java.util.Objects;
import java.util.stream.LongStream;

class DefaultCellSpace implements CellSpace {

	private final ByteSpace space;
	private final int cellSize;
	private final int cellShift;
	
	DefaultCellSpace(ByteSpace space, int cellSize) {
		this.space = space;
		this.cellSize = cellSize;
		this.cellShift = Integer.bitCount(cellSize - 1);
	}
	
	@Override
	public byte[] get(long key) {
		return space.getBytes(key << cellShift, cellSize);
	}
	
	@Override
	public LongStream domain() {
		return LongStream.range(0, capacity());
	}

	@Override
	public CellSpace put(long key, byte[] value) {
		if (Objects.requireNonNull(value).length != cellSize) {
			throw new IllegalArgumentException();
		}
		space.put(key << cellShift, value);
		return this;
	}

	@Override
	public int cellSize() {
		return cellSize;
	}

	@Override
	public long capacity() {
		return space.capacity() >>> cellShift;
	}

}
