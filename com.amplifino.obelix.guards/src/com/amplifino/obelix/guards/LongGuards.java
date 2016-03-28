package com.amplifino.obelix.guards;

import java.util.function.LongConsumer;
import java.util.function.LongFunction;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface LongGuards {

	long write(long value, LongConsumer consumer);
	<T> T readWrite(long value, LongFunction<T> function);
	<T> T read(long value, LongFunction<T> function);	
	
	static LongGuards create(int guards) {
		return new DefaultLongGuards(guards);
	}
}
