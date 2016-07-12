package com.amplifino.obelix.space;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Optional;

/**
 * ByteSpace backed by ByteBuffer
 * 
 * This class wraps a byte buffer into a ByteSpace interface
 *
 */
public final class ByteBufferSpace implements ByteSpace {
	
	private final ByteBuffer byteBuffer;
	
	private ByteBufferSpace(ByteBuffer byteBuffer) {
		this.byteBuffer = (ByteBuffer) byteBuffer.rewind();
	}
	
	/**
	 * returns a ByteSpace wrapping the argument
	 * 
	 * @param byteBuffer the buffer to wrap
	 * @return the ByteSpace wrapping the argument
	 */
	static ByteBufferSpace of (ByteBuffer byteBuffer) {
		return new ByteBufferSpace(byteBuffer);
	}

	private int offset(long position , int length) {
		if (position < 0 || position + length > capacity()) {
			throw new IllegalArgumentException("" + position + "-" + length);
		} 
		return (int) position;
	}

	private Optional<MappedByteBuffer> mappedBuffer() {
		return Optional.of(byteBuffer)
				.filter(MappedByteBuffer.class::isInstance)
				.map(MappedByteBuffer.class::cast);
	}
	
	@Override
	public ByteSpace get(long position, byte[] bytes, int start, int length) {
		((ByteBuffer) byteBuffer.duplicate().position(offset(position, length))).get(bytes, start, length);
		return this;
	}
	
	@Override
	public ByteSpace get(long position, ByteBuffer buffer) {
		buffer.put(get(position, buffer.remaining()));
		return this;
	}

	@Override
	public ByteBuffer get(long position, int length) {
		int offset = offset(position , length);
		//return ByteBuffer.wrap(getBytes(position, length));
		return ((ByteBuffer) (byteBuffer.asReadOnlyBuffer().position(offset).limit(offset + length))).slice();
	}
	
	@Override
	public byte[] getBytes(long position, int length) {
		if (length < 0) {
			throw new IllegalArgumentException();
		}
		byte[] result = new byte[length];
		get(position, result);
		return result;
	}
	
	@Override
	public ByteSpace put(long position, byte[] bytes, int start, int length) {
		((ByteBuffer) byteBuffer.duplicate().position(offset(position, length))).put(bytes, start, length);
		return this;
	}
	
	@Override
	public ByteSpace put(long position, ByteBuffer buffer) {
		((ByteBuffer) (byteBuffer.duplicate().position(offset(position, buffer.remaining())))).put(buffer);
		return this;
	}
	
	@Override
	public byte get(long position) {
		return byteBuffer.get(offset(position, Byte.BYTES));
	}
	
	@Override
	public short getShort(long position) {
		return byteBuffer.getShort(offset(position, Short.BYTES));
	}

	@Override
	public char getChar(long position) {
		return byteBuffer.getChar(offset(position, Character.BYTES));
	}
	
	@Override
	public int getInt(long position) {
		return byteBuffer.getInt(offset(position, Integer.BYTES));
	}
	
	@Override
	public float getFloat(long position) {
		return byteBuffer.getFloat(offset(position, Float.BYTES));
	}
	
	@Override
	public long getLong(long position) {
		return byteBuffer.getLong(offset(position, Long.BYTES));
	}
	
	@Override
	public double getDouble(long position) {
		return byteBuffer.getDouble(offset(position, Double.BYTES));
	}
	
	@Override
	public ByteSpace put(long position, byte in) {
		byteBuffer.put(offset(position, Byte.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putShort(long position, short in) {
		byteBuffer.putShort(offset(position, Short.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putChar(long position, char in) {
		byteBuffer.putChar(offset(position, Character.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putInt(long position, int in) {
		byteBuffer.putInt(offset(position, Integer.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putFloat(long position, float in) {
		byteBuffer.putFloat(offset(position, Float.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putLong(long position, long in) {
		byteBuffer.putLong(offset(position, Long.BYTES), in);
		return this;
	}
	
	@Override
	public ByteSpace putDouble(long position, double in) {
		byteBuffer.putDouble(offset(position, Double.BYTES), in);
		return this;
	}
	
	/**
	 * 	 {@inheritDoc}
	 * 
	 * <p>If the backing ByteBuffer is a MappedByteBuffer, call force on the MappedByteBuffer.
	 * Otherwise do nothing</p>
	 */
	@Override
	public ByteSpace force() throws IOException {
		Optional<MappedByteBuffer> mappedBuffer = mappedBuffer();
		if (mappedBuffer.isPresent()) {
			try {
				mappedBuffer.get().force();
			} catch (UnsupportedOperationException e) {
				// ignore, the mapped buffer was a direct buffer, not a memory maped file buffer
			}
		}
		return this;
	}
	
	/**
	 * 	 {@inheritDoc}
	 * 
	 * <p>If the backing ByteBuffer is a MappedByteBuffer, call force on the MappedByteBuffer.
	 * Otherwise do nothing</p>
	 */

	@Override
	public void close() throws IOException {
		force();
	}

	@Override
	public long capacity() {
		return byteBuffer.capacity();
	}
	
}
