package com.amplifino.obelix.sortedmaps;

import java.util.Comparator;

import org.osgi.annotation.versioning.ProviderType;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.SegmentTypes;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.sets.SortedInfiniteMap;
import com.amplifino.obelix.space.ByteSpace;

@ProviderType
public interface SortedMapBuilder<K,V> {

	SortedInfiniteMap<K,V> build();
	SortedInfiniteMap<K,V> build(SortedMapTypes type);
	SortedMapBuilder<K,V> keyInjection(Injection<K, byte[]> keyInjection);
	SortedMapBuilder<K,V> valueInjection(Injection<V, byte[]> valueInjection);
	SortedMapBuilder<K,V> pairInjection(Injection<OrderedPair<K,V>, byte[]> pairInjection);
	SortedMapBuilder<K,V> split(long split);
	SortedMapBuilder<K,V> segmentType(SegmentTypes segmentType);
	SortedMapBuilder<K,V> comparator(Comparator<K> comparator);
	SortedMapBuilder<K,V> blockSize(long size);
	
	
	static <K,V> SortedMapBuilder<K,V> on(ByteSpace space) {
		return new SortedMapBuilderImpl<>(space);
	}
	
}
