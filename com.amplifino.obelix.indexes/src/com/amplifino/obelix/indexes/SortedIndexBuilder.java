package com.amplifino.obelix.indexes;

import java.util.Comparator;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.SegmentTypes;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.sets.SortedIndex;
import com.amplifino.obelix.sortedmaps.SortedMapTypes;
import com.amplifino.obelix.space.ByteSpace;

@ProviderType
public interface SortedIndexBuilder<K,V> {

	SortedIndex<K,V> build();
	SortedIndex<K,V> build(SortedMapTypes type);
	SortedIndexBuilder<K,V> keyInjection(Injection<K, byte[]> keyInjection);
	SortedIndexBuilder<K,V> valueInjection(Injection<V, byte[]> valueInjection);
	SortedIndexBuilder<K,V> pairInjection(Injection<OrderedPair<K,V>, byte[]> pairInjection);
	SortedIndexBuilder<K,V> split(long split);
	SortedIndexBuilder<K,V> segmentType(SegmentTypes segmentType);
	SortedIndexBuilder<K,V> keyComparator(Comparator<K> comparator);
	SortedIndexBuilder<K,V> valueComparator(Comparator<V> valueComparator);
	SortedIndexBuilder<K,V> blockSize(long size);
	
	
	static <K,V> SortedIndexBuilder<K,V> on(ByteSpace space) {
		return new SortedIndexBuilderImpl<>(space);
	}
	
}
