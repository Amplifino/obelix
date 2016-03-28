package com.amplifino.obelix.space;
/**
 * 
 * Space Counters
 * 
 * <p> Supports Byte Space statistic counters.
 * Logical is defined as used by the ByteSpace API.
 * The semantics of physical reads and writes are determined
 * by the ByteSpace implementer. For a paged space it is typically
 * the number of calls delegated to pages.  
 * </p> 
 *
 */
public enum SpaceCounters {
	LOGICALREADS,
	LOGICALWRITES,
	PHYSICALREADS,
	PHYSICALWRITES,
	BYTESREAD,
	BYTESWRITTEN,
	/**
	 * Counter indicating how often a page was not cached
	 */
	PAGEFAULTS;

}
