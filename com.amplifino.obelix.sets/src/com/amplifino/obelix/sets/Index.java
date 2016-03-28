package com.amplifino.obelix.sets;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Defines a mutable binary relation.
 *
 * <p>Behavior resembles guava's MultiMap but is not limited to tiny sets </p>
 * 
 * @param <K> the type of elements of the source set
 * @param <V> the type of elements of the target set
 */
@ProviderType
public interface Index<K,V> extends BinaryRelation<K, V> {
	/**
	 * adds the argument pair to the relation if not already an element of the graph, do nothing otherwise
	 * 
	 * <p>Note that not all implementations test for duplicate key value pairs, and may add the pair multiple times</p>
	 * 
	 * @param key source element
	 * @param value target element
	 * @return this relation
	 */
	Index<K,V> put(K key, V value);
	
	/**
	 * removes the argument pair from the graph if present, do nothing otherwise
	 * @param key source element
	 * @param value target element
	 * @return this relation
	 */
	Index<K,V> remove(K key, V value);
	
}
