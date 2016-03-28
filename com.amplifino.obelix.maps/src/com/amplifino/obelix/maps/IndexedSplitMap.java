package com.amplifino.obelix.maps;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.InfiniteMap;
import com.amplifino.obelix.sets.SetCounters;
import com.amplifino.obelix.stores.Store;

public final class IndexedSplitMap<K,V> extends AbstractInfiniteMap<K, V> {
	private final InfiniteMap<K, Long> index;
	private final Store<V> store;
	
	protected IndexedSplitMap(InfiniteMap<K, Long> index, Store<V> store) {
		this.index = index;
		this.store = store;
	}
	
	public static <K,V> IndexedSplitMap<K,V> of (InfiniteMap<K, Long> index, Store<V> store) {
		return new IndexedSplitMap<>(index, store);
	}
	
	private Optional<OrderedPair<K, Long>> getIndexEntry(K key) {
		Objects.requireNonNull(key);
		return index.get(key).map(storeKey -> OrderedPair.of(key, storeKey));
	}
		
	@Override
	public Optional<V> get(K key) {
		Objects.requireNonNull(key);
		counters().increment(SetCounters.GETS);
		return index.get(key).map(store::get);
	}
	
	@Override
	public IndexedSplitMap<K,V> put(K key, V value) {
		Objects.requireNonNull(value);
		Optional<OrderedPair<K, Long>> indexEntry = getIndexEntry(key);
		indexEntry.ifPresent( pair -> {
			counters().increment(SetCounters.UPDATES);
			store.replace(pair.value(), value, storeKey -> {
				counters().increment(SetCounters.MIGRATES);
				index.put(pair.key(), storeKey);
			});
		});
		if (!indexEntry.isPresent()) {
			counters().increment(SetCounters.INSERTS);
			index.put(key, store.add(value));
		}
		return this;
	}
	
	@Override
	public IndexedSplitMap<K, V> remove(K key) {
		getIndexEntry(key).ifPresent( entry -> {
			counters().increment(SetCounters.REMOVALS);
			store.remove(entry.value());
			index.remove(key);
		});
		return this;
	}
	
	@Override
	public Stream<K> domain() {
		return index.domain();
	}
	@Override
	public Stream<V> range() {
		return store.range();
	}
	
	@Override
	public Stream<OrderedPair<K,V>> graph() {
		return index.graph().map(OrderedPair.mapValue(store::get));
	}
	
}
