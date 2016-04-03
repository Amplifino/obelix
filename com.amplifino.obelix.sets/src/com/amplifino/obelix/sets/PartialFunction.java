package com.amplifino.obelix.sets;

import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.OrderedPair;
/**
 * Represents a partial function. As such some elements of the source set do not participate in the relation 
 *
 * @param <K> the type of elements of the source set
 * @param <V> the type of elements of the target set
 */
@ProviderType
public interface PartialFunction<K,V> extends BinaryRelation<K, V> {
	
	/**
	 * returns an Optional containing the element of the singleton range obtained by restricting the domain 
	 * to its intersection with the singleton containing the argument. 
	 * If the intersection is empty, returns an empty Optional
	 * 
	 * @param key the source element
	 * @return the optional
	 */
	Optional<V> get(K key);
	
	@Override
	default Stream<? extends OrderedPair<K, V>> graph(K key) {
		return get(key)
			.map(OrderedPair.withKey(key))
			.map(Stream::of)
			.orElse(Stream.empty());
	}
}
