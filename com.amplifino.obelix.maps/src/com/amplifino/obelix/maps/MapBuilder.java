package com.amplifino.obelix.maps;

import java.util.function.ToLongFunction;
import org.osgi.annotation.versioning.ProviderType;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.SegmentTypes;
import com.amplifino.obelix.sets.InfiniteMap;
import com.amplifino.obelix.sets.Injection;
import com.amplifino.obelix.space.ByteSpace;

@ProviderType
public interface MapBuilder<K,V> {

	InfiniteMap<K,V> build();
	InfiniteMap<K,V> build(MapTypes type);
	MapBuilder<K,V> keyInjection(Injection<K, byte[]> keyInjection);
	MapBuilder<K,V> valueInjection(Injection<V, byte[]> valueInjection);
	MapBuilder<K,V> pairInjection(Injection<OrderedPair<K,V>, byte[]> pairInjection);
	MapBuilder<K,V> split(long split);
	MapBuilder<K,V> hashSize(long size);
	MapBuilder<K,V> segmentType(SegmentTypes segmentType);
	MapBuilder<K,V> hashFunction(ToLongFunction<? super K> hashFunction);
	
	static <K,V> MapBuilder<K,V> on(ByteSpace space) {
		return new MapBuilderImpl<>(space);
	}
	
}
