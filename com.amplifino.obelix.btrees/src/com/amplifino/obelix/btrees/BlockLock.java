package com.amplifino.obelix.btrees;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.amplifino.obelix.stores.Block;
import com.amplifino.obelix.stores.ByteSpaceBlock;

class BlockLock {
	
	private final Block<byte[]> block;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	private AtomicBoolean valid = new AtomicBoolean(true);
	
	private BlockLock(Block<byte[]> block) {
		this.block = block;
	}
	
	public static <T> BlockLock on(ByteSpaceBlock block) {
		return new BlockLock(block);
	}
	
	Block<byte[]> block() {
		return block;
	}
	
	Lock readLock() throws SplitException {
		Lock readLock = lock.readLock();
		readLock.lock();
		if (valid.get()) {
			return readLock;
		} else {
			readLock.unlock();
			throw new SplitException();
		}
	}
	
	Lock writeLock() throws SplitException {
		Lock writeLock = lock.writeLock();
		writeLock.lock();
		if (valid.get()) {
			return writeLock;
		} else {
			writeLock.unlock();
			throw new SplitException();
		}
	}
	
	boolean isValid() {
		return valid.get();
	}
	
	void invalidate() {
		if (!lock.isWriteLockedByCurrentThread()) {
			throw new IllegalStateException();
		}
		valid.set(false);
	}
	
	void truncate(int n) {
		if (valid.get()) {
			throw new IllegalStateException();
		}
		if (!lock.isWriteLockedByCurrentThread()) {
			throw new IllegalStateException();
		}
		block.truncate(n);
	}

 	 
}
