package com.amplifino.obelix.indexes;

import java.util.Comparator;
import java.util.Optional;

import com.amplifino.obelix.injections.PairInjection;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.SegmentTypes;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.sets.SortedIndex;
import com.amplifino.obelix.sortedmaps.SortedMapBuilder;
import com.amplifino.obelix.sortedmaps.SortedMapTypes;
import com.amplifino.obelix.space.ByteSpace;

class SortedIndexBuilderImpl<K,V> implements SortedIndexBuilder<K, V> {
	
	private final SortedMapBuilder<OrderedPair<K, ValueAdapter<V>>,Boolean> mapBuilder;
	private Optional<Injection<K, byte[]>> keyInjection = Optional.empty();
	private Optional<Injection<V, byte[]>> valueInjection= Optional.empty();
	private Optional<Injection<OrderedPair<K,V>, byte[]>> pairInjection = Optional.empty();
	@SuppressWarnings({ "unchecked", })
	private Comparator<? super K> keyComparator = (Comparator<K>) Comparator.naturalOrder();
	@SuppressWarnings({ "unchecked" })
	private Comparator<? super V> valueComparator = (Comparator<V>) Comparator.naturalOrder();
	
	SortedIndexBuilderImpl(ByteSpace space) {
		this.mapBuilder = SortedMapBuilder.on(space);
	}

	@Override
	public SortedIndex<K, V> build() {
		finishBuilder();
		return new IndexOnSortedMap<>(mapBuilder.build(), valueComparator);
	}
	
	@Override
	public SortedIndex<K, V> build(SortedMapTypes type) {
		finishBuilder();
		return new IndexOnSortedMap<>(mapBuilder.build(type), valueComparator);
	}

	private void finishBuilder() {
		mapBuilder.keyInjection(mapKeyInjection(pairInjection()));
		mapBuilder.valueInjection(noInjection());	
		mapBuilder.comparator(mapComparator());
	}
	
	@Override
	public SortedIndexBuilder<K, V> keyInjection(Injection<K, byte[]> keyInjection) {
		this.keyInjection = Optional.of(keyInjection);
		return this;
	}

	@Override
	public SortedIndexBuilder<K, V> valueInjection(Injection<V, byte[]> valueInjection) {
		this.valueInjection = Optional.of(valueInjection);
		return this;
	}

	@Override
	public SortedIndexBuilder<K, V> pairInjection(Injection<OrderedPair<K,V>, byte[]> pairInjection) {
		this.pairInjection = Optional.of(pairInjection);
		return this;
	}

	@Override
	public SortedIndexBuilder<K, V> segmentType(SegmentTypes segmentType) {
		mapBuilder.segmentType(segmentType);
		return this;
	}
	
	@Override
	public SortedIndexBuilder<K,V> split(long split) {
		mapBuilder.split(split);
		return this;
	}

	@Override
	public SortedIndexBuilder<K,V> keyComparator(Comparator<? super K> comparator) {
		this.keyComparator = comparator;
		return this;
	}
	
	@Override
	public SortedIndexBuilder<K,V> valueComparator(Comparator<? super V> comparator) {
		this.valueComparator = comparator;
		return this;
	}
	
	@Override
	public SortedIndexBuilder<K,V> blockSize(long size) {
		mapBuilder.blockSize(size);
		return this;
	}
	
	private Injection<OrderedPair<K,V>, byte[]> pairInjection() {
		return pairInjection.orElseGet(() -> PairInjection.of(keyInjection.get(), valueInjection.get()));
	}
	 
	private static Injection<Boolean, byte[]> noInjection() {
		return Injection.<Boolean, byte[]>mapper(b -> new byte[0]).unmapper(b -> Boolean.TRUE);
	}

	private Injection<OrderedPair<K,ValueAdapter<V>>,byte[]> mapKeyInjection(Injection<OrderedPair<K,V>,byte[]> pairInjection) {
		
		return new Injection<OrderedPair<K,ValueAdapter<V>>,byte[]>() {

			@Override
			public byte[] map(OrderedPair<K, ValueAdapter<V>> in) {
				return pairInjection.map(in.with(in.value().get()));
			}

			@Override
			public OrderedPair<K, ValueAdapter<V>> unmap(byte[] in) {
				OrderedPair<K,V> pair = pairInjection.unmap(in);
				return OrderedPair.of(pair.key(), ValueAdapter.of(pair.value(), valueComparator));
			}			
		};
	}
	
	private Comparator<OrderedPair<K, ValueAdapter<V>>> mapComparator() {
		return Comparator
				.<OrderedPair<K,ValueAdapter<V>>, K>comparing(OrderedPair::key, keyComparator)
				.thenComparing(OrderedPair::value);
	}
	
	
}
