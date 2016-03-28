package com.amplifino.obelix.sets;

import java.util.stream.Stream;
import org.osgi.annotation.versioning.ProviderType;
import com.amplifino.obelix.pairs.OrderedPair;

/**
 * Represents a full function. 
 * 
 * <p> In a full function the source set and the domain are identical.
 * The difference with partial function is rather subjective,
 * as one can always treat a partial function as a full function by restricting its source set to the domain
 * </p>
 *
 * @param <K> the type of elements of the source set
 * @param <V> the type of elements of the target set
 */
@ProviderType
public interface FullFunction<K,V> extends BinaryRelation<K, V> {
	
	/**
	 * return the element of the singleton range obtained by restricting the domain to the singleton containing the argument
	 * @param key the domain singleton element
	 * @return the range singleton element
	 * @throws java.util.NoSuchElementException
	 * 		if the argument is not an element of the domain
	 */
	V get(K key);
	
	@Override
	default Stream<? extends OrderedPair<K,V>> graph(K key) {
		return Stream.of(OrderedPair.of(key, get(key)));
	}
}
