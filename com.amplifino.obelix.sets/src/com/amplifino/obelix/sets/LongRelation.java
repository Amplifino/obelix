package com.amplifino.obelix.sets;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.LongKeyPair;

/**
 * Implements a relation whose domain is a subset of <code>LongStream.rangeClose(Long.MIN_VALUE, Long.MAX_VALUE)</code>.
 * Rather old fashioned.
 *
 * @param <V> the type of elements of the range
 */

@ProviderType
public interface LongRelation<V> {
	
	/**
	 * return the relation range when restricting the domain to its intersection with the singleton containing the argument
	 * 
	 * @param key the source element
	 * @return a stream representing the range
	 */
	Stream<V> range(long key);
	
	/**
	 * return the relation's domain
	 * 
	 * @return a stream representing the domain
	 */
	LongStream domain();
	
	/**
	 * returns the relations's graph when restricting the domain to its intersection with the argument
	 * 
	 * @param subset the source subset
	 * @return a stream representing the graph
	 * 
	 */
	default Stream<LongKeyPair<V>> graph (LongStream subset ) {
		return subset
			.mapToObj(key -> LongKeyPair.of(key, range(key)))
			.flatMap(pair -> pair.value().map( value -> LongKeyPair.of(pair.key(), value )));
	}

	/**
	 * returns the relation's graph
	 * @return a stream representing the graph
	 */
	default Stream<LongKeyPair<V>> graph () {
		return graph(domain());
	}
	
	/**
	 * returns the relation's range
	 * @return a stream representing the range
	 */	
	default Stream<V> range() {
		return domain().boxed().flatMap(this::range);
	}
}
