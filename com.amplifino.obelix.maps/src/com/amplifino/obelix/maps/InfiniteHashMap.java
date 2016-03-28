package com.amplifino.obelix.maps;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amplifino.obelix.guards.LongGuards;
import com.amplifino.obelix.hash.LongHashByteSpace;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.stores.Store;

public final class InfiniteHashMap<K,V> extends AbstractInfiniteMap<K, V> {
	
	private final LongHashByteSpace index;
	private final Store<Stream<OrderedPair<K,V>>> store;
	private final ToLongFunction<K> hashFunction;
	private final LongGuards guards = LongGuards.create(17); 
	
	public InfiniteHashMap(LongHashByteSpace hashSpace, Store<Stream<OrderedPair<K,V>>> store, ToLongFunction<K> hashFunction) {
		this.index = hashSpace;
		this.store = store;
		this.hashFunction = hashFunction;
	}
	
	protected long hash(K key) {
		return index.normalize(hashFunction.applyAsLong(key));
	}
	
	private OptionalLong getStoreKey(long hash) {
		long bin = index.get(hash);
		return bin == 0 ? OptionalLong.empty() : OptionalLong.of(bin);
	}
	
	private OptionalLong getStoreKey(K key) {
		return getStoreKey(hash(key));
	}
	
	private Stream<OrderedPair<K,V>> getStream(K key) {
		OptionalLong storeKey = getStoreKey(key);
		if (storeKey.isPresent()) {
			return store.get(storeKey.getAsLong());
		} else {
			return Stream.empty();
		}
	}
	
	@Override
	public Optional<V> get(K key) {
		return guards.read(hash(key), hash -> getStream(key)
			.filter(pair -> this.equals(key, pair.key()))
			.map(OrderedPair::value)
			.findFirst());
	}
			
	@Override
	public InfiniteHashMap<K,V> put(K key, V value) {
		Stream<OrderedPair<K,V>> newStream = Stream.of(OrderedPair.of(key,value));
		guards.write(hash(key), hash -> { 
			OptionalLong storeKey = getStoreKey(hash);
			if (storeKey.isPresent()) {
				Stream<OrderedPair<K,V>> stream = store.get(storeKey.getAsLong())
						.filter(entry -> !equals(key, entry.key()));
				store.replace(storeKey.getAsLong(), Stream.concat(stream, newStream), newStoreKey -> index.put(hash, newStoreKey));
			} else {
				long newStoreKey = store.add(newStream);
				index.put(hash, newStoreKey);
			}
		});
		return this;
	}
	
	@Override
	public InfiniteHashMap<K, V> remove(K key) {
		guards.write(hash(key), hash -> {
			getStoreKey(hash).ifPresent( storeKey -> {
				List<OrderedPair<K,V>> list = store.get(storeKey)
						.filter(pair -> !equals(key, pair.key()))
						.collect(Collectors.toList());
				if (list.isEmpty()) {
					store.remove(storeKey);
					index.put(hash, 0);
				} else {
					store.replace(storeKey, list.stream(), newStoreKey -> index.put(hash, newStoreKey));
				}
			});
		});
		return this;
	}
	
	@Override
	public Stream<OrderedPair<K,V>> graph() {
		return store.range().flatMap(Function.identity());
	}

}
