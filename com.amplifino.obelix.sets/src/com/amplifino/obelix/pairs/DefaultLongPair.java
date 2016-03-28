package com.amplifino.obelix.pairs;

class DefaultLongPair implements LongPair {
		
	private final long key;
	private final long value;
		
	DefaultLongPair(long key, long value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public final long key() {
		return key;
	}
	
	@Override
	public final long value() {
		return value;
	}
	
	@Override
	public String toString() {
		return "(" + key + "," + value + ")";
	}

	@Override
	public final boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if ( o == null) {
			return false;
		}
		if (o instanceof LongPair) {
			LongPair other = (LongPair) o;
			return key == other.key() && value == other.value();
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return Long.hashCode(key) ^  Long.hashCode(value);
	}
	
}
