package com.amplifino.obelix.maps;

import java.util.Objects;
import java.util.Optional;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import com.amplifino.obelix.guards.LongGuards;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.stores.Store;

public final class HashIndexedMap<K,V> extends AbstractInfiniteMap<K, V> {
	
	private final SmartSynonymHashCluster index;
	private final Store<OrderedPair<K,V>> entryStore;
	private final ToLongFunction<? super K> hashFunction;
	private final LongGuards guards = LongGuards.create(17);
	
	public HashIndexedMap(SmartSynonymHashCluster index, Store<OrderedPair<K,V>> entryStore, ToLongFunction<? super K> hashFunction) {
		this.index = index;
		this.entryStore = entryStore;
		this.hashFunction = hashFunction;
	}
	
	protected long hash(K key) {
		return index.normalize(hashFunction.applyAsLong(key));
	}
	
	protected long hashFromEntryKey(Long entryKey) {
		return hash(entryStore.get(entryKey).key());
	}
	
	private Optional<Long> getEntryKey(K key, long hash) {
		Objects.requireNonNull(key);
		return index.range(hash)
			.filter(value -> this.equals(key, entryStore.get(value).key()))
			.findAny();
	}
	
	@Override
	public Optional<V> get(K key) {
		return guards.read(hash(key), hash -> getEntryKey(key, hash)
			.map(entryStore::get)
			.map(OrderedPair::value));
	}
			
	@Override
	public HashIndexedMap<K,V> put(K key, V value) {
		guards.write(hash(key), hash -> {
			OrderedPair<K , V> pair = OrderedPair.of(key,value);
			Optional<Long> entryKeyHolder = getEntryKey(key, hash);
			if (entryKeyHolder.isPresent()) {
				entryStore.replace(entryKeyHolder.get(), pair, (newKeyIndex) -> {
					index.remove(hash, entryKeyHolder.get());
					index.put(hash, newKeyIndex);
				});
			} else {
				long entryKey = entryStore.add(pair);
				index.put(hash, entryKey);
			}
		});
		return this;
	}
	
	@Override
	public HashIndexedMap<K, V> remove(K key) {
		guards.write(hash(key), hash -> {
			getEntryKey(key, hash).ifPresent( entryKey -> {
				entryStore.remove(entryKey);
				index.remove(hash, entryKey);
			});
		});
		return this;
	}
	
	@Override
	public Stream<OrderedPair<K,V>> graph() {
		return entryStore.range();
	}

}
