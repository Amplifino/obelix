package com.amplifino.obelix.space;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;

import static com.amplifino.obelix.space.SpaceCounters.*;
/**
 * Byte Space using backing file with regular fileChannel IO
 * 
 * <p> Implements a file space on top of regular file IO. 
 * Note that quite a few file systems have a limit just short of 16TB for file length. 
 * Users of this class on file systems not supporting sparse files may suffer from excessive space allocation</p>
 *  
 */
public final class FileChannelSpace implements ByteSpace {

	private final FileChannel channel;
	private final Counters<SpaceCounters> counters = Counters.of(SpaceCounters.class);
	
	private FileChannelSpace(FileChannel channel) {
		this.channel = channel;
	}
	
	/**
	 * creates a ByteSpace backed by the given file 
	 * If the file does not exist, a sparse file will be created
	 * 
	 * @param path the file backing the ByteSpace
	 * @return byte space
	 * @throws IOException
	 * 		if an IO error occurs
	 */
	public static FileChannelSpace of(Path path) throws IOException {
		return new FileChannelSpace(channel(path));
	}
	
	private static FileChannel channel(Path path) throws IOException {
		if (Files.exists(path)) {
			return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
		} else {
			return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.SPARSE);
		}
	}
	
	/**
	 * {inheritDoc}
	 * 
	 * This implementations calls force on the underlying FileChannel
	 */
	@Override
	public FileChannelSpace force() throws IOException {
		channel.force(true);
		return this;
	}
	
	/**
	 * {inheritDoc}
	 * 
	 * This implementations calls close on the underlying FileChannel
	 */
	@Override
	public void close() throws IOException {
		channel.close();
	}
	
	@Override
	public ByteSpace get(long position, byte[] bytes, int start, int length) {
		counters.increment(LOGICALREADS).increment(PHYSICALREADS).add(BYTESREAD, length);
		try {
			channel.read(ByteBuffer.wrap(bytes, start, length), position);
			return this;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ByteSpace put(long position, byte[] bytes, int start, int length) {
		counters.increment(LOGICALWRITES).increment(PHYSICALWRITES).add(BYTESWRITTEN, length);
		try {
			channel.write(ByteBuffer.wrap(bytes, start, length), position);
			return this;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	/**
	 * {inheritDoc}
	 * 
	 * This implementation returns <code>Long.MAX_VALUE</code>, 
	 * but most file systems limit logical file length. 
	 * 
	 */
	public long capacity() {
		return Long.MAX_VALUE;
	}

	@Override
	public Counts counts() {
		return counters.counts();
	}
	
}
