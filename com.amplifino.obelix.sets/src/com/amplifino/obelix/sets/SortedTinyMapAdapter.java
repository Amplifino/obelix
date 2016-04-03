package com.amplifino.obelix.sets;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.OrderedPair;

class SortedTinyMapAdapter<K,V> extends TinyMapAdapter<K, V> implements SortedMap<K, V> {

	private final SortedInfiniteMap<K, V> map;
	
	SortedTinyMapAdapter(SortedInfiniteMap<K, V> map) {
		super(map);
		this.map = map;
	}

	@Override
	public Comparator<? super K> comparator() {
		return map.comparator();
	}

	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return new SortedTinyMapAdapter<>(new Filter<>(map, Optional.of(fromKey), Optional.of(toKey)));
	}

	@Override
	public SortedMap<K, V> headMap(K toKey) {
		return new SortedTinyMapAdapter<>(new Filter<>(map, Optional.empty(), Optional.of(toKey)));
	}

	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		return new SortedTinyMapAdapter<>(new Filter<>(map, Optional.of(fromKey) , Optional.empty()));
	}

	@Override
	public K firstKey() {
		return map.domain().findFirst().orElse(null);
	}

	@Override
	public K lastKey() {
		return map.domain().reduce((previous, current) -> current).orElse(null);
	}

	private static class Filter<K,V> implements SortedInfiniteMap<K, V> {
		
		private final SortedInfiniteMap<K, V> map;
		private final Predicate<K> filter;
		
		Filter(SortedInfiniteMap<K,V> map, Optional<K> from , Optional<K> to ) {
			this.map = map;
			Predicate<K> fromFilter = k -> from.map( lowerLimit -> map.comparator().compare(k, lowerLimit) >= 0).orElse(true);
			Predicate<K> toFilter = k -> to.map(upperLimit -> map.comparator().compare(k, upperLimit) < 0).orElse(true);
			this.filter = fromFilter.and(toFilter);
		}
		
		@Override
		public InfiniteMap<K, V> put(K key, V value) {
			if (filter.test(key)) {
				map.put(key, value);
			} else {
				throw new IllegalArgumentException();
			}
			return this;
		}

		@Override
		public InfiniteMap<K, V> remove(K key) {
			if (filter.test(key)) {
				map.remove(key);
			} else {
				throw new IllegalArgumentException();
			}
			return this;
		}

		@Override
		public Optional<V> get(K key) {
			return Optional.of(key).filter(filter).flatMap(map::get);
		}

		@Override
		public Stream<? extends OrderedPair<K, V>> graph() {
			return map.graph().filter(pair -> filter.test(pair.key()));
		}

		@Override
		public Stream<? extends OrderedPair<K, V>> graph(Optional<K> lowerIncluded, Optional<K> upperExcluded) {
			return map.graph(lowerIncluded, upperExcluded).filter(pair -> filter.test(pair.key()));
		}

		@Override
		public Comparator<? super K> comparator() {
			return map.comparator();
		}
	}	
}
