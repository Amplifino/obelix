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
	
	public static ConcurrentBlockSpace on (BlockSpace space) {
		return new ConcurrentBlockSpace(space);
	}
		
	public BlockLock get(long blockNumber) {
		return cache.computeIfAbsent(blockNumber, this::fetch); 
	}
	
	private BlockLock fetch(long blockNumber) {
		return BlockLock.on(space.get(blockNumber));
	}
		
	public void remove(long blockNumber) {
		cache.remove(blockNumber);
		space.remove(blockNumber);
	}
	
	public void refresh(long blockNumber) {
		cache.remove(blockNumber);
	}
	
	public long getUserHeader() {
		return space.getUserHeader();
	}
	
	public void putUserHeader(long value) {
		 space.putUserHeader(value);
	}
	
	public long allocate() {
		return space.allocate();
	}
	
}

	
