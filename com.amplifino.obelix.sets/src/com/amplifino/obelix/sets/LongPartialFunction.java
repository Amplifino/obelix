package com.amplifino.obelix.sets;

import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

/**
 * represents a partial function whose source set is a subset of longs
 *
 * @param <V> the type of elements of the target set
 */
@ProviderType
public interface LongPartialFunction<V> extends LongRelation<V>{

	/**
	 * returns an Optional containing the element of the singleton range obtained by restricting the domain 
	 * to its intersection with the singleton containing the argument. 
	 * If the intersection is empty, return an empty Optional
	 * 
	 * @param key the source element
	 * @return the optional
	 */
	Optional<V> get(long key);
	
	@Override
	default Stream<V> range(long key) {
		return get(key).map(Stream::of).orElse(Stream.empty());
	}
}
