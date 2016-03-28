package com.amplifino.obelix.guards;

import java.util.function.Supplier;

class DefaultGuard implements Guard {

	@Override
	public synchronized void run(Runnable runnable) {
		runnable.run();
	}

	@Override
	public synchronized <T> T supply(Supplier<T> supplier) {
		return supplier.get();
	}

}
