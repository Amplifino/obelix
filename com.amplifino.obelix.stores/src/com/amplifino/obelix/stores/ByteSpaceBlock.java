package com.amplifino.obelix.stores;

import java.nio.ByteBuffer;

import com.amplifino.obelix.space.ByteSpace;

public final class ByteSpaceBlock implements Block<byte[]>  {
	
	private final ByteSpace space;
	private int size;

	private ByteSpaceBlock(ByteSpace space) {
		this.space = space;
		this.size = space.getInt(0);
	}
	
	public static ByteSpaceBlock on (ByteSpace space) {
		return new ByteSpaceBlock(space);
	}
	
	@Override
	public int size() {
		return size;
	}
	
	private int checkLength(byte[] element) {
		// fit at least any 2 records in a block
		long maxLength = (space.capacity() - 3* Integer.BYTES) / 2;
		int length = element.length;
		if (length > maxLength) {
			throw new IllegalArgumentException("Length " + length + " exceeds block maximum allowed record length of " + maxLength);
		}
		return length;
	}
	
	@Override
	public boolean canTake(byte[] element) {
		return element.length + Integer.BYTES <= remaining();
	}
	
	private long remaining() {
		return space.capacity() - offset(size()) - Integer.BYTES * size();
	}
	
	private boolean size(int newSize) {
		this.size = newSize;
		space.putInt(0L, newSize);
		return true;
 	}
	
	private boolean added() {
		return size(size() + 1);
	}
	
	private boolean removed() {
		return size(size() - 1);
	}
		
	private long offset(int index) {
		return index == 0 ? Integer.BYTES : space.getInt(space.capacity() - Integer.toUnsignedLong(index * Integer.BYTES));
	}
	
	private byte[] data(int index) {
		long offset = offset(index);
		int length = (int) (offset(index+1) - offset);
		return space.getBytes(offset, length);
	}
		
	@Override
	public byte[] get(int index) {
		if (index < 0 || index >= size()) {
			throw new IllegalArgumentException();
		}
		return data(index);
	}

	@Override
	public boolean set(int index, byte[] element) {
		if (index < 0 || index >= size()) {
			throw new IllegalArgumentException();
		}
		final long start = offset(index);
		final long end = offset(index+1);
		final long last = offset(size);
		final int  length = (int) (offset(index + 1) - offset(index));
		final int delta = checkLength(element) -  length;
		if (delta > 0) {
			final long current = last + size() * Integer.BYTES;
			if (current + delta > space.capacity()) {
				return false;
			}
		}
		if (delta != 0) {
			move(end, last, delta);
			long position = space.capacity() - size() * Integer.BYTES;
			for (int i = size() ; i > index; i--) {
				space.putInt(position,  space.getInt(position) + delta);
				position += Integer.BYTES;
			}			
		}
		space.put(start, element);
		return true;
	}

	@Override
	public boolean add(int index, byte[] element) {
		if (index < 0 || index > size()) {
			throw new IllegalArgumentException();
		}
		if (checkLength(element) + Integer.BYTES > remaining()) {			
			return false;
		}
		long last = offset(size());
		long offset = offset(index);
		if (offset < last) {
			move(offset, last, element.length);
			final int rowDirectoryLength = (size() + 1) * Integer.BYTES; 
			final long position = space.capacity() - rowDirectoryLength;
			final int modifiedRowDirectoryLength = rowDirectoryLength - index * Integer.BYTES;
			ByteBuffer from =  space.get(position + Integer.BYTES, index == 0 ? modifiedRowDirectoryLength - Integer.BYTES : modifiedRowDirectoryLength);
			ByteBuffer to = ByteBuffer.allocate(rowDirectoryLength);
			for (int i = size() ; i >= index; i--) {
				to.putInt(((i == 0) ? Integer.BYTES : from.getInt()) + element.length); 			
			}
			space.put(position, (ByteBuffer) to.flip());
		} else {
			space.putInt(space.capacity() - (index +1 ) * Integer.BYTES, (int) (offset + element.length));
		}
		space.put(offset, element);
		return added();
	}
	
	
	private void move(long start, long end, int offset) {
		space.put(start + offset, space.getBytes(start,  Math.toIntExact(end - start)));
	}
	
	@Override
	public void remove(int index) {
		if (index < 0 || index >= size()) {
			throw new IllegalArgumentException();
		}
		long last = offset(size());
		long offset = offset(index);
		int length = (int) (offset(index+1) - offset);
		if (offset < last) {
			move(offset + length, last, -length);
			long position = space.capacity() - (index + 1) * Integer.BYTES;
			for (int i = index ; i < size() ; i++) {
				space.putInt(position, space.getInt(position - Integer.BYTES) - length);
				position -= Integer.BYTES;
			}
		}
		removed();
	}
	
	@Override
	public void truncate(int end) {
		size(end);
	}
	
}
