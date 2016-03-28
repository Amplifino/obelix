package com.amplifino.obelix.space;

class LimitedSpace extends WrappedSpace {
	
	private final long capacity;
	
	LimitedSpace (ByteSpace space, long capacity) {
		super(space);
		this.capacity = capacity;
	}
	
	private void check(long position , int length) {
		if (Long.compareUnsigned(capacity,  position + length) < 0) {
			throw new IllegalArgumentException("Invalid position: " + position + " or length: " + length);
		}  
	}
	
	@Override
	public ByteSpace get(long position, byte[] bytes, int start, int length) {
		check(position, length); 
		return super.get(position, bytes, start, length);
	}

	@Override
	public ByteSpace put(long position, byte[] bytes, int start, int length) {
		check(position, length);
		return super.put(position, bytes, start, length);
	}
	
	@Override
	public long capacity() {
		return capacity;
	}
	
	@Override
	public ByteSpace capacity(long newCapacity) {
		return space().capacity(newCapacity);
	}
}
