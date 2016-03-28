package com.amplifino.obelix.sets;

import java.util.stream.LongStream;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Represents a full function whose domain is a subset of <code>LongStream.rangeClose(Long.MIN_VALUE, Long.MAX_VALUE)</code>
 *
 */
@ProviderType
public interface LongToLongFullFunction extends LongToLongRelation {
	
	/**
	 * returns the element of the singleton range obtained by restricting the domain to the singleton containing the argument
	 * 
	 * @param key the source element
	 * @return the range element
	 */
	long get(long key);
	
	@Override
	default LongStream range(long key) {
		return LongStream.of(get(key));
	}
}
