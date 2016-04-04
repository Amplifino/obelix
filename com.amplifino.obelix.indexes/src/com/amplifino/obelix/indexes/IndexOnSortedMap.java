package com.amplifino.obelix.indexes;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.Index;
import com.amplifino.obelix.sets.SortedIndex;
import com.amplifino.obelix.sets.SortedInfiniteMap;

class IndexOnSortedMap<K,V> implements SortedIndex<K, V> {

	private final SortedInfiniteMap<OrderedPair<K,ValueAdapter<V>>,Boolean> map;
	private final Comparator<? super V> valueComparator;
	
	IndexOnSortedMap(SortedInfiniteMap<OrderedPair<K,ValueAdapter<V>>, Boolean> map, Comparator<? super V> valueComparator) {
		this.map = map;
		this.valueComparator = valueComparator;
	}

	@Override
	public Index<K, V> put(K key, V value) {
		map.put(OrderedPair.of(key, ValueAdapter.of(value, valueComparator)), true);
		return this;
	}

	@Override
	public Index<K, V> remove(K key, V value) {
		map.remove(OrderedPair.of(key, ValueAdapter.of(value, valueComparator)));
		return this;
	}

	@Override
	public Stream<OrderedPair<K,V>> graph(K key) {
		return map.graph(Optional.of(OrderedPair.of(key,ValueAdapter.minimum())), Optional.of(OrderedPair.of(key, ValueAdapter.maximum())))
				.map(OrderedPair::key)
				.map(OrderedPair.mapValue(ValueAdapter::get));
	}

	@Override
	public Stream<OrderedPair<K, V>> graph() {
		return map.domain().map(OrderedPair.mapValue(ValueAdapter::get));
	}

	@Override
	public Stream<OrderedPair<K, V>> graph(Optional<K> lowerIncluded, Optional<K> upperExcluded) {
		return map.graph(lowerIncluded.map(this::minimum), upperExcluded.map(this::maximum))
				.map(OrderedPair::key)
				.map(OrderedPair.mapValue(ValueAdapter::get));
	}

	private OrderedPair<K, ValueAdapter<V>> minimum(K key) {
		return OrderedPair.of(key , ValueAdapter.minimum());
	}
	
	private OrderedPair<K, ValueAdapter<V>> maximum(K key) {
		return OrderedPair.of(key , ValueAdapter.maximum());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Comparator<K> comparator() {
		return (Comparator<K>) Comparator.naturalOrder();
	}
}
