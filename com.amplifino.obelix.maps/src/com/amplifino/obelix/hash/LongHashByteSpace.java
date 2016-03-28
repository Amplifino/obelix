package com.amplifino.obelix.hash;

import com.amplifino.counters.Counters;
import com.amplifino.obelix.segments.AbstractSegment;
import com.amplifino.obelix.segments.Segment;
import com.amplifino.obelix.segments.SegmentCounters;
import com.amplifino.obelix.space.ByteSpace;

public final class LongHashByteSpace extends AbstractSegment implements LongHashSpace {
	
	private static final short TYPETAG = 513;
	private static final long CAPACITYOFFSET = AbstractSegment.HEADERBYTES;
	private static final long HEADERBYTES = CAPACITYOFFSET + Long.BYTES;
	
	private final long capacity;
	private final Counters<SegmentCounters> counters = Counters.of(SegmentCounters.class);
	
	private LongHashByteSpace(ByteSpace space, long capacity) {
		super(space.capacity(spaceSize(capacity)), TYPETAG);
		this.capacity = capacity;
	}
	
	static public long spaceSize(long capacity) {
		return capacity * Long.BYTES + HEADERBYTES;
	}
	
	static public LongHashByteSpace on(ByteSpace space) {
		short currentTag = Segment.typeTag(space);
		if (currentTag != TYPETAG) {
			throw new IllegalStateException();
		}
		long capacity = space.getLong(CAPACITYOFFSET);
		return new LongHashByteSpace(space, capacity);
	}
	
	static public LongHashByteSpace on(ByteSpace space, long capacity) {
		short currentTag = Segment.typeTag(space);
		if (currentTag <= 0) {
			space.putShort(Segment.TYPETAGOFFSET, TYPETAG);
			space.putLong(CAPACITYOFFSET, capacity);
		} else {
			if (currentTag != TYPETAG || capacity != space.getLong(CAPACITYOFFSET)) {
				throw new IllegalArgumentException();
			}
		}
		return new LongHashByteSpace(space, capacity);
	}

	private static long overhead() {
		return HEADERBYTES;
	}
	
	@Override
	public long capacity() {
		return capacity;
	}

	private long offset(long index) {
		return normalize(index) * Long.BYTES + overhead();
	}
	
	@Override
	public long get(long index) {
		counters.increment(SegmentCounters.READS);
		return space().getLong(offset(index));
	}

	@Override
	public LongHashByteSpace put(long index, long value) {
		counters.increment(SegmentCounters.UPDATES);
		space().putLong(offset(index), value);
		return this;
	}
	
	public Long normalize(long index) {
		return Long.remainderUnsigned(index, capacity);
	}

}
