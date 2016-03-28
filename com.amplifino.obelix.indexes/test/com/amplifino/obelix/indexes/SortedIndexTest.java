package com.amplifino.obelix.indexes;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Random;

import org.junit.Test;

import com.amplifino.obelix.injections.RawInjections;
import com.amplifino.obelix.sets.SortedIndex;
import com.amplifino.obelix.sortedmaps.SortedMapTypes;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.space.DirectorySpace;
import com.amplifino.obelix.space.HeapSpace;

public class SortedIndexTest {
	
	private static final long SAMPLESIZE = 10;
	private static final boolean HEAP = true;
	private final ByteSpace space;
	private final SortedIndex<String, String> index;

	
	public SortedIndexTest() {
		this.space = createSpace();
		this.index = createIndex(space);
	}
	private SortedIndex<String,String> createIndex(ByteSpace space) {
		return SortedIndexBuilder.<String,String>on(space)
				.keyInjection(RawInjections.strings())
				.valueInjection(RawInjections.strings())
				.build(SortedMapTypes.BTREE);		
	}
	
	private static ByteSpace createSpace() {
		if (HEAP) {
			return new HeapSpace();
		} else {
			return DirectorySpace.on(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" ));
		}
	}
	
	
	@Test
	public void test() throws IOException  {
		new Random(1234).longs(SAMPLESIZE)
			.forEach( random -> {
				String key = "" + random;
				String value = "" + (random + 1);
				index.put(key, key);
				index.put(key, value);
		});
		index.graph("0","9").peek(System.out::println).count();
	}
	
	
}
