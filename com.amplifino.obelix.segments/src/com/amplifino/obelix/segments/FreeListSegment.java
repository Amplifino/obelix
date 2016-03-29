package com.amplifino.obelix.segments;

import java.util.OptionalLong;

import com.amplifino.obelix.guards.LongGuards;
import com.amplifino.obelix.space.ByteSpace;

class FreeListSegment extends HeaderSegment {

	private final FreeList freeList;
		
	private FreeListSegment(ByteSpace space) {
		super(space, SegmentTypes.FREELIST.tag());
		this.freeList = new FreeList(new FreeListOwner(), HeaderSegment.HEADERBYTES);
	}
	
	static FreeListSegment on (ByteSpace space) {
		return new FreeListSegment(space);
	}
	
	@Override
	protected long start() {
		return super.start() + (FreeList.HEADERBYTES >>> 3);
	}

	@Override
	OptionalLong findFree(long size, LongGuards guards) {
		return freeList.find(size);
	}

	@Override
	void free(long key) {
		freeList.free(key);
	}
	
	public long allocateFreeListRecord() {
		long key = allocate(512);
		HeaderRecord.marked(space(), key, 512); 
		return key;
	}
	
	public void printStats() {
		System.out.println("HWM: " + highWaterMark());
		freeList.printStats();
	}
	
	
	private class FreeListOwner implements FreeList.FreeListOwner {
		
		@Override
		public ByteSpace space() {
			return FreeListSegment.this.space();
		}
		
		@Override
		public FreeList.TryResult tryAllocate(long key, long size) {
			HeaderRecord record  = record(key);
			if (record.isFree()) {
				return record.canTake(size) ? FreeList.TryResult.REMOVE : FreeList.TryResult.REJECT;
			} else {
				return FreeList.TryResult.REJECT;
			}
		}	
		
		@Override
		public long allocateFreeListRecord() {
			long key = allocate(512);
			HeaderRecord.marked(space(), key, 512); 
			return key;
		}
	}
	
}
