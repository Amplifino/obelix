package com.amplifino.obelix.space;

class LimitedSpace extends WrappedSpace {
	
	private final long capacity;
	
	LimitedSpace (ByteSpace space, long capacity) {
		super(space);
		this.capacity = capacity;
	}
	
	@Override
	long translate(long position , int length) {
		if (Long.compareUnsigned(capacity,  position + length) < 0) {
			throw new IllegalArgumentException("Invalid position: " + position + " or length: " + length);
		}  
		return position;
	}
	
	@Override
	public long capacity() {
		return capacity;
	}
	
	@Override
	public ByteSpace capacity(long newCapacity) {
		if (capacity == newCapacity) {
			return this;
		}
		if (Long.compareUnsigned(newCapacity, capacity) > 0) {
			throw new IllegalArgumentException("New capacity: " + newCapacity + "exceeds current capacity: " + capacity);
		}
		return space().capacity(newCapacity);
	}
	
	@Override
	public ByteSpace shift(long shift) {
		return space().slice(shift, capacity);
	}
}
