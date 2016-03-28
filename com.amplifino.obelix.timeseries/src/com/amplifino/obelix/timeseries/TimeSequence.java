package com.amplifino.obelix.timeseries;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.sets.FullFunction;

@ProviderType
public interface TimeSequence<V> extends FullFunction<Instant, V> {
	
	Instant normalize(Instant instant);
	TimeSequence<V> put(Instant instant, V value);
	public Stream<? extends TimeSeriesElement<V>> graph(Optional<Instant> start , Optional<Instant> end);
	default public Stream<? extends TimeSeriesElement<V>> graph() {
		return graph(Optional.empty(), Optional.empty());
	}
	
}
