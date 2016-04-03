package com.amplifino.obelix.sets;

import java.util.Map;
import java.util.function.Function;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;

/**
 * Implements a mutable partial function 
 * 
 * Behavior resembles java.util.Map, but is not limited to tiny maps.
 * The interface is infinite in its api, implementations and hardware restrictions may impose limits
 * In general InfiniteMap do not support null values as keys or values
 * For performance reasons put and remove do not return the old element,
 * but instead return the receiver allowing method chaining.
 * Also InfiniteMaps do not define equality other than <code>Object.equals()</code>
 * 
 * @param <K> the type of elements of the source set
 * @param <V> the type of elements of the target set
 */
@ProviderType
public interface InfiniteMap<K, V> extends PartialFunction<K,V> {

	/**
	 * adds a pair to the function. If the function is already defined for the key, 
	 * replace the value with the new value
	 * @param key source element
	 * @param value target element
	 * @return this map
	 */
	InfiniteMap<K, V> put(K key, V value);
	
	/**
	 * removes the pair with the argument as source element from the graph if present, do nothing otherwise
	 * @param key source element of the pair to remove
	 * @return this map
	 */
	InfiniteMap<K,V> remove(K key);
	
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
	default V computeIfAbsent(K key, Function<? super K,? extends V> mappingFunction) {
		return get(key).orElseGet( () -> {
			V value = mappingFunction.apply(key);
			put(key,value);
			return value;
		});
	}
	
	/**
	 *  get statistics
	 *  
	 *  <p> Implementations may provide statistics, preferably using the <code>SetCounters</code> class,
	 *  or a logical superset. By default no statistics are gathered</p>
	 *  
	 *  @return the statistics counts
	 */
	default Counts counts() {
		return Counters.empty(SetCounters.class).counts();
	}
	
	/**
	 * wraps the receiver with a java.util.Map interface
	 * 
	 * <p> the receiver should only be modified through the wrapper after calling this method. 
	 * For large maps this method may be slow as it has to obtain the size of the map </p>
	 * 
	 * @return the map wrapper
	 */
	default Map<K,V> asTinyMap() {
		return new TinyMapAdapter<>(this);
	}
	
	/**
	 * wraps the argument with an InfiniteMap implementation
	 * @param map to wrap
	 * @param <K> type of elements of the source set
	 * @param <V> type of elements of the target set
	 * @return the wrapped map
	 */
	static <K,V> InfiniteMap<K,V>  wrap(Map<K,V> map) {
		return MapAdapter.wrap(map);
	}
		
}
