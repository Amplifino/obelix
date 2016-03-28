package com.amplifino.obelix.stores;

import java.util.function.LongConsumer;
import java.util.function.Predicate;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.sets.LongFullFunction;

@ProviderType
public interface Store<T> extends LongFullFunction<T>  {

	long add(T element);
	void remove(long key);
	int spareBits();
	
	default long replace(long key, T newElement) {
		remove(key);
		return add(newElement);
	}
	
	default void replace(long key, T newElement, LongConsumer consumer) {
		long newKey = replace(key, newElement);
		if (newKey != key) {
			consumer.accept(newKey);
		}
	}
	
	default long upsert(T newElement, Predicate<T> matcher) {
		return graph()
			.filter(pair -> matcher.test(pair.value()))
			.findFirst()
			.map(pair -> this.replace(pair.key(), newElement))
			.orElseGet(() -> this.add(newElement));
	}
	
}
