package com.amplifino.obelix.guards;

import java.util.concurrent.locks.Lock;

public final class LockResource implements AutoCloseable {
	private final Lock lock;
	
	private LockResource(Lock lock) {
		this.lock = lock;
	}
	
	public static LockResource lock(Lock lock) {
		lock.lock();
		return new LockResource(lock);
	}

	@Override
	public void close() {
		lock.unlock();
	}
}

