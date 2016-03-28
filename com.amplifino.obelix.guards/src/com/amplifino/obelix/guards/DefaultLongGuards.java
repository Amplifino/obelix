package com.amplifino.obelix.guards;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.stream.IntStream;

class DefaultLongGuards implements LongGuards {

	private final ReadWriteLock[] guards;
	
	DefaultLongGuards(int slots) {
		guards = IntStream.range(0, slots).mapToObj(val -> new ReentrantReadWriteLock()).toArray(ReadWriteLock[]::new);
	}
	
	@Override
	public long write(long value, LongConsumer consumer) {
		Lock guard = guard(value).writeLock();
		guard.lock();
		try {
			consumer.accept(value);
		} finally {
			guard.unlock();
		}
		return value;
	}
	
	@Override
	public <T> T readWrite(long value, LongFunction<T> function) {
		Lock guard = guard(value).writeLock();
		guard.lock();
		try {
			return function.apply(value);
		} finally {
			guard.unlock();
		}
	}
	
	@Override
	public <T> T read(long value, LongFunction<T> function) {
		ReadWriteLock guard = guard(value);
		guard.readLock().lock();
		try {
			return function.apply(value);
		} finally {
			guard.readLock().unlock();
		}
	}

	private ReadWriteLock guard(long value) {
		int index = (int) Long.remainderUnsigned(value, guards.length);
		return guards[index];
	}
}
