package com.amplifino.obelix.guards;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

import org.junit.Test;

public class ConcurrencyTest {

	private static final long limit = 500_000_000L;
	private Lock lock = new ReentrantLock();
	private Object monitor = new Object();
	private volatile long val; 

	@Test
	public void test() {
		time("Lock free", this::uncontended);
		time("Atomic", this::atomic);
		time("Uncontended lock ", this::uncontendedLock);
		time("Synchronized ", this::sync);
		time("Volatile ", this::vola);
	}
	
	long uncontended() {
		long count = 0;
		for (long i = 0 ; i < limit ; i++) {
			count++;
		}
		return count;
	}
	
	long vola() {
		for (long i = 0 ; i < limit ; i++) {
			val++;
		}
		return val;
	}
	
	long atomic() {
		AtomicLong count = new AtomicLong();
		for (long i = 0 ; i < limit ; i++) {
			count.incrementAndGet();
		}
		return count.get();
	}
	
	long uncontendedLock() {
		long count = 0;
		for (long i = 0 ; i < limit ; i++) {
			lock.lock();	
			try {
				count++;
			} finally {
				lock.unlock();
			}
		}
		return count;
	}
	
	long sync() {
		long count = 0;
		for (long i = 0 ; i < limit ; i++) {
			synchronized (monitor)  {
				count++;
			}
		}
		return count;
	}
	
	private void time(String header, LongSupplier runable) {
		long now = System.nanoTime();
		runable.getAsLong();
		long elapsed = System.nanoTime() - now;
		System.out.println(header + ": " + (elapsed / 1_000_000) + " ms");
	}
	
}
