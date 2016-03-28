package com.amplifino.obelix.stores;

import org.osgi.annotation.versioning.ProviderType;
import com.amplifino.obelix.sets.FullFunction;

@ProviderType
public interface ObjectStore<K, V> extends FullFunction<K,V> {

	K add(V element);
	
	void remove(K handle);
	
	default K replace(K handle, V newElement) {
		remove(handle);
		return add(newElement);
	}
	
}
