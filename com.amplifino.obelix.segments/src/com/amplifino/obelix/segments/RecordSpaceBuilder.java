package com.amplifino.obelix.segments;

import com.amplifino.obelix.space.ByteSpace;

public interface RecordSpaceBuilder {
	RecordSpace build();
	RecordSpace build(SegmentTypes type);
	
	static RecordSpaceBuilder on(ByteSpace space) {
		return new RecordSpaceBuilderImpl(space);
	}
	
}
