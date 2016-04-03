package com.amplifino.obelix.space;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface CellSpace {

	byte[] get(long key);
	CellSpace put(long position, byte[] value);
	int cellSize();
	long capacity();
	
	static CellSpace on(ByteSpace space , int cellSize) {
		if (Integer.highestOneBit(cellSize) != cellSize || cellSize == 0) {
			throw new IllegalArgumentException("Cellsize not a power of 2");
		}
		return new DefaultCellSpace(space, cellSize);
	}
	
}
