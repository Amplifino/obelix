package com.amplifino.obelix.pairs;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface LongValuePair<K>  {
	
	K key();
	long value();
	
	LongValuePair<K> with(long newValue);
	
	OrderedPair<K,Long> boxed();
	
	static <K> LongValuePair<K> of(K key, long value) {
		return new DefaultLongValuePair<>(key, value);
	}

}
