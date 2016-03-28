package com.amplifino.obelix.space;

class ShiftedSpace extends WrappedSpace {
	
	private final long bias;
	
	ShiftedSpace (ByteSpace space, long bias) {
		super(space);
		this.bias = bias;
	}
	
	@Override
	public ByteSpace get(long position, byte[] bytes, int start, int length) { 
		return super.get(position + bias, bytes, start, length);
	}

	@Override
	public ByteSpace put(long position, byte[] bytes, int start, int length) {
		return super.put(position + bias, bytes, start, length);
	}
	
	@Override
	public ByteSpace shift(long offset) {
		return space().shift(offset + bias);
	}
}
