package com.amplifino.obelix.pairs;

import java.util.Arrays;
import java.util.Objects;

class DefaultOrderedPair<K,V> implements OrderedPair<K,V> {
		
	private final K key;
	private final V value;
		
	DefaultOrderedPair(K key, V value) {
		this.key = Objects.requireNonNull(key);
		this.value = Objects.requireNonNull(value);
	}
	
	@Override
	public final K key() {
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
		if (o instanceof OrderedPair) {
			OrderedPair<?,?> other = (OrderedPair<?,?>) o;
			return Objects.deepEquals(key, other.key()) && Objects.deepEquals(value(), other.value());
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return hash(key) ^ hash(value);
	}
		
	static int hash(Object value) {
		if (value == null) {
			return 0;
		}
        if (value instanceof Object[]) {
        	return Arrays.hashCode((Object[]) value);
        } else if (value instanceof byte[]) {
        	return Arrays.hashCode((byte[]) value);	
        } else if (value instanceof short[]) {
            return Arrays.hashCode((short[]) value);
        } else if (value instanceof int[]) {
        	return Arrays.hashCode((int[]) value);
        } else if (value instanceof long[]) {
            return Arrays.hashCode((long[]) value);
        } else if (value instanceof char[]) {
        	return Arrays.hashCode((char[]) value);
        }  else if (value instanceof float[]) {
        	return Arrays.hashCode((float[]) value);
        } else if (value instanceof double[]) {
        	return Arrays.hashCode((double[]) value);
        } else if (value instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) value);
        } else {
        	return value.hashCode();
        }
    }

	@Override
	public <T> OrderedPair<K, T> with(T newValue) {
		return new DefaultOrderedPair<>(key, newValue);
	}

}
