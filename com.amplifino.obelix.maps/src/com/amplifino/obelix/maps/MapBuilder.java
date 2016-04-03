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

	/**
	 * builds an InfiniteMap on an existing bytespace
	 * @return
	 */
	InfiniteMap<K,V> build();
	/**
	 * builds an InfiniteMap of the given type on a nnew bytespace
	 * @param type
	 * @return
	 */
	InfiniteMap<K,V> build(MapTypes type);
	/**
	 * sets the injection used to map keys
	 * @param keyInjection
	 * @return
	 */
	MapBuilder<K,V> keyInjection(Injection<K, byte[]> keyInjection);
	/**
	 * sets the injection used to map values
	 * @param valueInjection
	 * @return
	 */
	MapBuilder<K,V> valueInjection(Injection<V, byte[]> valueInjection);
	/**
	 * sets the injection used to map key value pairs
	 * this takes precedence over keyInjection and valueInjection
	 * @param pairInjection
	 * @return
	 */
	MapBuilder<K,V> pairInjection(Injection<OrderedPair<K,V>, byte[]> pairInjection);
	/**
	 * sets the split point between the key segment and the value segment
	 * Hash based map types should use hashSize instead.
	 * @param split
	 * @return
	 */
	MapBuilder<K,V> split(long split);
	/**
	 * sets the size of the hash space
	 * @param size
	 * @return
	 */
	MapBuilder<K,V> hashSize(long size);
	/**
	 * sets the segment type
	 * @param segmentType
	 * @return
	 */
	MapBuilder<K,V> segmentType(SegmentTypes segmentType);
	/**
	 * sets the hash function
	 * @param hashFunction
	 * @return
	 */
	MapBuilder<K,V> hashFunction(ToLongFunction<? super K> hashFunction);
	
	
	/**
	 * creates a new MapBuilder using the argument as byte space
	 * @param space
	 * @return the new MapBuilder
	 */
	static <K,V> MapBuilder<K,V> on(ByteSpace space) {
		return new MapBuilderImpl<>(space);
	}
	
}
