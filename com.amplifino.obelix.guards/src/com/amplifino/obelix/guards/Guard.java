package com.amplifino.obelix.guards;

import java.util.function.Supplier;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Guard provides mutual exclusion protection
 *
 */
@ProviderType
public interface Guard {
	
	/**
	 * runs the argument while holding the lock
	 * @param runnable
	 */
	void run(Runnable runnable);
	/**
	 * execute the argument while holding the lock
	 * @param supplier
	 * @return supplier.get()
	 */
	<T> T supply(Supplier<T> supplier);
	
	/**
	 * creates a new Guard
	 * @return the new Guard
	 */
	static Guard create() {
		return new DefaultGuard();
	}
}
