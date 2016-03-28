package com.amplifino.obelix.segments;

class Header {
	
	static final int LONGS = 1;
	static final int BYTES = LONGS * Long.BYTES;
	
	private final long value;
	
	private Header(long value) {
		this.value = value;
	}
	
	private Header(long slotSize, int length, int flags ) {
		this (((long)length << 32) + ((slotSize << 3) | flags));
	}
	
	static Header of(long value) {
		return new Header(value);
	}
	
	static Header of(long slotSize, int length) {
		return new Header(slotSize, length, 1);
	}
	
	static Header marked(long slotSize, int length) {
		return new Header(slotSize, length, 3);
	}
	
	long value() {
		return value;
	}
	
	long slotSize() {
		return (value &  0xFFFFFFFFL) >> 3;
	}
	
	int length() {
		return (int) (value >> 32);
	}
	
	int flags() {
		return (int) (value & 7);
	}

	Header length(int length) {
		long slotSize = slotSize() << 3;
		if (length > (slotSize - 1)) {
			throw new IllegalArgumentException("No space for " + length + " bytes");
		}
		return new Header(slotSize >>> 3, length, flags());
	}
	
	boolean isValid() {
		return flags() == 1;			
	}
	
	boolean isFree() {
		return flags() == 2;
	}
	
	boolean isMarked() {
		return flags() == 3;
	}
	
	private long base() {
		return value & ~7;
	}
	
	Header free() {
		return new Header(base() | 2); 
	}
	
	Header valid() {
		return new Header(base() | 1); 
	}
	
	Header marked() {
		return new Header(base() | 3); 
	}
	
	boolean canTake(long size) {
		return size + Header.BYTES <=  (slotSize() << 3);
	}
	
	@Override
	public String toString() {
		return "slotSize: " + slotSize() + " length: " + length() + " flags: " + flags();
	}
	
}
