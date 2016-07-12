package com.amplifino.obelix.space;

class ShiftedSpace extends WrappedSpace {
	
	private final long bias;
	
	ShiftedSpace (ByteSpace space, long bias) {
		super(space);
		this.bias = bias;
	}
	
	@Override
	long translate(long position, int offset) {
		return position + bias;
	}
	
	@Override
	public ByteSpace shift(long offset) {
		return space().shift(offset + bias);
	}
	
	@Override
	public ByteSpace capacity(long capacity) {
		return space().slice(bias, capacity);
	}
}
