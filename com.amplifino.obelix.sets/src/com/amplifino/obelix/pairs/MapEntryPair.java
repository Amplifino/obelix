package com.amplifino.obelix.pairs;

import java.util.Map;
import java.util.Objects;

class MapEntryPair<K,V> implements OrderedPair<K,V> {
	
	private final Map.Entry<K,V> entry;
		
	public MapEntryPair(Map.Entry<K,V> entry) {
		this.entry = Objects.requireNonNull(entry);
	}

	@Override
	public K key() {
		return entry.getKey();
	}

	@Override
	public V value() {
		return entry.getValue();
	}

	
	@Override
	public <T> OrderedPair<K, T> with(T newValue) {
		return new DefaultOrderedPair<>(key(), newValue);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) { 
			return true;
		}
		if (o == null) {
			return false;
		}
		if (o instanceof OrderedPair) {
			OrderedPair<?,?> other = (OrderedPair<?,?>) o;
			return Objects.deepEquals(key(), other.key()) && Objects.deepEquals(value(), other.value());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return entry.hashCode();
	}

}
