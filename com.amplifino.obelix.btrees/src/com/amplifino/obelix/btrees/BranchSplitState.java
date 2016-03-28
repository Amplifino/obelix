package com.amplifino.obelix.btrees;

import com.amplifino.counters.Counters;
import com.amplifino.obelix.pairs.LongValuePair;

class BranchSplitState<K> {
	
	private final ConcurrentBlock<LongValuePair<K>> lock;
	private final Counters<BTreeCounters> counters;
	 
	BranchSplitState(ConcurrentBlock<LongValuePair<K>> lock, Counters<BTreeCounters> counters) {
		this.lock = lock;
		this.counters = counters;
	}
	  
	<T> T retryGet(GetWithRetry<T> getter) throws SplitException {
		boolean first = true;
		while(lock.isValid()) {
			try {
				return getter.get();
			} catch(SplitException e) {
				if (first) {
					counters.increment(BTreeCounters.RESTARTS);
					first = false;
				} else {
					counters.increment(BTreeCounters.FAILEDRESTARTS);
					break;
				}
			}
		}
		throw new SplitException();
	}
	
	void retryPut(PutWithRetry putter) throws SplitException {
		boolean first = true;
		while(lock.isValid()) {
			try {
				putter.put();
				return;
			} catch(SplitException e) {
				if (first) {
					counters.increment(BTreeCounters.RESTARTS);
					first = false;
				} else {
					counters.increment(BTreeCounters.FAILEDRESTARTS);
					break;
				}
			}
		}
		throw new SplitException();
	}
	
	interface GetWithRetry<T> {
		T get() throws SplitException;
	}
	
	interface PutWithRetry {
		void put() throws SplitException;
	}
	
}
