package com.amplifino.obelix.sets;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.pairs.LongPair;

/**
 * Implements a relation whose domain is a subset of <code>LongStream.rangeClose(Long.MIN_VALUE, Long.MAX_VALUE)</code>.
 * Rather old fashioned.
 *
 * @param <V> the type of elements of the range
 */

@ProviderType
public interface LongToLongRelation {
	
	/**
	 * return the relation range when restricting the domain to its intersection with the singleton containing the argument
	 * 
	 * @param key the source element
	 * @return a stream representing the range
	 */
	LongStream range(long key);
	
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
	default Stream<LongPair> graph (LongStream subset ) {
		return subset
			.mapToObj(key -> LongKeyPair.of(key, range(key).boxed()))
			.flatMap(pair -> pair.value().map( value -> LongPair.of(pair.key(), value )));
	}

	/**
	 * returns the relation's graph
	 * @return a stream representing the graph
	 */
	default Stream<LongPair> graph () {
		return graph(domain());
	}
	
	/**
	 * returns the relation's range
	 * @return a stream representing the range
	 */	
	default LongStream range() {
		return domain().flatMap(this::range);
	}
}
