package com.amplifino.obelix.space;

import java.io.IOException;
import java.nio.ByteBuffer;

abstract class WrappedSpace implements ByteSpace {

	private final ByteSpace space;

	WrappedSpace(ByteSpace space) {
		this.space = space;
	}
	
	abstract long translate(long position, int length); 
	
	@Override
	public ByteSpace get(long position, byte[] bytes, int start, int length) {
		space.get(translate(position, length), bytes, start, length);
		return this;
	}
	
	@Override
	public ByteSpace get(long position, ByteBuffer buffer) {
		space.get(translate(position, buffer.remaining()), buffer);
		return this;
	}
	
	@Override
	public ByteBuffer get(long position, int length) {
		return space.get(translate(position, length), length);
	}
	
	@Override
	public byte[] getBytes(long position, int length) {
		return space.getBytes(translate(position, length), length);
	}

	@Override
	public ByteSpace put(long position, byte[] bytes, int start, int length) {
		space.put(translate(position, length), bytes, start, length);
		return this;
	}
	
	@Override 
	public ByteSpace put(long position, ByteBuffer buffer) {
		space.put(translate(position, buffer.remaining()), buffer);
		return this;
	}

	@Override
	public byte get(long position) {
		return space.get(translate(position, Byte.BYTES));
	}
	
	@Override
	public short getShort(long position) {
		return space.getShort(translate(position, Short.BYTES));
	}
	
	@Override
	public char getChar(long position) {
		return space.getChar(translate(position, Character.BYTES));
	}
	
	@Override
	public int getInt(long position) {
		return space.getInt(translate(position, Integer.BYTES));
	}
	
	@Override
	public float getFloat(long position) {
		return space.getFloat(translate(position, Float.BYTES));
	}
	
	@Override
	public long getLong(long position) {
		return space.getLong(translate(position, Long.BYTES));
	}
	
	@Override
	public double getDouble(long position) {
		return space.getDouble(translate(position, Double.BYTES));
	}
	
	@Override
	public ByteSpace put(long position, byte in) {
		space.put(translate(position, Byte.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putShort(long position, short in) {
		space.putShort(translate(position, Short.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putChar(long position, char in) {
		space.putChar(translate(position, Character.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putInt(long position, int in) {
		space.putInt(translate(position, Integer.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putFloat(long position, float in) {
		space.putFloat(translate(position, Float.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putLong(long position, long in) {
		space.putLong(translate(position, Long.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putDouble(long position, double in) {
		space.putDouble(translate(position, Double.BYTES), in);
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
	
	@Override 
	public ByteSpace slice(long shift, long capacity) {
		return space().slice(translate(shift,0), capacity);
	}

}
