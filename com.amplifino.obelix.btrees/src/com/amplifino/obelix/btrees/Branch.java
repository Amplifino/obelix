package com.amplifino.obelix.btrees;

import java.util.stream.Stream;

import com.amplifino.counters.Counters;
import com.amplifino.obelix.pairs.LongValuePair;
import com.amplifino.obelix.pairs.OrderedPair;

interface Branch<K,V> extends Node<K, V> {

	Stream<OrderedPair<K,V>> next(RealNode<K,V> child) throws SplitException;
	void split(SplitAction<K,V> splitAction) throws SplitException;
	void remove(RealNode<K,V> child) throws SplitException;
	long allocateLeaf();
	long allocateBranch();
	ConcurrentBlock<OrderedPair<K,V>> leafBlock(long tag);
	ConcurrentBlock<LongValuePair<K>> branchBlock(long tag);
	RealNode<K,V> node(Branch<K,V> parent, long tag);
	void free(long tag);
	void refresh(long tag);
	Counters<BTreeCounters> counters();
}
