package com.amplifino.obelix.sortedmaps;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import com.amplifino.obelix.btrees.BTree;
import com.amplifino.obelix.injections.LongValuePairInjection;
import com.amplifino.obelix.injections.PairInjection;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.BlockSpace;
import com.amplifino.obelix.segments.RecordSpace;
import com.amplifino.obelix.segments.RecordSpaceBuilder;
import com.amplifino.obelix.segments.Segment;
import com.amplifino.obelix.segments.SegmentTypes;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.sets.SortedInfiniteMap;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.stores.Store;
import com.amplifino.obelix.stores.StoreAdapter;

class SortedMapBuilderImpl<K,V> implements SortedMapBuilder<K, V>, SortedMapTypes.Visitor<SortedInfiniteMap<K,V>> {
	
	private final ByteSpace space;
	private final short currentTag;
	private Optional<Injection<K, byte[]>> keyInjection = Optional.empty();
	private Optional<Injection<V, byte[]>> valueInjection= Optional.empty();
	private Optional<Injection<OrderedPair<K,V>, byte[]>> pairInjection = Optional.empty();
	private SegmentTypes segmentType = SegmentTypes.FREELIST;
	private OptionalLong split = OptionalLong.empty();
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Comparator<K> comparator = (Comparator) Comparator.naturalOrder();
	private long blockSize = 4096;
	
	SortedMapBuilderImpl(ByteSpace space) {
		this.space = space;
		this.currentTag = Segment.ownerTag(space);
	}

	@Override
	public SortedInfiniteMap<K, V> build() {
		if (currentTag <= 0) {
			throw new IllegalStateException();
		}
		return build(SortedMapTypes.tag(currentTag));
	}

	@Override
	public SortedInfiniteMap<K, V> build(SortedMapTypes type) {
		return type.visit(this);
	}

	@Override
	public SortedMapBuilder<K, V> keyInjection(Injection<K, byte[]> keyInjection) {
		this.keyInjection = Optional.of(keyInjection);
		return this;
	}

	@Override
	public SortedMapBuilder<K, V> valueInjection(Injection<V, byte[]> valueInjection) {
		this.valueInjection = Optional.of(valueInjection);
		return this;
	}

	@Override
	public SortedMapBuilder<K, V> pairInjection(Injection<OrderedPair<K,V>, byte[]> pairInjection) {
		this.pairInjection = Optional.of(pairInjection);
		return this;
	}

	@Override
	public SortedMapBuilder<K, V> segmentType(SegmentTypes segmentType) {
		this.segmentType = Objects.requireNonNull(segmentType);
		return this;
	}
	
	@Override
	public SortedMapBuilder<K,V> split(long split) {
		if (split < 256) {
			throw new IllegalArgumentException();
		}
		this.split = OptionalLong.of(split);
		return this;
	}

	@Override
	public SortedMapBuilder<K,V> comparator(Comparator<K> comparator) {
		this.comparator = comparator;
		return this;
	}
	
	@Override
	public SortedMapBuilder<K,V> blockSize(long size) {
		this.blockSize = size;
		return this;
	}
	
	private Injection<OrderedPair<K,V>, byte[]> pairInjection() {
		return pairInjection.orElseGet(() -> PairInjection.of(keyInjection.get(), valueInjection.get()));
	}

	@Override
	public SortedInfiniteMap<K, V> visitBTree() {
		BlockSpace blockSpace = currentTag <= 0 ? BlockSpace.on(space, blockSize, 0) : BlockSpace.on(space);
		if (currentTag <= 0) {
			blockSpace.ownerTag(SortedMapTypes.BTREE.tag());
		}
		return BTree.on(blockSpace, comparator, keyInjection.get(), pairInjection());
	}
	
	@Override
	public SortedInfiniteMap<K, V> visitValueSegmentWithBTreeIndex() {
		long splitOffset = currentTag == 0 ? split.orElse(Long.highestOneBit(space.capacity()) >>> 1) : Segment.next(space);
		BlockSpace blockSpace = currentTag <= 0 ? BlockSpace.on(space, blockSize, 0) : BlockSpace.on(space);
		if (currentTag <= 0) {
			blockSpace.ownerTag(SortedMapTypes.BTREE.tag()).next(splitOffset);
		}
		BTree<K,Long> btree = BTree.on(
				blockSpace, 
				comparator, 
				keyInjection.get(), 
				LongValuePairInjection.of(keyInjection.get()).boxed());
		RecordSpaceBuilder builder = RecordSpaceBuilder.on(space.shift(splitOffset));
		RecordSpace valueSegment = currentTag == 0 ? builder.build(segmentType) : builder.build();
		Store<V> store = StoreAdapter.wrap(valueSegment, valueInjection.get());
		return new SortedIndexedSplitMap<>(btree, store);
	}
}
