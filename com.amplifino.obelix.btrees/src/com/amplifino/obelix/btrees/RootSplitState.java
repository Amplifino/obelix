package com.amplifino.obelix.btrees;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.amplifino.counters.Counters;

class RootSplitState<K,V> {
	
	private Optional<RealNode<K,V>> root;
	private final ConcurrentBlockSpace space;
	private final Counters<BTreeCounters> counters;
	private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
	

	RootSplitState(Optional<RealNode<K,V>> root, ConcurrentBlockSpace space, Counters<BTreeCounters> counters) {
		this.root = root;
		this.space = space;
		this.counters = counters;
	}

	Optional<RealNode<K,V>> root() {
		Lock readLock = lock.readLock();
		readLock.lock();
		try {
			return root;
		} finally {
			readLock.unlock();
		}
	}
	
	void newRoot(Optional<RealNode<K,V>> oldRoot , NewRootProvider<K,V> operator) throws SplitException {
		Lock writeLock = lock.writeLock();
		writeLock.lock();
		try {
			if (this.root.equals(oldRoot)) {
				this.root = operator.apply(this.root);
				space.putUserHeader(this.root.map(RealNode::tag).orElse(0L));
			} else {
				throw new SplitException();
			}
		} finally {
			writeLock.unlock();
		}
	}
	 
	<T> Optional<T> retryGet(GetWithRetry<K,V,T> getter)  {
		int i = 0;
		for(Optional<RealNode<K,V>> currentRoot = root(); currentRoot.isPresent(); currentRoot = root()) {
			try { 
				return Optional.ofNullable(getter.get(currentRoot.get()));
			} catch(SplitException e) {
				i = check(i);
			}
		}
		return Optional.empty();
	}
	
	void retryPut(PutWithRetry<K,V> putter)  {
		int i = 0;
		for(Optional<RealNode<K,V>> currentRoot = root();;currentRoot = root()) {
			try {
				putter.put(currentRoot);
				return;
			} catch(SplitException e) {
				i = check(i);
			}
		}
	}
	
	private int check(int count) {
		counters.increment(BTreeCounters.RESTARTS).max(BTreeCounters.MAXRESTARTS, count + 1);		
		return count + 1;
	}

	interface GetWithRetry<K,V,T> {
		T get(RealNode<K,V> root) throws SplitException;
	}
	
	interface PutWithRetry<K,V> {
		void put(Optional<RealNode<K,V>> root) throws SplitException;
	}
	
	interface NewRootProvider<K,V> {
		Optional<RealNode<K,V>> apply(Optional<RealNode<K,V>> oldRoot) throws SplitException;
	}
	
}
