package com.amplifino.obelix.hash;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.sets.LongFullFunction;

@ProviderType
public interface HashSpace<T> extends LongFullFunction<T> {
	
	long capacity();
	
	@Override
	default LongStream domain() {
		return LongStream.range(0, capacity());
	}
	
	@Override
	default Stream<LongKeyPair<T>> graph() {
		return domain().mapToObj(LongKeyPair.graph(this::get));
	}
	
	default Stream<T> range() {
		return domain().mapToObj(this::get);
	}
	
	HashSpace<T> put(long key, T value);	
	
}
