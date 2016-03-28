package com.amplifino.obelix.btrees;

interface RealNode<K,V> extends Node<K,V> {

	Branch<K,V> parent();
	RealNode<K,V> parent(Branch<K,V> parent);
	RealNode<K,V> split(int split) throws SplitException;
	K firstKey() throws SplitException;
	long tag();
	void truncate(int split);
}
