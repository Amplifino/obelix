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
			throw new IllegalArgumentException();
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
		((ByteBuffer) byteBuffer.slice().position(offset(position, length))).get(bytes, start, length);
		return this;
	}

	@Override
	public ByteSpace put(long position, byte[] bytes, int start, int length) {
		((ByteBuffer) byteBuffer.slice().position(offset(position, length))).put(bytes, start, length);
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
			mappedBuffer.get().force();
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
