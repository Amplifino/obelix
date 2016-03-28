package com.amplifino.obelix.btrees;

import com.amplifino.obelix.pairs.LongValuePair;
import com.amplifino.obelix.pairs.OrderedPair;

class SplitAction<K,V> {

	private final RealNode<K, V> existingNode;
	private final OrderedPair<K,V> reason;
	private RealNode<K,V> newNode;
	private LongValuePair<K> entry;
	private final int split;
	private final K splitKey;
	
	SplitAction(RealNode<K,V> existingNode, int split, K splitKey, OrderedPair<K,V> reason)  {
		this.existingNode = existingNode;
		this.split = split;
		this.splitKey = splitKey;
		this.reason = reason;
	}
	
	LongValuePair<K> prepare() throws SplitException {
		this.newNode = existingNode.split(split);
		this.entry = LongValuePair.of(newNode.firstKey(), newNode.tag());
		return entry;
	}
	
	void commit() {
		existingNode.truncate(split);
		existingNode.parent().refresh(existingNode.tag());
	}
	
	void undo() {
		existingNode.parent().refresh(existingNode.tag());
		newNode.parent().free(newNode.tag());
	}
	
	RealNode<K,V> existingNode() {
		return existingNode;
	}
	
	RealNode<K,V> newNode() {
		return newNode;
	}
	
	int split() {
		return split();
	}
	
	K splitKey() {
		return splitKey;
	}
	
	OrderedPair<K,V> reason() {
		return reason;
	}
}
