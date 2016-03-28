package com.amplifino.obelix.sequences;

import com.amplifino.obelix.sets.Injection;

public final class ObjectSequenceSpace<V> implements SequenceSpace<V> {
		
	private final RawSequenceSpace space;
	private final Injection<V,byte[]> injection;
	
	public ObjectSequenceSpace(RawSequenceSpace space, Injection<V,byte[]> injection) {
		this.space = space;
		this.injection = injection;
	}

	@Override
	public V get(long sequenceId, long key) {
		return injection.unmap(space.get(sequenceId, key));
	}

	@Override
	public SequenceSpace<V> put(long sequenceId, long key, V value) {
		space.put(sequenceId, key, injection.map(value));
		return this;
	}

	
}
