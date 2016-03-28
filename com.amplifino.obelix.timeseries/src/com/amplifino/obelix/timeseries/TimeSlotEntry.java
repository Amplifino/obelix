package com.amplifino.obelix.timeseries;

import java.time.Instant;

public final class TimeSlotEntry<V> {

	private final int offset;
	private final V value;
	
	public TimeSlotEntry(int offset, V value) {
		this.offset = offset;
		this.value = value;
	}
	
	public V get() {
		return value;
	}
	
	public int offset() {
		return offset;
	}
	
	public Instant instant(Instant instant) {
		return instant.plusMillis(offset);
	}
	
	public TimeSeriesElement<V> toEntry(Instant instant) {
		return TimeSeriesElement.of(instant(instant), get());
	}
}
