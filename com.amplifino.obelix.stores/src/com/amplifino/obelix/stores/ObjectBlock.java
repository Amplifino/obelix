package com.amplifino.obelix.stores;

import java.util.StringJoiner;
import java.util.stream.IntStream;

import com.amplifino.obelix.sets.Injection;

public final class ObjectBlock<T> implements Block<T> {
	private final Injection<T,byte[]> injection;
	private final Block<byte[]> block;
	
	private ObjectBlock(Injection<T, byte[]> injection , Block<byte[]> block) {
		this.injection = injection;
		this.block = block;
	}
	
	public static <T> ObjectBlock<T> on (Injection<T, byte[]> injection , Block<byte[]> block) {
		return new ObjectBlock<>(injection, block);
	}

	@Override
	public int size() {
		return block.size();
	}

	@Override
	public T get(int index) {
		return injection.unmap(block.get(index));
	}

	@Override
	public boolean set(int index, T element) {
		return block.set(index, injection.map(element));
	}

	@Override
	public boolean add(int index, T element) {
		return block.add(index, injection.map(element));
	}

	@Override
	public void remove(int index) {
		block.remove(index);
	}
	
	@Override
	public void truncate(int end) {
		block.truncate(end);
	}
	
	@Override 
	public String toString() {
		return IntStream.range(0, size())
			.mapToObj(this::get)
			.map(Object::toString)
			.collect(() -> new StringJoiner("{","}",",") , StringJoiner::add, StringJoiner::merge)
			.toString();
	}
	
	@Override
	public boolean canTake(T element) {
		return block.canTake(injection.map(element));
	}

}
