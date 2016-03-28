package com.amplifino.obelix.space;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ReadOnlyBufferException;
import java.nio.file.FileSystems;
import org.junit.Test;


public class MappedSpaceTest {

	@Test
	public void test() throws IOException {
		ByteSpace space = MappedFileSpace.temp("temptest");
		space.putLong(0, Long.MAX_VALUE);
		long limit = (1L << 40) - Long.BYTES;
		space.putLong(limit , Long.MIN_VALUE);
		assertEquals(Long.MAX_VALUE,  space.getLong(0));
		assertEquals(Long.MIN_VALUE,  space.getLong(limit));
		space.close();
		space = MappedFileSpace.temp("temptest");
		assertEquals(0L,  space.getLong(0));
		assertEquals(0L,  space.getLong(limit));
		space.close();
	}
	
	@Test(expected=ReadOnlyBufferException.class)
	public void writeReadTest() throws IOException {
		ByteSpace space = MappedFileSpace.of(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" , "rw.map"));
		space.putLong(0, Long.MAX_VALUE);
		long limit = (1L << 40) - Long.BYTES;
		space.putLong(limit , Long.MIN_VALUE);
		assertEquals(Long.MAX_VALUE,  space.getLong(0));
		assertEquals(Long.MIN_VALUE,  space.getLong(limit));
		space.close();
		space = MappedFileSpace.read(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" , "rw.map"));		
		assertEquals(Long.MAX_VALUE,  space.getLong(0));
		assertEquals(Long.MIN_VALUE,  space.getLong(limit));
		try {
			space.putLong(limit , 12345678L);
		} finally {
			space.close();
		}
	}
}
