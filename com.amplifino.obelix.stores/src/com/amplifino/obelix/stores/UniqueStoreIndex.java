package com.amplifino.obelix.stores;

import java.util.Optional;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.LongValuePair;

@ProviderType
public interface UniqueStoreIndex<K> extends StoreMap<K> {

	Stream<LongValuePair<K>> range(Optional<K> lowerIncluded, Optional<K> upperExcluded);
	
	default Stream<LongValuePair<K>> range(K lower, K upper) {
		return range(Optional.of(lower), Optional.of(upper));
	}
	
	default Stream<LongValuePair<K>> atLeast(K lower) {
		return range(Optional.of(lower), Optional.empty());
	}
	
	default Stream<LongValuePair<K>> lessThan(K upper) {
		return range(Optional.empty(), Optional.of(upper));
	}
	
	default Stream<LongValuePair<K>> sorted() {
		return range(Optional.empty(), Optional.empty()); 
	}
	
}
