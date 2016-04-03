package com.amplifino.obelix.sets;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.OrderedPair;

class MapAdapter<K,V> implements InfiniteMap<K,V> {

	private final Map<K,V> map;
	
	private MapAdapter(Map<K,V> map) {
		this.map = map;
	}

	static <K,V> MapAdapter<K,V> wrap(Map<K,V> map) {
		return new MapAdapter<>(map);
	}
	
	@Override
	public Optional<V> get(K key) {
		return Optional.ofNullable(map.get(key));
	}

	@Override
	public Stream<OrderedPair<K, V>> graph() {
		return map.entrySet().stream().map(OrderedPair::of);
	}

	@Override
	public InfiniteMap<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}

	@Override
	public InfiniteMap<K, V> remove(K key) {
		map.remove(key);
		return this;
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}
	
	
}
