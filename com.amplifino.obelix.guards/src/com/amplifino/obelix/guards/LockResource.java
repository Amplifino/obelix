package com.amplifino.obelix.guards;

import java.util.concurrent.locks.Lock;

/**
 * LockResource is a wrapper classes around Lock,
 * for using locks in a try with resources.
 *
 */
public final class LockResource implements AutoCloseable {
	private final Lock lock;
	
	private LockResource(Lock lock) {
		this.lock = lock;
	}
	
	/**
	 * Locks the argument.
	 * @param lock
	 * @return
	 */
	public static LockResource lock(Lock lock) {
		lock.lock();
		return new LockResource(lock);
	}

	/**
	 * releases the lock
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		lock.unlock();
	}
}

