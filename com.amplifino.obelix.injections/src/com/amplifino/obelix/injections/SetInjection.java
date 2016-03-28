package com.amplifino.obelix.injections;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.amplifino.obelix.sets.Injection;

public final class SetInjection<T> implements Injection<Set<T>, byte[] > {

	private final Injection<Stream<T>,byte[]> injection;
	
	private SetInjection(Injection<T,byte[]> injection) {
		this.injection = StreamInjection.of(injection);
	}
	
	public static <T> SetInjection<T> of(Injection<T,byte[]> injection) {
		return new SetInjection<>(injection);
	}
	
	@Override
	public byte[] map(Set<T> list) {
		return injection.map(list.stream());
	}

	@Override
	public Set<T> unmap(byte[] in) {
		return injection.unmap(in).collect(HashSet::new, HashSet::add, HashSet::addAll);
 	}


}
