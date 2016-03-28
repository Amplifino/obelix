package com.amplifino.obelix.timeseries;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.InfiniteMap;

public final class IrregularTimeSeries<V> implements TimeSeries<V> {
	
	private final DefaultTimeSequence<Stream<TimeSlotEntry<V>>> regularSeries;
	
	public IrregularTimeSeries(DefaultTimeSequence<Stream<TimeSlotEntry<V>>> regularSeries) {
		this.regularSeries = regularSeries;
	}

	@Override
	public Optional<V> get(Instant key) {
		return regularSeries.get(key)
			.filter( entry -> entry.instant(key).equals(key))
			.map(TimeSlotEntry::get)
			.findFirst();
	}

	@Override
	public Stream<? extends TimeSeriesElement<V>> graph() {
		return regularSeries.graph()
				.flatMap(OrderedPair.mapPair((instant, stream) -> stream.map( entry -> entry.toEntry(instant))));
	}

	@Override
	public TimeSeries<V> put(Instant instant, V value) {
		Instant cellInstant = regularSeries.normalize(instant);
		int offset = (int) (instant.getEpochSecond() - cellInstant.getEpochSecond());
		Stream.Builder<TimeSlotEntry<V>> builder = Stream.builder();
		builder.add(new TimeSlotEntry<V>(offset,value));
		regularSeries
			.get(instant)
			.filter(entry -> !entry.instant(cellInstant).equals(instant))
			.forEach(builder::add);
		regularSeries.put(cellInstant, builder.build().sorted(Comparator.comparing(TimeSlotEntry::offset)));
		return this;
	}

	@Override
	public InfiniteMap<Instant, V> remove(Instant instant) {
		Instant cellInstant = regularSeries.normalize(instant);
		Stream.Builder<TimeSlotEntry<V>> builder = Stream.builder();
		regularSeries
			.get(instant)
			.filter(entry -> !entry.instant(cellInstant).equals(instant))
			.forEach(builder::add);
		regularSeries.put(cellInstant, builder.build().sorted(Comparator.comparing(TimeSlotEntry::offset)));
		return this;	
	}

	@Override
	public Comparator<Instant> comparator() {
		return Comparator.naturalOrder();
	}

	@Override
	public Stream<? extends TimeSeriesElement<V>> graph(Optional<Instant> start, Optional<Instant> end) {
		Stream<? extends TimeSeriesElement<V>> result = regularSeries.graph(start, end.map(regularSeries::ceil))
			.flatMap(timeSeriesElement -> timeSeriesElement.value().map( entry -> entry.toEntry(timeSeriesElement.instant())));
		return result
			.filter(timeSeriesElement -> startFilter(start).test(timeSeriesElement.instant()))
			.filter(timeSeriesElement -> endFilter(end).test(timeSeriesElement.instant()));
	}
	
	private Predicate<Instant> startFilter(Optional<Instant> start) {
		return candidate -> !start.filter(s -> comparator().compare(candidate, s) < 0).isPresent();
	}
	
	private Predicate<Instant> endFilter(Optional<Instant> end) {
		return candidate -> !end.filter(s -> comparator().compare(candidate, s) >= 0).isPresent();
	}


}
