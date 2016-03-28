package com.amplifino.obelix.maps;

import java.util.stream.LongStream;

import com.amplifino.obelix.hash.HashSpace;
import com.amplifino.obelix.hash.LongHashByteSpace;
import com.amplifino.obelix.segments.SmartPointer;

class SmartPointerHashSpace implements HashSpace<SmartPointer> {
	
	private final LongHashByteSpace hashSpace;
	
	SmartPointerHashSpace(LongHashByteSpace hashSpace) {
		this.hashSpace = hashSpace;
	}

	@Override
	public SmartPointerHashSpace put(long key, SmartPointer value) {
		hashSpace.put(key,  value.value());
		return this;
	}


	@Override
	public SmartPointer get(long key) {
		return SmartPointer.of(hashSpace.get(key));
	}

	@Override
	public LongStream domain() {
		return hashSpace.domain();
	}

	@Override
	public long capacity() {
		return hashSpace.capacity();
	}

}
