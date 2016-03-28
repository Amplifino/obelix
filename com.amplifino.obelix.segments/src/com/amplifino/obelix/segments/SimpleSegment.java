package com.amplifino.obelix.segments;

import java.util.OptionalLong;
import java.util.PrimitiveIterator;

import com.amplifino.obelix.guards.LongGuards;
import com.amplifino.obelix.space.ByteSpace;

public final class SimpleSegment extends HeaderSegment {

	private SimpleSegment(ByteSpace space) {
		super(space, SegmentTypes.SIMPLE.tag());
	}
	
	static SimpleSegment on (ByteSpace space) {
		return new SimpleSegment(space);
	}
	
	@Override
	OptionalLong findFree(long size, LongGuards guards) {
		PrimitiveIterator.OfLong iterator = keys().iterator();
		while(iterator.hasNext()) {
			long key = iterator.nextLong();
			if (guards.read(key, k -> canTake(k, size))) {
				if (guards.readWrite(key, this::mark)) {
					return OptionalLong.of(key);
				}
			}
		}
		return OptionalLong.empty();
	}
	
	private boolean canTake(long key, long size) {
		HeaderRecord record = record(key);
		return  record.isFree() && record.canTake(size);
	}

	private boolean mark(long key) {
		HeaderRecord record = record(key);
		if (record.isFree()) {
			record.mark();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	void free(long key) {
	}
	
}
