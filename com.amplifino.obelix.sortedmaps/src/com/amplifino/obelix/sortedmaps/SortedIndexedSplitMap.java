package com.amplifino.obelix.sortedmaps;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.amplifino.counters.Counts;
import com.amplifino.obelix.maps.AbstractInfiniteMap;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.SetCounters;
import com.amplifino.obelix.sets.SortedInfiniteMap;
import com.amplifino.obelix.stores.Store;

public final class SortedIndexedSplitMap<K,V> extends AbstractInfiniteMap<K, V> implements SortedInfiniteMap<K, V> {
	private final SortedInfiniteMap<K, Long> index;
	private final Store<V> store;
	
	protected SortedIndexedSplitMap(SortedInfiniteMap<K, Long> index, Store<V> store) {
		this.index = index;
		this.store = store;
	}
	
	public static <K,V> SortedIndexedSplitMap<K,V> of (SortedInfiniteMap<K, Long> index, Store<V> store) {
		return new SortedIndexedSplitMap<>(index, store);
	}
	
	private Optional<OrderedPair<K, Long>> getIndexEntry(K key) {
		Objects.requireNonNull(key);
		return index.get(key).map(OrderedPair.withKey(key));
	}
		
	@Override
	public Optional<V> get(K key) {
		Objects.requireNonNull(key);
		counters().increment(SetCounters.GETS);
		return index.get(key).map(store::get);
	}
	
	@Override
	public SortedIndexedSplitMap<K,V> put(K key, V value) {
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
	public SortedIndexedSplitMap<K, V> remove(K key) {
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

	@Override
	public Stream<OrderedPair<K, V>> graph(Optional<K> lowerIncluded, Optional<K> upperExcluded) {
		return index.graph(lowerIncluded, upperExcluded).map(OrderedPair.mapValue(store::get));
	}

	@Override
	public Comparator<K> comparator() {
		return index.comparator();
	}
	
	@Override
	public Counts counts() {
		return index.counts();
	}
}
