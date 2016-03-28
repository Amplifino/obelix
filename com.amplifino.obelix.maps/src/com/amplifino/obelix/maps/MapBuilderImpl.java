package com.amplifino.obelix.maps;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.LongUnaryOperator;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import com.amplifino.obelix.hash.LongHashByteSpace;
import com.amplifino.obelix.injections.LongValuePairInjection;
import com.amplifino.obelix.injections.PairInjection;
import com.amplifino.obelix.injections.StreamInjection;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.RecordSpace;
import com.amplifino.obelix.segments.RecordSpaceBuilder;
import com.amplifino.obelix.segments.Segment;
import com.amplifino.obelix.segments.SegmentTypes;
import com.amplifino.obelix.sets.InfiniteMap;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.stores.Store;
import com.amplifino.obelix.stores.StoreAdapter;

class MapBuilderImpl<K,V> implements MapBuilder<K, V>, MapTypes.Visitor<InfiniteMap<K,V>> {
	
	private final ByteSpace space;
	private final short currentTag;
	private Optional<Injection<K, byte[]>> keyInjection = Optional.empty();
	private Optional<Injection<V, byte[]>> valueInjection= Optional.empty();
	private Optional<Injection<OrderedPair<K,V>, byte[]>> pairInjection = Optional.empty();
	private SegmentTypes segmentType = SegmentTypes.FREELIST;
	private OptionalLong split = OptionalLong.empty();
	private OptionalLong hashSize = OptionalLong.empty();
	private ToLongFunction<? super K> hashFunction = k -> Objects.hashCode(k) & 0x00000000FFFFFFFFL;
	
	MapBuilderImpl(ByteSpace space) {
		this.space = space;
		this.currentTag = Segment.ownerTag(space);
	}

	@Override
	public InfiniteMap<K, V> build() {
		if (currentTag <= 0) {
			throw new IllegalStateException();
		}
		return build(MapTypes.tag(currentTag));
	}

	@Override
	public InfiniteMap<K, V> build(MapTypes type) {
		return type.visit(this);
	}

	@Override
	public MapBuilder<K, V> keyInjection(Injection<K, byte[]> keyInjection) {
		this.keyInjection = Optional.of(keyInjection);
		return this;
	}

	@Override
	public MapBuilder<K, V> valueInjection(Injection<V, byte[]> valueInjection) {
		this.valueInjection = Optional.of(valueInjection);
		return this;
	}

	@Override
	public MapBuilder<K, V> pairInjection(Injection<OrderedPair<K,V>, byte[]> pairInjection) {
		this.pairInjection = Optional.of(pairInjection);
		return this;
	}

	@Override
	public MapBuilder<K, V> segmentType(SegmentTypes segmentType) {
		this.segmentType = Objects.requireNonNull(segmentType);
		return this;
	}
	
	@Override
	public MapBuilder<K,V> split(long split) {
		if (split < 256) {
			throw new IllegalArgumentException();
		}
		this.split = OptionalLong.of(split);
		return this;
	}
	
	@Override
	public MapBuilder<K,V> hashSize(long size) {
		if (size < 1) {
			throw new IllegalArgumentException();
		}
		this.hashSize = OptionalLong.of(size);
		return this;
	}
	
	@Override
	public MapBuilder<K,V> hashFunction(ToLongFunction<? super K> hashFunction) {
		this.hashFunction = Objects.requireNonNull(hashFunction);
		return this;
	}
	
	private Injection<OrderedPair<K,V>, byte[]> pairInjection() {
		return pairInjection.orElseGet(() -> PairInjection.of(keyInjection.get(), valueInjection.get()));
	}
	
	@Override
	public InfiniteMap<K, V> visitPairSegment() {
		RecordSpaceBuilder builder = RecordSpaceBuilder.on(space);
		RecordSpace pairSegment = currentTag == 0 ? builder.build(segmentType) : builder.build();
		if (currentTag == 0) {
			pairSegment.ownerTag(MapTypes.PAIRSEGMENT.tag());
		}
		return LinearMap.on(pairSegment, pairInjection());
	}

	@Override
	public InfiniteMap<K, V> visitValueSegmentWithKeyIndex() {
		long splitOffset = currentTag == 0 ? split.orElse(Long.highestOneBit(space.capacity()) >>> 1) : Segment.next(space);
		RecordSpaceBuilder builder = RecordSpaceBuilder.on(space.capacity(splitOffset));
		RecordSpace valueSegment = currentTag == 0 ? builder.build(segmentType) : builder.build();
		if (currentTag == 0) {
			valueSegment.ownerTag(MapTypes.VALUESEGMENTWITHKEYINDEX.tag()).next(splitOffset);
		}
		ByteSpace keySpace = space.shift(splitOffset);
		MapBuilder<K, Long> indexBuilder = MapBuilder.<K, Long>on(keySpace).pairInjection(LongValuePairInjection.of(keyInjection.get()).boxed());
		Store<V> valueStore =  StoreAdapter.wrap(valueSegment, valueInjection.get());
		return new IndexedSplitMap<>(currentTag == 0 ? indexBuilder.build(MapTypes.PAIRSEGMENT) : indexBuilder.build(), valueStore);
	}

	@Override
	public InfiniteMap<K, V> visitPairSegmentWithSynonymHash() {
		long splitOffset = currentTag == 0 ? LongHashByteSpace.spaceSize(hashSize.getAsLong()) : Segment.next(space);
		LongHashByteSpace hashSpace = currentTag == 0 ? LongHashByteSpace.on(space, hashSize.getAsLong()) : LongHashByteSpace.on(space);
		if (currentTag == 0) {
			hashSpace.ownerTag(MapTypes.PAIRSEGMENTWITHSYNONYMHASH.tag()).next(splitOffset);
		}
		RecordSpaceBuilder builder = RecordSpaceBuilder.on(space.shift(splitOffset));
		RecordSpace pairSegment = currentTag == 0 ? builder.build(segmentType) : builder.build();
		Store<OrderedPair<K,V>> store = StoreAdapter.wrap(pairSegment, pairInjection());
		LongUnaryOperator hashHelper =  value -> hashFunction.applyAsLong(store.get(value).key());
		SmartSynonymHashCluster hashIndex = new SmartSynonymHashCluster(hashSpace, hashHelper); 
		return new HashIndexedMap<>(hashIndex, store, hashFunction);
	}

	@Override
	public InfiniteMap<K, V> visitPairSegmentWithBinHash() {
		long splitOffset = currentTag == 0 ? LongHashByteSpace.spaceSize(hashSize.getAsLong()) : Segment.next(space);
		LongHashByteSpace hashSpace = currentTag == 0 ? LongHashByteSpace.on(space, hashSize.getAsLong()) : LongHashByteSpace.on(space);
		if (currentTag == 0) {
			hashSpace.ownerTag(MapTypes.PAIRSEGMENTWITHSYNONYMHASH.tag()).next(splitOffset);
		}
		RecordSpaceBuilder builder = RecordSpaceBuilder.on(space.shift(splitOffset));
		RecordSpace pairSegment = currentTag == 0 ? builder.build(segmentType) : builder.build();
		Store<Stream<OrderedPair<K,V>>> store = StoreAdapter.wrap(pairSegment, StreamInjection.of(pairInjection()));
		ToLongFunction<K> hashFunction = k -> (long) k.hashCode() & 0x00000000FFFFFFFFL; 
		return new InfiniteHashMap<>(hashSpace, store, hashFunction);
	}
}
