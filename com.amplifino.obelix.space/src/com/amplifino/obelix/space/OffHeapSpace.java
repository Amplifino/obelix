package com.amplifino.obelix.space;

import java.nio.ByteBuffer;
/**
 * A Byte Space backed by Java Off Heap memory
 * 
 *
 */
public final class OffHeapSpace extends PagedSpace {

	/**
	 * Creates a new ByteSpace with allocations in 64K chunks
	 * 64K was chosen as default to map the behavior of some filesystems
	 * that allocate space to sparse files in 64K chunks 
	 */
	public OffHeapSpace() {
		this(16);  // 64K page size
	}
	
	/**
	 * Creates a new ByteSpace with a page size of <code>2^pageShift</code>
	 * @param pageShift the page shift
	 * @throws IllegalArgumentException
	 * 		if pageShift negative or greater than 30
	 */
	public OffHeapSpace(int pageShift) {
		super(pageShift);
		if (pageShift > 30) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public long capacity() {
		return -1;
	}

	@Override
	protected ByteSpace map(long page, long pageSize) {
		if (pageSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException();
		}
		return ByteBufferSpace.of(ByteBuffer.allocateDirect((int) pageSize));
	}
	
}
