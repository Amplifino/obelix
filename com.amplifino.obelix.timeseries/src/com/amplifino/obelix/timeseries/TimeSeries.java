package com.amplifino.obelix.timeseries;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.SortedInfiniteMap;

@ProviderType
public interface TimeSeries<V> extends SortedInfiniteMap<Instant, V> {
	
	@Override
	TimeSeries<V> put(Instant instant, V value);
	
	@Override
	Stream<? extends TimeSeriesElement<V>> graph();
	
	@Override
	Stream<? extends TimeSeriesElement<V>> graph(Optional<Instant> start, Optional<Instant>  end);
	
	@Override
	default Stream<? extends TimeSeriesElement<V>> graph(Stream<Instant> instants) {
		return instants.map(OrderedPair.graph(this::get))
			.filter(OrderedPair.filterValue(Optional::isPresent))
			.map(OrderedPair.mapPair((instant , optional) -> TimeSeriesElement.of(instant , optional.get())));
	}
	
	@Override
	default Stream<? extends TimeSeriesElement<V>> graph(Instant instant) {
		return this.get(instant)
			.map(value -> TimeSeriesElement.of(instant,value))
			.map(Stream::of)
			.orElse(Stream.empty());
	}
	
	@Override
	default Stream<? extends TimeSeriesElement<V>> graph(Instant lower, Instant upper) {
		return graph(Optional.of(lower), Optional.of(upper));
	}
	
	@Override
	default Stream<? extends TimeSeriesElement<V>> atLeast(Instant lower) {
		return graph(Optional.of(lower), Optional.empty());
	}
	
	@Override
	default Stream<? extends TimeSeriesElement<V>> lessThan(Instant upper) {
		return graph(Optional.empty(), Optional.of(upper));
	}
	
	@Override
	default Stream<? extends TimeSeriesElement<V>> sorted() {
		return graph(Optional.empty(), Optional.empty()); 
	}
	
	@Override
	default Comparator<Instant> comparator() {
		return Comparator.naturalOrder();
	}
	
}
