package com.amplifino.obelix.maps;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.RecordSpace;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.sets.SetCounters;
import com.amplifino.obelix.stores.Store;
import com.amplifino.obelix.stores.StoreAdapter;

public final class LinearMap<K,V> extends AbstractInfiniteMap<K,V> {
	
	private final Store<OrderedPair<K, V>> store;
	
	private LinearMap(RecordSpace recordSpace, Injection<OrderedPair<K,V>, byte[]> injection) {
		this.store = StoreAdapter.wrap(recordSpace, injection);
	}
	
	static public <K,V> LinearMap<K, V> on (RecordSpace space, Injection<OrderedPair<K,V>, byte[]> injection) {
		return new LinearMap<>(space, injection);
	}
	
	private OrderedPair<K,V> pair(K key, V value) {
		return OrderedPair.of(key,value); 
	}
	
	private OptionalLong getStoreKey(K key) {
		Objects.requireNonNull(key);
		return store.graph()
			.filter(pair -> this.equals(key, pair.value().key()))
			.mapToLong(LongKeyPair::key)
			.findAny();
	}
	
	@Override
	public Optional<V> get(K key) {
		Objects.requireNonNull(key);
		counters().increment(SetCounters.GETS);
		return store.range()
			.filter( pair -> this.equals(key, pair.key()))
			.map(OrderedPair::value)
			.findAny();
	}

	@Override
	public LinearMap<K,V> put(K key, V value) {
		OptionalLong storeKey = getStoreKey(key);
		OrderedPair<K, V> pair = pair(key, value);
		if (storeKey.isPresent()) {
			counters().increment(SetCounters.UPDATES);
			store.replace(storeKey.getAsLong(), pair, ignored -> counters().increment(SetCounters.MIGRATES));
		} else {
			counters().increment(SetCounters.INSERTS);
			store.add(pair);
		}
		return this;
	}

	@Override
	public LinearMap<K,V> remove(K key) {
		getStoreKey(key).ifPresent(storeKey -> {
			counters().increment(SetCounters.REMOVALS);
			store.remove(storeKey);
		});
		return this;
	}

	@Override
	public Stream<K> domain() {
		return graph().map(OrderedPair::key);
	}

	@Override
	public Stream<V> range() {
		return graph().map(OrderedPair::value);
	}

	@Override
	public Stream<OrderedPair<K,V>> graph() {
		return store.range();
	}
	

}
