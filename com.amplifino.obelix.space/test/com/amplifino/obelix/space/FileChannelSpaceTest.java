package com.amplifino.obelix.space;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystems;

import org.junit.Test;

public class FileChannelSpaceTest {

	@Test
	public void writeReadTest() throws IOException {
		ByteSpace space = FileChannelSpace.of(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" , "rwc.map"));
		space.putLong(0, Long.MAX_VALUE);
		long limit = (1L << 40) - Long.BYTES;
		space.putLong(limit , Long.MIN_VALUE);
		assertEquals(Long.MAX_VALUE,  space.getLong(0));
		assertEquals(Long.MIN_VALUE,  space.getLong(limit));
		space.close();
		space = FileChannelSpace.of(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" , "rwc.map"));
		assertEquals(Long.MAX_VALUE,  space.getLong(0));
		assertEquals(Long.MIN_VALUE,  space.getLong(limit));
		space.close();
	}
}
