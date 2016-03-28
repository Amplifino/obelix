package com.amplifino.obelix.guards;

import java.util.function.Supplier;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Guard {
	
	void run(Runnable runnable);
	<T> T supply(Supplier<T> supplier);
	
	static Guard create() {
		return new DefaultGuard();
	}
}
