package com.amplifino.obelix.space;

import java.io.IOException;

abstract class WrappedSpace implements ByteSpace {

	private final ByteSpace space;

	WrappedSpace(ByteSpace space) {
		this.space = space;
	}
	
	
	@Override
	public ByteSpace get(long position, byte[] bytes, int start, int length) {
		space.get(position, bytes, start, length);
		return this;
	}

	@Override
	public ByteSpace put(long position, byte[] bytes, int start, int length) {
		space.put(position, bytes, start, length);
		return this;
	}

	@Override
	public ByteSpace force() throws IOException {
		space.force();
		return this;
	}

	@Override
	public void close() throws IOException {
		space.close();
	}
	
	@Override
	public long capacity() {
		return space.capacity();
	}
	
	protected ByteSpace space() {
		return space();
	}

}
