package com.amplifino.obelix.btrees;

import java.util.Optional;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.OrderedPair;

interface Node<K,V> {
	
	void put(K key , V value) throws SplitException;
	void remove(K key) throws SplitException;

	int compare(K key1, K key2);
	Optional<V> get(K key) throws SplitException;
	Stream<OrderedPair<K,V>> start(Optional<K> key) throws SplitException;
	Stream<OrderedPair<K,V>> next(Optional<K> key) throws SplitException;
	Optional<K> trySplit(Optional<K> startKey, Optional<K> endKey)  throws SplitException;
} 
