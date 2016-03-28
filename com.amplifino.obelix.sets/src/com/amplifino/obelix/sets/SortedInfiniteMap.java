package com.amplifino.obelix.sets;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.OrderedPair;

@ProviderType
public interface  SortedInfiniteMap<K,V> extends InfiniteMap<K,V> {

	Stream<? extends OrderedPair<K, V>> graph(Optional<K> lowerIncluded, Optional<K> upperExcluded);
	
	Comparator<K> comparator();
	
	default Stream<? extends OrderedPair<K,V>> graph(K lower, K upper) {
		return graph(Optional.of(lower), Optional.of(upper));
	}
	
	default Stream<? extends OrderedPair<K,V>> atLeast(K lower) {
		return graph(Optional.of(lower), Optional.empty());
	}
	
	default Stream<? extends OrderedPair<K,V>> lessThan(K upper) {
		return graph(Optional.empty(), Optional.of(upper));
	}
	
	default Stream<? extends OrderedPair<K,V>> sorted() {
		return graph(Optional.empty(), Optional.empty()); 
	}
	
	@Override
	default SortedMap<K,V> asTinyMap() {
		return new SortedTinyMapAdapter<>(this);
	}
	
}
