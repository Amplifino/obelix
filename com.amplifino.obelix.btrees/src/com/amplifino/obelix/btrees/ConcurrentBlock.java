package com.amplifino.obelix.btrees;

import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.stores.Block;
import com.amplifino.obelix.stores.ObjectBlock;

class ConcurrentBlock<T> {

	private final Block<T> block;
	private final BlockLock lock;
	
	private ConcurrentBlock(Block<T> block, BlockLock lock ) {
		this.block = block;
		this.lock = lock;
	}
	
	static <T> ConcurrentBlock<T> on(Injection<T, byte[]> injection, BlockLock lock) {
		return new ConcurrentBlock<>(ObjectBlock.on(injection, lock.block()), lock);
	}
		
 	<R> R get(Function<Block<T>, R> function) throws SplitException {
 		Lock readLock = lock.readLock();
		try {
			return function.apply(block);
		} finally {
			readLock.unlock();
		}
	}
	
	ConcurrentBlock<T> put(SplitConsumer<T> consumer) throws SplitException {
		Lock writeLock = lock.writeLock();
		try {
			consumer.accept(block);
		} finally {
			writeLock.unlock();
		}		
		return this;
	}
	
	void invalidate() {
		lock.invalidate();
	}
	
	void truncate(int split) {
		block.truncate(split);
	}
	
	boolean isValid() {
		return lock.isValid();
	}
	
	@FunctionalInterface
	interface SplitConsumer<T> {
		void accept(Block<T> block) throws SplitException;
	}
 
}
