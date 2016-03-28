package com.amplifino.obelix.space;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystems;

import org.junit.Test;


public class DirectorySpaceTest {

	@Test
	public void test() throws IOException {
		DirectorySpace space = DirectorySpace.on(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" ));
		space.putLong(0, Long.MAX_VALUE);
		space.putLong(Long.MAX_VALUE - Long.BYTES , Long.MIN_VALUE);
		space.counts().print();
		space.close();
		space = DirectorySpace.on(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" ));
		assertEquals(Long.MAX_VALUE,  space.getLong(0));
		assertEquals(Long.MIN_VALUE,  space.getLong(Long.MAX_VALUE - Long.BYTES));
		space.counts().print();
		space.close();
	}

}
