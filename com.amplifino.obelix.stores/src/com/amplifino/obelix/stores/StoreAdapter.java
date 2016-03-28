package com.amplifino.obelix.stores;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.sets.Injection;

public final class StoreAdapter<T,S> implements Store<T> {

	private final Store<S> store;
	private final Injection<T,S> injection;
	
	private StoreAdapter(Store<S> store , Injection<T,S> injection) {
		this.store = store;
		this.injection = injection;
	}

	public static <T,S> StoreAdapter<T,S> wrap (Store<S> store, Injection<T,S> injection ) {
		return new StoreAdapter<>(store, injection);
	}
	
	@Override
	public T get(long key) {
		return injection.unmap(store.get(key));
	}

	@Override
	public LongStream domain() {
		return store.domain();
	}
	
	@Override
	public Stream<T> range() {
		return store.range().map(injection::unmap);
	}
	
	
	@Override
	public Stream<LongKeyPair<T>> graph() {
		return store.graph().map(pair -> LongKeyPair.of(pair.key(), injection.unmap(pair.value())));
	}
	
	@Override
	public Stream<T> range(long key) {
		return store.range().map(injection::unmap);
	}


	@Override
	public long add(T element) {
		return store.add(injection.map(element));
	}

	@Override
	public void remove(long key) {
		store.remove(key);
	}
	
	@Override
	public long replace(long key, T element) {
		return store.replace(key, injection.map(element));
	}
	
	@Override
	public int spareBits() {
		return store.spareBits();
	}
}
	
	
