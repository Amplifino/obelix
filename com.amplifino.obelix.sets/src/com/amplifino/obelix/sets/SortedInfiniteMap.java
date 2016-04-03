package com.amplifino.obelix.sets;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.OrderedPair;

/**
 * An InfiniteMap that keeps it key value pairs in key order as defined by its comparator
 * Note that the inherited graph() method does not guarantee a sorted stream. Use sorted() instead
 *
 * @param <K>
 * @param <V>
 */
@ProviderType
public interface  SortedInfiniteMap<K,V> extends InfiniteMap<K,V> {

	/**
	 * return graph subset corresponding to the key range defined by the arguments
	 * @param lowerIncluded
	 * @param upperExcluded
	 * @return
	 */
	Stream<? extends OrderedPair<K, V>> graph(Optional<K> lowerIncluded, Optional<K> upperExcluded);
	
	/**
	 * returns the key comparator
	 * @return
	 */
	Comparator<? super K> comparator();
	
	default Stream<? extends OrderedPair<K, V>> graph(K lower, K upper) {
		return graph(Optional.of(lower), Optional.of(upper));
	}
	
	default Stream<? extends OrderedPair<K, V>> atLeast(K lower) {
		return graph(Optional.of(lower), Optional.empty());
	}
	
	default Stream<? extends OrderedPair<K, V>> lessThan(K upper) {
		return graph(Optional.empty(), Optional.of(upper));
	}
	
	default Stream<? extends OrderedPair<K, V>> sorted() {
		return graph(Optional.empty(), Optional.empty()); 
	}
	
	@Override
	default SortedMap<K,V> asTinyMap() {
		return new SortedTinyMapAdapter<>(this);
	}
	
}
