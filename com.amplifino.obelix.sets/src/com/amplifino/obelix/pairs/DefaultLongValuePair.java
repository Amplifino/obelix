package com.amplifino.obelix.pairs;

import java.util.Objects;

class DefaultLongValuePair<K> implements LongValuePair<K> {
		
	private final K key;
	private final long value;
		
	DefaultLongValuePair(K key, long value) {
		this.key = Objects.requireNonNull(key);
		this.value = value;
	}
	
	@Override
	public final K key() {
		return key;
	}
	
	@Override
	public long value() {
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
		if (o instanceof LongValuePair) {
			LongValuePair<?> other = (LongValuePair<?>) o;
			return Objects.deepEquals(key, other.key()) && value == other.value();
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return DefaultOrderedPair.hash(key) ^ Long.hashCode(value);
	}

	@Override
	public LongValuePair<K> with(long newValue) {
		return new DefaultLongValuePair<>(key, newValue);
	}

	@Override
	public OrderedPair<K, Long> boxed() {
		return OrderedPair.of(key, value);
	}
		
}
