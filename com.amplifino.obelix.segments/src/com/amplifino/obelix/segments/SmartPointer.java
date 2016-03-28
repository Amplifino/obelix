package com.amplifino.obelix.segments;
 
public final class SmartPointer {
	
	static final int BYTES = Long.BYTES;
	
	private final long value;
	
	private SmartPointer(long value) {
		this.value = value;
	}
	
	public static SmartPointer of(long value) {
		return new SmartPointer(value);
	}
	
	public static SmartPointer wrap(long value) {
		return of(value << 1);
	}
	
	private static SmartPointer of(long pointer, int flags) {
		return of((pointer << 1) | flags);
	}
	
	public long get() {
		return value >>> 1;
	}
	
	public long value() {
		return value;
	}
	
	private long flags() {
		return value & 1;
	}
	
	public boolean isValid() {
		return flags() == 0 & !isZero();
	}
	
	public boolean isMarked() {
		return flags() == 1;
	}
	
	public SmartPointer valid() {
		return SmartPointer.of(get(), 0);
	}
	
	public SmartPointer marked() {
		return SmartPointer.of(get(), 1);
	}
	
	public boolean isZero() {
		return value == 0;
	}
} 
