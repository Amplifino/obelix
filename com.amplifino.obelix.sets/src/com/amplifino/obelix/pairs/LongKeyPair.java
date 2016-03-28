package com.amplifino.obelix.pairs;

import java.util.function.LongFunction;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Represents an ordered pair whose source elements are longs
 *
 * @param <V> the type of target elements;
 */
@ProviderType
public interface LongKeyPair<V>  {
	/**
	 * returns the pair's first element
	 * @return the first element
	 */
	long key();
	/**
	 * returns the pair's second element
	 * @return the second element
	 */
	V value();
	
	/**
	 * returns an {@code OrderedPair<Long,V>} equivalent to the receiver
	 * @return the boxed pair
	 */
	default OrderedPair<Long,V> boxed() {
		return OrderedPair.of(key(),value());
	}
	
	/**
	 * creates an ordered pair from the argument
	 * @param key the first element
	 * @param value the second element
	 * @param <V> the type of the second element
	 * @return the new pair
	 */
	static <V> LongKeyPair<V> of(long key, V value) {
		return new DefaultLongKeyPair<>(key, value);
	}
	
	static <V> LongFunction<LongKeyPair<V>> graph( LongFunction<V> generator) {
		return key -> LongKeyPair.of(key, generator.apply(key));
	}
	

}
