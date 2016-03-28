package com.amplifino.obelix.space;

import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import java.util.stream.LongStream;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.sets.LongFullFunction;

@ProviderType
public interface CellSpace extends LongFullFunction<byte[]> {

	public CellSpace put(long position, byte[] value);
	public int cellSize();
	public long capacity();
	
	default Stream<LongKeyPair<byte[]>> graph(long start, long end) {
		return graph(LongStream.range(start,end));
	}
	
	default CellSpace put(Stream<LongKeyPair<byte[]>> stream) {
		stream.forEach(pair -> put(pair.key() , pair.value()));
		return this;
	}
	
	static CellSpace on(ByteSpace space , int cellSize) {
		if (Integer.highestOneBit(cellSize) != cellSize || cellSize == 0) {
			throw new IllegalArgumentException("Cellsize not a power of 2");
		}
		return new DefaultCellSpace(space, cellSize);
	}
}
