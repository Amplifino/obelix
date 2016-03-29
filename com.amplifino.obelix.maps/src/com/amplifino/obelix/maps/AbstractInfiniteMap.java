package com.amplifino.obelix.maps;

import java.util.Objects;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;
import com.amplifino.obelix.sets.InfiniteMap;
import com.amplifino.obelix.sets.SetCounters;

public abstract class AbstractInfiniteMap<K,V> implements InfiniteMap<K, V> {
	
	private final Counters<SetCounters> counters = Counters.of(SetCounters.class);
	
	protected AbstractInfiniteMap() {
	}
	
	protected final boolean equals(K key1, K key2) {
		counters.increment(SetCounters.EQUALTESTS);
		return areEqual(key1, key2);
	}
	
	protected boolean areEqual(K key1, K key2) {
		return Objects.deepEquals(key1, key2);
	}
	
	protected final Counters<SetCounters> counters() {
		return counters;
	}
	
	@Override
	public Counts counts() {
		return counters.counts();
	}
	
}
