package com.amplifino.obelix.btrees;

class BranchEntry<K,V> {

	private final K key;
	private final RealNode<K,V> node;
	
	BranchEntry(K key, RealNode<K,V> node) {
		this.key = key;
		this.node = node;
	}
	
	K key() {
		return key;
	}
	
	RealNode<K,V> node() {
		return node;
	}
	
	@Override
	public String toString() {
		return "(" +  key + ","  + node + ")";
	}
	
}
