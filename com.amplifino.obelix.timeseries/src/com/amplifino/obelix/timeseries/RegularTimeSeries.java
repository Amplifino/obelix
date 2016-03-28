package com.amplifino.obelix.timeseries;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import com.amplifino.obelix.sets.InfiniteMap;

public final class RegularTimeSeries<V> implements TimeSeries<V> {
	
	private final DefaultTimeSequence<Optional<V>> sequence;
	
	public RegularTimeSeries(DefaultTimeSequence<Optional<V>> sequence) {
		this.sequence= sequence;
	}

	@Override
	public Optional<V> get(Instant instant) {
		return sequence.get(instant);
	}

	@Override
	public Stream<TimeSeriesElement<V>> graph(Instant key) {
		return get(key).map(value -> Stream.of(TimeSeriesElement.of(key, value))).orElse(Stream.empty());
	}

	@Override
	public Stream<TimeSeriesElement<V>> graph() {
		return sequence.graph()
				.filter(entry -> entry.value().isPresent())
				.map(entry -> TimeSeriesElement.of(entry.instant(), entry.value().get()));
	}

	@Override	
	public TimeSeries<V> put(Instant instant, V value) {
		sequence.put(instant, Optional.of(value));
		return this;
	}

	@Override
	public InfiniteMap<Instant, V> remove(Instant instant) {
		sequence.put(instant, Optional.empty());
		return this;
	}

	@Override
	public Stream<TimeSeriesElement<V>> graph(Optional<Instant> start, Optional<Instant> end) {
		return sequence.graph(start, end)
				.filter(entry -> entry.value().isPresent())
				.map(entry -> TimeSeriesElement.of(entry.instant(), entry.value().get()));
	}
}

