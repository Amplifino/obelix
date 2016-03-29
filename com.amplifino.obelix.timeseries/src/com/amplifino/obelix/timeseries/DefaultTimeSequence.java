package com.amplifino.obelix.timeseries;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.amplifino.obelix.sequences.Sequence;

public class DefaultTimeSequence<V> implements TimeSequence<V> {
	
	private final Sequence<V> sequence;
	private final TimeUnit timeUnit;
	private final int cellDuration;
	
	
	public DefaultTimeSequence(Sequence<V> sequence , TimeUnit timeUnit, int cellDuration) {
		this.sequence = sequence;
		this.timeUnit = timeUnit;
		this.cellDuration = cellDuration;
	}

	private long sequenceKey(Instant instant) {
		return timeUnit.convert(instant.getEpochSecond(), TimeUnit.SECONDS) / cellDuration;
	}
	
	private Instant timeSeriesKey(long key) {
		return Instant.ofEpochSecond(TimeUnit.SECONDS.convert(key, timeUnit) * cellDuration);
	}
	
	@Override
	public Instant normalize(Instant instant) {
		return timeSeriesKey(sequenceKey(instant));
	}
	
	public Instant ceil(Instant instant) {
		return timeSeriesKey(sequenceKey(instant) + 1);
	}
	
	@Override
	public V get(Instant instant) {
		return sequence.get(sequenceKey(instant));
	}
	
	@Override
	public DefaultTimeSequence<V> put(Instant instant, V value) {
		sequence.put(sequenceKey(instant), value);
		return this;
	}

	@Override
	public Stream<? extends TimeSeriesElement<V>> graph() {
		return sequence.graph().map(pair -> TimeSeriesElement.of(timeSeriesKey(pair.key()), pair.value()));
	}
	
	@Override
	public Stream<? extends TimeSeriesElement<V>> graph(Optional<Instant> start , Optional<Instant> end) {
		return sequence.graph(sequenceDomain(start,end))
				.map(pair -> TimeSeriesElement.of(timeSeriesKey(pair.key()), pair.value()));
	}
	
	private LongStream sequenceDomain (Optional<Instant> start , Optional<Instant> end) {
		long longStart = start.map(this::sequenceKey).orElse(Long.MIN_VALUE);
		return end
			.map(instant -> LongStream.range(longStart,this.sequenceKey(instant)))
			.orElseGet(() -> LongStream.rangeClosed(longStart, Long.MAX_VALUE));
		
	}

}
