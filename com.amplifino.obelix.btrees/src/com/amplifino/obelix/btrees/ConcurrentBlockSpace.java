package com.amplifino.obelix.btrees;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amplifino.obelix.segments.BlockSpace;

class ConcurrentBlockSpace  {

	private final BlockSpace space;
	private final Map<Long, BlockLock> cache = new ConcurrentHashMap<>();
	
	private ConcurrentBlockSpace(BlockSpace space)  {
		this.space = space;
	}
	
	static ConcurrentBlockSpace on (BlockSpace space) {
		return new ConcurrentBlockSpace(space);
	}
		
	BlockLock get(long blockNumber) {
		return cache.computeIfAbsent(blockNumber, this::fetch); 
	}
	
	private BlockLock fetch(long blockNumber) {
		return BlockLock.on(space.get(blockNumber));
	}
		
	void remove(long blockNumber) {
		cache.remove(blockNumber);
		space.remove(blockNumber);
	}
	
	void refresh(long blockNumber) {
		cache.remove(blockNumber);
	}
	
	long getUserHeader() {
		return space.getUserHeader();
	}
	
	void putUserHeader(long value) {
		 space.putUserHeader(value);
	}
	
	long allocate() {
		return space.allocate();
	}
	
}

	
