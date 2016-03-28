package com.amplifino.obelix.segments;

import com.amplifino.obelix.space.ByteSpace;

/**
 * Defines the common header format for a segment
 * 
 */
public abstract class AbstractSegment implements Segment {
	
	private static final long PARAMETEROFFSET = 4L;
	private static final long NEXTOFFSET = 8L;
	protected static final long HEADERBYTES = NEXTOFFSET + Long.BYTES;
	
	private final ByteSpace space;
	
	/**
	 * Initializes the space with the type tag field for new segments
	 * performs a type tag check otherwise
	 * @param space the segment's space
	 * @param typeTag tag to write or check
	 */
	protected AbstractSegment(ByteSpace space, short typeTag) {
		if (typeTag <= 0) {
			throw new IllegalArgumentException();
		}
		this.space = space;
		short currentTag = Segment.typeTag(space);
		if (currentTag <= 0) {
			space.putShort(Segment.TYPETAGOFFSET, typeTag);
		} else if (currentTag != typeTag) {
			throw new IllegalArgumentException();
		}
	}
		
	protected ByteSpace space() {
		return space;
	}
	
	protected int parameter() {
		return space().getInt(PARAMETEROFFSET);
	}
	
	protected AbstractSegment parameter(int value) {
		space.putInt(PARAMETEROFFSET, value);
		return this;
	}
	
	@Override
	public short ownerTag() {
		return space.getShort(OWNERTAGOFFSET);
	}
	
	@Override
	public AbstractSegment ownerTag(short ownerTag) {
		space.putShort(OWNERTAGOFFSET, ownerTag);
		return this;
	}
	
	@Override 
	public long next() {
		return space.getLong(NEXTOFFSET);
	}
	
	@Override
	public AbstractSegment next(long next) {
		space.putLong(NEXTOFFSET, next);
		return this;
	}
	
}
