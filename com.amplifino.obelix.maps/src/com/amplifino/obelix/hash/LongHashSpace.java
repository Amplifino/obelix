package com.amplifino.obelix.hash;

import java.util.stream.LongStream;
import org.osgi.annotation.versioning.ProviderType;
import com.amplifino.obelix.sets.LongToLongFullFunction;

@ProviderType
public interface LongHashSpace extends LongToLongFullFunction  {
	
	long capacity();

	LongHashSpace put(long index, long value);
	
	@Override
	default LongStream domain() {
		return LongStream.range(0, capacity());
	}

}
