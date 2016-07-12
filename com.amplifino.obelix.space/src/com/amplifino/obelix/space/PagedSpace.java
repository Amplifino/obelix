package com.amplifino.obelix.space;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;

/**
 * Abstract class for ByteSpace implementations that support paging
 * 
 * <p> This class splits the 64 bit address in two parts:
 * The page number and the page offset.
 * The page number is used as a key into the page map to find the ByteSpace serving the page
 * The split between the page number and the offset is always on a bit boundary</p> 
 */
public abstract class PagedSpace implements ByteSpace {

	private final int pageShift;
	private final long pageSize;
	private final long offsetMask;
	
	private Map<Long, ByteSpace> map = new ConcurrentHashMap<>();
	private final Counters<SpaceCounters> counters =  Counters.of(SpaceCounters.class);
	
	/**
	 * creates a new PagedSpace using <code>pageShift</code>bits for the offset
	 * That is the page size is <code>2^pageShift</code>
	 * 
	 * @param pageShift 
	 * @throws IllegalArgumentException
	 * 		if pageShift is negative or greater than 62
	 */
	protected PagedSpace(int pageShift) {
		if (pageShift < 0 || pageShift > 62) {
			throw new IllegalArgumentException("Illegal page shift: " + pageShift);
		}
		this.pageShift = pageShift;
		this.pageSize = 1L << pageShift;
		this.offsetMask = this.pageSize - 1;
	}
		
	private ByteSpace getPage(long position) {
		return map.computeIfAbsent(position >>> pageShift, page -> this.map(page));
	}
	
	private final ByteSpace map(long page) {
		counters.increment(SpaceCounters.PAGEFAULTS);
		return map(page, pageSize);
	}
	
	/**
	 * returns a byte space serving the requested page.
	 * This method will only be called once for a given page, 
	 * as pages are cached by the implementation
	 *  
	 * @param page requested page number
	 * @param capacity the page size
	 * @return a ByteSpace mapping the page
	 */
	protected abstract ByteSpace map(long page, long capacity);
	
	private long getOffset(long position) {
		return position & offsetMask; 
	}
	
	@Override
	public PagedSpace put(long position, byte[] bytes, int start , int length) {
		counters.increment(SpaceCounters.LOGICALWRITES).add(SpaceCounters.BYTESWRITTEN, length);
		return doPut(position, bytes, start, length);
	}
	
	private PagedSpace doPut(long position, byte[] bytes, int start , int length) {
		long offset = getOffset(position);
		if (offset + length > pageSize) {
			int split = (int) (pageSize - offset);
			getPage(position).put(offset, bytes, start, split);
			return doPut(position + split , bytes, start + split , length - split);
		} else {
			counters.increment(SpaceCounters.PHYSICALWRITES);
			getPage(position).put(offset, bytes, start, length);
			return this;
		}
	}
	
	@Override 
	public ByteSpace put(long position, ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return put(position, bytes);		
	}
	
	@Override
	public PagedSpace get(long position, byte[] bytes, int start , int length) {
		counters.increment(SpaceCounters.LOGICALREADS).add(SpaceCounters.BYTESREAD, length);
		return doGet(position, bytes, start, length);
	}

	private PagedSpace doGet(long position , byte[] bytes, int start, int length) {
		long offset = getOffset(position);
		if (offset + length > pageSize) {
			int split = (int) (pageSize - offset);
			getPage(position).get(offset, bytes, start, split);
			return doGet(position + split , bytes, start + split, length - split);			
		} else {
			counters.increment(SpaceCounters.PHYSICALREADS);
			getPage(position).get(offset, bytes, start, length);
			return this;
		}
	}
	
	@Override
	public ByteSpace get(long position, ByteBuffer buffer) {
		buffer.put(get(position, buffer.remaining()));
		return this;
	}
	
