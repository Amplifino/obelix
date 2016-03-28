package com.amplifino.obelix.pairs;

import java.util.Objects;

class DefaultLongKeyPair<V> implements LongKeyPair<V> {
		
	private final long key;
	private final V value;
		
	DefaultLongKeyPair(long key, V value) {
		this.key = key;
		this.value = Objects.requireNonNull(value);
	}
	
	@Override
	public final long key() {
		return key;
	}
	
	@Override
	public V value() {
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
		if (o instanceof LongKeyPair) {
			LongKeyPair<?> other = (LongKeyPair<?>) o;
			return key == other.key() && Objects.deepEquals(value(), other.value());
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return Long.hashCode(key) ^  DefaultOrderedPair.hash(value);
	}
	
}
