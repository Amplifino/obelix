package com.amplifino.obelix.stores;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.LongValuePair;

@ProviderType
public interface StoreIndex<K> {

	LongStream range(K key);
	StoreIndex<K> put(K key, long value);
	StoreIndex<K> remove(K key);
	
	Stream<LongValuePair<K>> graph();
	
	default Stream<K> domain() {
		return graph().map(LongValuePair::key);
	}
	
	default LongStream range() {
		return graph().mapToLong(LongValuePair::value);
	}
}
