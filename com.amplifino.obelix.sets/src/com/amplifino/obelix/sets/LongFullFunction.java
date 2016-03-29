package com.amplifino.obelix.sets;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.LongKeyPair;

/**
 * Represents a full function whose domain is a subset of <code>LongStream.rangeClose(Long.MIN_VALUE, Long.MAX_VALUE)</code>
 *
 * @param <V> the type of elements of the range
 */
@ProviderType
public interface LongFullFunction<V> extends LongRelation<V> {
	
	/**
	 * returns the element of the singleton range obtained by restricting the domain to the singleton containing the argument
	 * 
	 * @param key the source element
	 * @return the range element
	 */
	V get(long key);
	
	@Override
	default Stream<V> range(long key) {
		return Stream.of(get(key));
	}
	
	@Override
	default Stream<LongKeyPair<V>> graph (LongStream subset ) {
		return subset.mapToObj(LongKeyPair.graph(this::get));

	}
}
