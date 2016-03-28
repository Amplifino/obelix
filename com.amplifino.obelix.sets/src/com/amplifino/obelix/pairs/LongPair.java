package com.amplifino.obelix.pairs;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Represents an ordered pair whose source and target elements are longs
 *
 */
@ProviderType
public interface LongPair  {
	/**
	 * returns the pair's first element
	 * @return the first element
	 */
	long key();
	/**
	 * returns the pair's second element
	 * @return the second element
	 */
	long value();
	
	/**
	 * returns an {@code OrderedPair<Long,V>} equivalent to the receiver
	 * @return the boxed pair
	 */
	default OrderedPair<Long,Long> boxed() {
		return OrderedPair.of(key(),value());
	}
	
	/**
	 * creates an ordered pair from the argument
	 * @param key the first element
	 * @param value the second element
	 * @return the new pair
	 */
	static LongPair of(long key, long value) {
		return new DefaultLongPair(key, value);
	}

}
