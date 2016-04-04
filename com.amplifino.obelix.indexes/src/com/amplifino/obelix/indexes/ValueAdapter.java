package com.amplifino.obelix.indexes;

import java.util.Comparator;
import java.util.function.Supplier;

interface ValueAdapter<V> extends Supplier<V> , Comparable<ValueAdapter<V>> {

	static <V> ValueAdapter<V> of(V value, Comparator<? super V> comparator) {
		return new DefaultValueAdapter<>(value, comparator);
	}
	
	@SuppressWarnings("unchecked")
	static <V> ValueAdapter<V> minimum() {
		return Limits.MIN_VALUE;
	}
	
	@SuppressWarnings("unchecked")
	static <V> ValueAdapter<V> maximum() {
		return Limits.MAX_VALUE;
	}
	
	class DefaultValueAdapter<V> implements ValueAdapter<V> {
		private V value;
		private Comparator<? super V> comparator;
	
		private DefaultValueAdapter(V value, Comparator<? super V> comparator) {
			this.value = value;
			this.comparator = comparator;
		}	

		@Override
		public V get() {
			return value;
		}

		@Override
		public int compareTo(ValueAdapter<V> o) {
			if (o instanceof Limits) {
				return -o.compareTo(this);
			} else {
				return comparator.compare(get(), o.get());
			}
		}
	}
		
	@SuppressWarnings("rawtypes")
	class Limits implements ValueAdapter {
		private static Limits MIN_VALUE = new Limits(-1);
		private static Limits MAX_VALUE = new Limits(1);
		private final int compareResult;
		
		private Limits(int compareResult) {
			this.compareResult = compareResult;
		}
		
		@Override
		public Object get() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int compareTo(Object o) {
			return this == o ? 0 : compareResult;
		}
	}
}
