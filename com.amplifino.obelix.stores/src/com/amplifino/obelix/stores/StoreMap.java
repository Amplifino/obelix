package com.amplifino.obelix.stores;

import java.util.OptionalLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.LongValuePair;

@ProviderType
public interface StoreMap<K> {
	
	OptionalLong get(K key);
	StoreMap<K> put(K key, long value);
	StoreMap<K> remove(K key);
	
	Stream<LongValuePair<K>> graph();
	
	default Stream<K> domain() {
		return graph().map(LongValuePair::key);
	}
	
	default LongStream range() {
		return graph().mapToLong(LongValuePair::value);
	}
	
	default LongStream range(K key) {
		OptionalLong value = get(key);
		return value.isPresent() ? LongStream.of(value.getAsLong()) : LongStream.empty();
	}
}
