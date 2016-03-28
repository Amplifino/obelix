package com.amplifino.obelix.btrees;

import java.util.concurrent.atomic.AtomicReference;


abstract class AbstractNode<K,V> implements RealNode<K,V> {

	private final AtomicReference<Branch<K,V>> parent;

	AbstractNode(Branch<K,V> parent) {
		this.parent = new AtomicReference<>(parent);
	}
	
	public Branch<K,V> parent() {
		return parent.get();
	}
	
	public AbstractNode<K,V> parent(Branch<K,V> parent) {
		this.parent.set(parent);
		return this;
	}
	
	@Override
	public int compare(K key1, K key2) {
		return parent().compare(key1, key2);
	}	
	
}
