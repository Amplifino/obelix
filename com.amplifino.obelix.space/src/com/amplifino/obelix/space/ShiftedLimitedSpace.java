package com.amplifino.obelix.space;

class ShiftedLimitedSpace extends WrappedSpace {
	
	private final long bias;
	private final long capacity;
	
	ShiftedLimitedSpace (ByteSpace space, long bias, long capacity) {
		super(space);
		this.bias = bias;
		this.capacity = capacity;
	}
	
	@Override
	public long capacity() {
		return capacity;
	}
	
	@Override
	long translate(long position, int offset) {		
		if (Long.compareUnsigned(capacity,  position + offset) < 0) {
			throw new IllegalArgumentException("Invalid position: " + position + " or length: " + offset);
		}  
		return position + bias;
	}
	
	
}
