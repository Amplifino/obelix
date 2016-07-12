package com.amplifino.obelix.segments;

import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.stores.ByteSpaceBlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.LongStream;

/**
 * 
 * Header layout
 * 
 * 0..1  segment type tag
 * 1..2  owner type tag
 * 4..7  overhead
 * 8..15 next segment offset
 * 16..24 high water mark
 * 24..31 freelist header
 * 32..39 blockSize
 * 40..47 user bytes
 * 48..end of block  initial freelist space
 *
 */
public final class BlockSpace extends HighWaterMarkSpace  {

	private final static long USERBYTES = Long.BYTES;
	private final static long FREELISTOFFSET = HighWaterMarkSpace.HEADERBYTES;
	private final static long CAPACITYOFFSET = FREELISTOFFSET + FreeList.HEADERBYTES;
	private final static long USERBYTESOFFSET = CAPACITYOFFSET + Long.BYTES;
	private final static long HEADERBYTES = USERBYTESOFFSET + USERBYTES;
	private final static short TYPETAG = 129;
	
	private final FreeList freeList;
	private final long blockSize;
	private final int overhead;
	
	private final Map<Long, ByteSpaceBlock> cache = new ConcurrentHashMap<>();
	
	private BlockSpace(ByteSpace space)  {
		super(space, TYPETAG);
		overhead = parameter();
		if (overhead < 0) {
			throw new IllegalStateException();
		}
		blockSize = space().getLong(CAPACITYOFFSET);
		if (blockSize < 128) {
			throw new IllegalStateException();
		}
		this.freeList =  new FreeList(new FreeListOwner(), FREELISTOFFSET);
	}
	
	private BlockSpace(ByteSpace space, long blockSize, int overhead)  {
		super(space, TYPETAG);
		this.overhead = overhead;
		int parameter = parameter();
		if (parameter <= 0) {
			parameter(overhead);
		} else if (parameter != overhead) {
			throw new IllegalStateException();
		}
		this.blockSize = blockSize;
		long currentBlockSize = space().getLong(CAPACITYOFFSET);
		if (currentBlockSize <= 0) {
			space.putLong(CAPACITYOFFSET,  blockSize);
		} else if (currentBlockSize != blockSize) {
			throw new IllegalStateException();
		}
		this.freeList =  new FreeList(new FreeListOwner(), FREELISTOFFSET );
	}
	
	public static BlockSpace on (ByteSpace space) {
		return new BlockSpace(space);
	}
	
	public static BlockSpace on (ByteSpace space , long blockSize, int overhead) {
		return new BlockSpace(space, blockSize, overhead);
	}

	private long allocateBlock() {
		return allocate(1);
	}
	
	private long offset(long blockNumber) {
		if (blockNumber > highWaterMark()) {
			throw new IllegalArgumentException();
		}
		return blockNumber * blockSize;
	}
	
	public ByteSpaceBlock get(long blockNumber) {
		return cache.computeIfAbsent(blockNumber, b -> ByteSpaceBlock.on(space(b))); 
	}
	
	private  ByteSpace space(long blockNumber) {
		return space().slice(offset(blockNumber) + overhead, blockSize - overhead);
	}

	public long allocate() {
		return freeList.find(1).orElseGet(() -> allocateBlock());
	}
	
	public void remove(long blockNumber) {
		cache.remove(blockNumber);
		clear(blockNumber);
		freeList.free(blockNumber);
	}
	
	public long getUserHeader() {
		return space().getLong(USERBYTESOFFSET);
	}
	
	public void putUserHeader(long value) {
		 space().putLong(USERBYTESOFFSET, value);
	}
	
	@Override
	protected long allocationUnit() {
		return blockSize;
	}
	
	private void clear(long blockNumber) {
		long longPosition = offset(blockNumber) >>> 3;
		LongStream.range(0, blockSize >>> 3)
			.forEach( delta -> space().putLong((longPosition + delta) << 3, 0L));
	}
	
	private class FreeListOwner implements FreeList.FreeListOwner {
		
		@Override
		public ByteSpace space()  {
			return BlockSpace.this.space();
		}
		
		@Override
		public FreeList.TryResult tryAllocate(long blockNumber, long size) {
			return FreeList.TryResult.REMOVE;
		}
		
		@Override
		public long allocateFreeListRecord() {
			if (freeList.tail().isPresent()) {
				long blockNumber = allocateBlock();
				HeaderRecord record = HeaderRecord.marked(space(), blockNumber * blockSize / 8, blockSize >>> 3);
				return record.key();
			} else {
				long pointer = BlockSpace.HEADERBYTES;
				HeaderRecord record = HeaderRecord.marked(space(), pointer >>> 3, (blockSize - pointer) >>> 3);
				return record.key();
			}
		}

	}
}

	
