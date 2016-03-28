package com.amplifino.obelix.space;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;

/**
 * Abstract class for ByteSpace implementations that support paging
 * 
 * <p> This class splits the 63 bit address in two parts:
 * The page number and the page offset.
 * The page number is used as a key into the page map to find the ByteSpace serving the page
 * The split between the page number and the offset is always on a bit boundary</p> 
 */
public abstract class PagedSpace implements ByteSpace {

	private final int pageShift;
	private final long pageSize;
	private final long offsetMask;
	
	private Map<Long, ByteSpace> map = new ConcurrentHashMap<>();
	private final Counters<SpaceCounters> counters = Counters.of(SpaceCounters.class);
	
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
			doPut(position , bytes, start, split);
			return doPut(position + split , bytes, start + split , length - split);
		} else {
			counters.increment(SpaceCounters.PHYSICALWRITES);
			getPage(position).put(offset, bytes, start, length);
			return this;
		}
	}
	
	@Override
	public PagedSpace get(long position, byte[] bytes, int start , int length) {
		counters.increment(SpaceCounters.LOGICALREADS).add(SpaceCounters.BYTESREAD, length);
		return doGet(position, bytes, start, length);
	}

	private PagedSpace doGet(long position , byte[] bytes, int start, int length) {
		long offset = getOffset(position);
		if (offset + length > pageSize) {
			if (length > 1000) {
				System.out.println("Large");
			}
			int split = (int) (pageSize - offset);
			doGet(position , bytes, start, split);
			return doGet(position + split , bytes, start + split, length - split);			
		} else {
			counters.increment(SpaceCounters.PHYSICALREADS);
			getPage(position).get(offset, bytes, start, length);
			return this;
		}
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
	
}
