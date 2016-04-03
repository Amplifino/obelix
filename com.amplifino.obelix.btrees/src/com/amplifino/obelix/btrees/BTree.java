package com.amplifino.obelix.btrees;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;
import com.amplifino.obelix.injections.LongValuePairInjection;
import com.amplifino.obelix.pairs.LongValuePair;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.BlockSpace;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.sets.SortedInfiniteMap;

/**
 * 
 * An implementation of a SortedInfiniteMap using a BTree.
 * 
 */
public final class BTree<K,V> implements SortedInfiniteMap<K,V> {
	
	private final RootNode<K,V> root;
	private final Comparator<K> comparator;
	private final ConcurrentBlockSpace space;
	private final Injection<OrderedPair<K, V>, byte[]> leafInjection;
	private final Injection<LongValuePair<K>, byte[]> branchInjection;
	private final Counters<BTreeCounters> counters = Counters.of(BTreeCounters.class);
 	
	private BTree(BlockSpace space, Comparator<K> comparator, Injection<K, byte[]> keyInjection, Injection<OrderedPair<K,V>, byte []> pairInjection) {
		this.space = ConcurrentBlockSpace.on(space);
		this.comparator = comparator;
		leafInjection = pairInjection;
		branchInjection = LongValuePairInjection.of(keyInjection);
		this.root = new RootNode<>(this);
	}
	
	public static <K,V> BTree<K,V> on(BlockSpace space, Comparator<K> comparator, Injection<K, byte[]> keyInjection, Injection<OrderedPair<K,V>, byte[]> pairInjection) {
		return new BTree<>(space, comparator, keyInjection, pairInjection);
	}
	
	@Override
	public Optional<V> get(K key) {
		return root.get(key);
	}

	@Override
	public BTree<K,V> put(K key, V value) {
		root.put(key, value);
		return this;
	}

	@Override
	public BTree<K,V> remove(K key) {
		root.remove(key);
		return this;
	}
	
	@Override
	public Stream<OrderedPair<K,V>> graph() {
		return graph(Optional.empty(), Optional.empty());
	}

	@Override
	public Stream<OrderedPair<K,V>> graph(Optional<K> lowerIncluded, Optional<K> upperExcluded) {
		return root.graph(lowerIncluded, upperExcluded);
	}
	
	@Override
	public Stream<OrderedPair<K,V>> atLeast(K key) {
		return graph(Optional.of(key), Optional.empty());
	}
	
	@Override
	public Stream<OrderedPair<K,V>> graph(K key1, K key2) {
		return graph(Optional.of(key1), Optional.of(key2));
	}
	
	ConcurrentBlockSpace space() {
		return space;
	}
	
	int compare(K key1, K key2) {
		return comparator.compare(key1, key2);
	}
	
	Injection<OrderedPair<K, V>, byte[]> leafInjection() {
		return leafInjection;
	}


	Injection<LongValuePair<K>, byte[]> branchInjection() {
		return branchInjection;
	}

	Counters<BTreeCounters> counters() {
		return counters;
	}

	@Override
	public Counts counts() {
		return counters.counts();
	}

	@Override
	public Comparator<K> comparator() {
		return comparator;
	}
}
