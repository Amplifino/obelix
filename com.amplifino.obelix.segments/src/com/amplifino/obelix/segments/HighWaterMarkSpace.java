package com.amplifino.obelix.segments;

import com.amplifino.obelix.space.ByteSpace;

/**
 * Adds a high water mark field to the segment header 
 * HWM is maintained in unit allocationUnit defined by subclasses
 * 
 */
abstract class HighWaterMarkSpace extends AbstractSegment {
	
	private static long HIGHWATERMARKOFFSET = AbstractSegment.HEADERBYTES;
	protected static long HEADERBYTES = HIGHWATERMARKOFFSET + Long.BYTES;
	
	protected HighWaterMarkSpace(ByteSpace space, short typeTag) {
		super(space, typeTag);
	}
		
	protected final synchronized long highWaterMark() {
		return Math.max(start(), space().getLong(HIGHWATERMARKOFFSET));
	}
	
	protected synchronized long allocate(long size) {
		long current = highWaterMark();
		space().putLong(HIGHWATERMARKOFFSET,  current + size);
		return current;
	}
	
	/**
	 * offset of allocation space in allocation units
	 */
	protected long start() {
		return (HighWaterMarkSpace.HEADERBYTES + allocationUnit() - 1) / allocationUnit(); 
	}
	
	protected abstract long allocationUnit();
}
