package com.amplifino.obelix.segments;

import com.amplifino.obelix.space.ByteSpace;

class RecordSpaceBuilderImpl implements RecordSpaceBuilder, SegmentTypes.Visitor<RecordSpace> {		
	private final ByteSpace space;
	private int guards = 17;
	
	RecordSpaceBuilderImpl(ByteSpace space) {
		this.space = space;
	}
	
	@Override
	public RecordSpace build() {
		return build(SegmentTypes.tag(Segment.typeTag(space)));
	}
	
	@Override
	public RecordSpace build(SegmentTypes type) {
		return type.visit(this);
	}

	@Override
	public RecordSpace visitSimple() {
		return new ConcurrentRecordSpace(SimpleSegment.on(space), guards); 
	}

	@Override
	public RecordSpace visitNoReuse() {
		return NoReuseSegment.on(space);
	}

	@Override
	public RecordSpace visitFreeList() {
		return new ConcurrentRecordSpace(FreeListSegment.on(space), guards);
	}		
}

