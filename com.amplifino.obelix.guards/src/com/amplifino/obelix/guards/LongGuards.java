package com.amplifino.obelix.guards;

import java.util.function.LongConsumer;
import java.util.function.LongFunction;

import org.osgi.annotation.versioning.ProviderType;

/**
 * LongGuards protect a long value against simultaneous access
 * To limit the number of resources needed, this is typically implemented
 * by a fixed number of locks, and taking the remainder of the division
 * of the protected long value and the fixed number of locks as an index to the lock array
 * 
 *
 */
@ProviderType
public interface LongGuards {

	/**
	 * runs the consumer while holding the writelock on value
	 * @param value 
	 * @param consumer
	 * @return value
	 */
	long write(long value, LongConsumer consumer);
	/**
	 * applies the function to value while holding the write lock on value
	 * @param value
	 * @param function
	 * @return the function result
	 */
	<T> T readWrite(long value, LongFunction<T> function);
	/**
	 * applies the function to value while holding the read lock on value
	 * @param value
	 * @param function
	 * @return the function result
	 */
	<T> T read(long value, LongFunction<T> function);	
	
	/**
	 * creates a new LongGuards
	 * @param guards number of guards
	 * @return the new LongGuards
	 */
	static LongGuards create(int guards) {
		return new DefaultLongGuards(guards);
	}
}
