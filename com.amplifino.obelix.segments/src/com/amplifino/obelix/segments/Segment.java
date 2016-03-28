package com.amplifino.obelix.segments;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;
import com.amplifino.obelix.space.ByteSpace;

/**
 * Segment represents a part of a ByteSpace
 * 
 * A Segment has at least a 16 byte header:
 *  
 * Segment header layout:
 * 
 * 0..1   type tag
 * 1..2   owner tag
 * 3..7   reserved for Segment implementors
 * 8..15  offset to next segment
 *
 * All valid tags are > 0.
 * A value <= 0 indicates a new segment
 * 
 */
public interface Segment {
	
	/**
	 * a tag identifying the Segment's owner type. 
	 * Tags should be unique and > 0
	 * 
	 * @return the tag
	 */
	short ownerTag();
	/**
	 * sets the segment's owner tag
	 * @param ownerTag the new owner tag
	 * @return this segment
	 */
	Segment ownerTag(short ownerTag);
	/**
	 * returns the offset in bytes to the next segment
	 * @return the offset in bytes to the next segment or a value <= 0 if no next segment
	 */
	long next();
	/**
	 * sets the offset to the next segment
	 * @param next the offset in bytes
	 * @return this segment
	 */
	Segment next(long next);
	/**
	 * returns segment stats
	 * @return segment stats
	 */
	default Counts counts() {
		return Counters.empty(SegmentCounters.class).counts();
	}
	
	static final long TYPETAGOFFSET = 0L;
	static final long OWNERTAGOFFSET = 2L;
	static final long NEXTOFFSET = 8L;
	
	static short typeTag(ByteSpace space) {
		return space.getShort(TYPETAGOFFSET);
	}
	
	static short ownerTag(ByteSpace space) {
		return space.getShort(TYPETAGOFFSET);
	}
	
	static long next(ByteSpace space) {
		return space.getLong(NEXTOFFSET);
	}
	
}
