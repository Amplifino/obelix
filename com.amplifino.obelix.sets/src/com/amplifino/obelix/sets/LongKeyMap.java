package com.amplifino.obelix.sets;

import java.util.function.LongFunction;

import org.osgi.annotation.versioning.ProviderType;

/**
 * represents a mutable LongPartialFunction
 * 
 * @param V the type of elements of the range
 */
@ProviderType
public interface LongKeyMap<V> extends LongPartialFunction<V> {
	
	/**
	 * Adds the argument pair to the function if not already present.
	 * Do nothing otherwise
	 * 
	 * @param key source element
	 * @param value target element
	 * @return this function
	 */
	LongKeyMap<V> put(long key, V value);
	
	/**
	 * removes the pair with the argument as source element from the graph if present, do nothing otherwise 
	 * @param key source element
	 * @return this function
	 */
	LongKeyMap<V> remove(long key);
	
	/**
     * If the specified key is not already associated with a value, 
     * attempts to compute its value using the given mapping
     * function and enters it into this map
     *
     * @param key key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with
     *         the specified key
     *
     */
	default V computeIfAbsent(long key, LongFunction<V> mappingFunction) {
		return get(key).orElseGet( () -> {
			V value = mappingFunction.apply(key);
			put(key,value);
			return value;
		});
	}
	
}
