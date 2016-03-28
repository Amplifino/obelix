package com.amplifino.obelix.timeseries;

import java.time.Instant;

import com.amplifino.obelix.pairs.OrderedPair;

public final class TimeSeriesElement<V> implements OrderedPair<Instant,V> {
	
	private final Instant instant;
	private final V value;
	
	TimeSeriesElement(Instant instant, V value) {
		this.instant = instant;
		this.value = value;
	}
	
	public static <V> TimeSeriesElement<V> of(Instant instant, V value) {
		return new TimeSeriesElement<>(instant, value);
	}
	
	public static <V>  TimeSeriesElement<V> of(OrderedPair<Instant,V> pair) {
		return new TimeSeriesElement<>(pair.key(), pair.value());
	}
	
	public OrderedPair<Instant,V> asPair() {
		return OrderedPair.of(instant, value);
	}
	
	public Instant instant() {
		return instant;
	}
	
	@Override
	public Instant key() {
		return instant();
	}
	
	@Override
	public V value() {
		return value;
	}

	@Override
	public <T> OrderedPair<Instant, T> with(T newValue) {
		return TimeSeriesElement.of(instant, newValue);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof OrderedPair)) {
			return false;
		}
		if (this == other) {
			return true;
		}
		OrderedPair<?,?> o = (OrderedPair<?,?>) other;
		return instant.equals(o.key()) && value.equals(o.value());
	}
	
	@Override
	public int hashCode() {
		return instant.hashCode() ^ value.hashCode();
	}
}
