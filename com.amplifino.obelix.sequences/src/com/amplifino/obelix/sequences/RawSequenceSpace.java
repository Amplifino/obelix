package com.amplifino.obelix.sequences;

import com.amplifino.obelix.segments.AbstractSegment;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.space.CellSpace;

public final class RawSequenceSpace extends AbstractSegment implements SequenceSpace<byte[]> {
	
	private final static short TYPETAG = 5445;
	private final CellSpace space;
	private final LongMixer mixer;
	
	public RawSequenceSpace(ByteSpace space, LongMixer mixer , int cellSize) {
		super(space, TYPETAG);
		this.space = CellSpace.on(space.shift(AbstractSegment.HEADERBYTES), cellSize);
		this.mixer = mixer;
	}

	@Override
	public byte[] get(long sequenceId, long key ) {
		return space.get(mixer.mix(sequenceId, key));
	}
	
	@Override
	public RawSequenceSpace put(long sequenceId, long key, byte[] value) {
		space.put(mixer.mix(sequenceId, key), value);
		return this;
	}

}