	@Override
	public ByteBuffer get(long position, int length) {
		long offset = getOffset(position);
		if (offset + length > pageSize) {
			return ByteBuffer.wrap(getBytes(position, length));
		} else {
			counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, length);
			return getPage(position).get(offset, length);
		}
	}
	
	@Override
	public byte[] getBytes(long position, int length) {
		byte[] bytes = new byte[length];
		get(position, bytes);
		return bytes;
	}
	
	@Override 
	public byte get(long position) {
		counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, Byte.BYTES);
		return getPage(position).get(getOffset(position));
	}
	
	@Override
	public short getShort(long position) {
		long offset = getOffset(position);
		if (offset + Short.BYTES > pageSize) {
			return get(position, Short.BYTES).getShort();
		} else {
			counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, Short.BYTES);
			return getPage(position).getShort(offset);
		}
	}
	
	@Override
	public char getChar(long position) {
		long offset = getOffset(position);
		if (offset + Character.BYTES > pageSize) {
			return get(position, Character.BYTES).getChar();
		} else {
			counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, Character.BYTES);
			return getPage(position).getChar(offset);
		}
	}
	
	@Override
	public int getInt(long position) {
		long offset = getOffset(position);
		if (offset + Integer.BYTES > pageSize) {
			return get(position, Integer.BYTES).getInt();
		} else {
			counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, Integer.BYTES);
			return getPage(position).getInt(offset);
		}
	}
	
	@Override
	public float getFloat(long position) {
		long offset = getOffset(position);
		if (offset + Float.BYTES > pageSize) {
			return get(position, Float.BYTES).getFloat();
		} else {
			counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, Float.BYTES);
			return getPage(position).getFloat(offset);
		}
	}
	
	@Override
	public long getLong(long position) {
		long offset = getOffset(position);
		if (offset + Long.BYTES > pageSize) {
			return get(position, Long.BYTES).getLong();
		} else {
			counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, Long.BYTES);
			return getPage(position).getLong(offset);
		}
	}
	
	@Override
	public double getDouble(long position) {
		long offset = getOffset(position);
		if (offset + Double.BYTES > pageSize) {
			return get(position, Double.BYTES).getDouble();
		} else {
			counters.increment(SpaceCounters.LOGICALREADS).increment(SpaceCounters.PHYSICALREADS).add(SpaceCounters.BYTESREAD, Double.BYTES);
			return getPage(position).getDouble(offset);
		}
	}
	@Override
	public ByteSpace put(long position, byte in) {
		counters.increment(SpaceCounters.LOGICALWRITES).increment(SpaceCounters.PHYSICALWRITES).add(SpaceCounters.BYTESWRITTEN, Byte.BYTES);
		getPage(position).put(getOffset(position), in);
		return this;
	}
	
	@Override
	public ByteSpace putShort(long position, short in) {
		long offset = getOffset(position);
		if (offset + Short.BYTES > pageSize) {
			put(position, ByteBuffer.allocate(Short.BYTES).putShort(0, in));
		} else {
			counters.increment(SpaceCounters.LOGICALWRITES).increment(SpaceCounters.PHYSICALWRITES).add(SpaceCounters.BYTESWRITTEN, Short.BYTES);
			getPage(position).putShort(offset, in);
		}
		return this;
	}
	
	@Override
	public ByteSpace putChar(long position, char in) {
		long offset = getOffset(position);
		if (offset + Character.BYTES > pageSize) {
			put(position, ByteBuffer.allocate(Character.BYTES).putChar(0, in));
		} else {
			counters.increment(SpaceCounters.LOGICALWRITES).increment(SpaceCounters.PHYSICALWRITES).add(SpaceCounters.BYTESWRITTEN, Character.BYTES);
			getPage(position).putChar(offset, in);
		}
		return this;
	}
	
	@Override
	public ByteSpace putInt(long position, int in) {
		long offset = getOffset(position);
		if (offset + Integer.BYTES > pageSize) {
			put(position, ByteBuffer.allocate(Integer.BYTES).putInt(0, in));
		} else {
			counters.increment(SpaceCounters.LOGICALWRITES).increment(SpaceCounters.PHYSICALWRITES).add(SpaceCounters.BYTESWRITTEN, Integer.BYTES);
			getPage(position).putInt(offset, in);
		}
		return this;
	}
	
	@Override
	public ByteSpace putFloat(long position, float in) {
		long offset = getOffset(position);
		if (offset + Float.BYTES > pageSize) {
			put(position, ByteBuffer.allocate(Float.BYTES).putFloat(0, in));
		} else {
			counters.increment(SpaceCounters.LOGICALWRITES).increment(SpaceCounters.PHYSICALWRITES).add(SpaceCounters.BYTESWRITTEN, Float.BYTES);
			getPage(position).putFloat(offset, in);
		}
		return this;
	}
	
	@Override
	public ByteSpace putLong(long position, long in) {
		long offset = getOffset(position);
		if (offset + Long.BYTES > pageSize) {
			put(position, ByteBuffer.allocate(Long.BYTES).putLong(0, in));
		} else {
			counters.increment(SpaceCounters.LOGICALWRITES).increment(SpaceCounters.PHYSICALWRITES).add(SpaceCounters.BYTESWRITTEN, Long.BYTES);
			getPage(position).putLong(offset, in);
		}
		return this;
	}
	
	@Override
	public ByteSpace putDouble(long position, double in) {
		long offset = getOffset(position);
		if (offset + Integer.BYTES > pageSize) {
			put(position, ByteBuffer.allocate(Double.BYTES).putDouble(0, in));
		} else {
			counters.increment(SpaceCounters.LOGICALWRITES).increment(SpaceCounters.PHYSICALWRITES).add(SpaceCounters.BYTESWRITTEN, Double.BYTES);
			getPage(position).putDouble(offset, in);
		}
		return this;
	}
	
	/**
	 * 	{@inheritDoc}
	 * 
	 *  <p>This implementation calls force on all cached pages</p>
	 *  
	 */
	@Override
	public ByteSpace force() throws IOException {
		for (ByteSpace space : map.values()) {
			space.force();
		}
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p> This implementation calls close on all cached pages, and drops the cache</p>
	 * 
	 */
	@Override
	public void close() throws IOException {
		for (ByteSpace space : map.values()) {
			space.close();
		}
		map = null;
	}
	
	/**
	 * returns the page size
	 * @return the page size
	 */
	public long getPageSize() {
		return pageSize;
	}
	
	/**
	 * returns the page shift
	 * @return the page shift
	 */
	public int getPageShift() {
		return pageShift;
	}
	
	/**
	 * returns the number of pages currently in the page cache
	 * @return the number of mapped pages
	 */
	public int pageCount() {
		return map.size();
	}
	
	/**
	 * returns the number of allocated bytes
	 * @return the number of allocated bytes
	 */
	public long allocated() {
		return pageCount() * pageSize;
	}
	
	@Override
	public Counts counts() {
		return counters.counts();
	}
	
	@Override 
	public ByteSpace slice(long shift, long capacity) {
		long offset = getOffset(shift);
		if (offset + capacity > pageSize) {
			return ByteSpace.super.slice(shift, capacity);
		} else {
			return getPage(shift).slice(offset, capacity);
		}
	}
	
	
}
