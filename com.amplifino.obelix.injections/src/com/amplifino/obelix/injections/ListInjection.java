package com.amplifino.obelix.injections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.amplifino.obelix.sets.Injection;

public final class ListInjection<T> implements Injection<List<T>, byte[] > {

	private final Injection<Stream<T>,byte[]> injection;
	
	private ListInjection(Injection<T,byte[]> injection) {
		this.injection = StreamInjection.of(injection);
	}
	
	public static <T> ListInjection<T> of(Injection<T,byte[]> injection) {
		return new ListInjection<>(injection);
	}
	
	@Override
	public byte[] map(List<T> list) {
		return injection.map(list.stream());
	}

	@Override
	public List<T> unmap(byte[] in) {
		return injection.unmap(in).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
 	}


}
