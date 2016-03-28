package com.amplifino.obelix.stores;

import java.util.Optional;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Block<T> {

	int size();
	default boolean isEmpty() {
		return size() == 0;
	}
	default boolean add(T e) {
		return add(size(),e);
	}
	T get(int index);
	boolean set(int index, T element);
	boolean add(int index, T element);
	public void remove(int index);
	public void truncate(int index);
	public boolean canTake(T element);
	
	default Optional<T> first() {
		return isEmpty() ? Optional.empty() : Optional.of(get(0)); 
	}
	
	default Optional<T> last() {
		return isEmpty() ? Optional.empty() : Optional.of(get(size() - 1));
	}
	
}
