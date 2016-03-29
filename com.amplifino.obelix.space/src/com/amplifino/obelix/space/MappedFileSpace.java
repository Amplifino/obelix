package com.amplifino.obelix.space;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Byte Space using backing file with memory mapped  IO
 * 
 * <p> Implements a file space on top of memory mapped file IO. 
 * By default this implementation will allocate byte buffers in chunks of 1GB,
 * to limit the number of MappedByteBuffers. 
 * Embedded system users may prefer to create smaller chunks.
 * The Operating System may limit the amount of virtual memory available for a single process.
 * Currently x86 windows only gives a lousy 8TB to a single process, a ridiculous fraction of the 64 bit address space.</p>
 *  
 * <p>Note that quite a few file systems have a limit just short of 16TB for file length. 
 * Users of this class on file systems not supporting sparse files may suffer from excessive space allocation</p> 
 *  
 */
public final class MappedFileSpace extends PagedSpace {

	private final MapMode mapMode; 
	private final FileChannel channel;
	
	private MappedFileSpace(FileChannel channel, MapMode mapMode) {
		// use max pageShift to limit number of MappedByteBuffers
		this(channel, mapMode, 30);
	}
	
	/**
	 * Creates a new Byte Space backed by a memory mapped file
	 * @param channel a channel to the backing file
	 * @param mapMode READ_WRITE , PRIVATE or READ_ONLY 
	 * @param pageShift number of bits in the page shift
	 */
	public MappedFileSpace(FileChannel channel, MapMode mapMode, int pageShift) {
		super(pageShift);
		if (pageShift > 30) {
			throw new IllegalArgumentException();
		}
		this.channel = channel;
		this.mapMode = mapMode;
	}
	
	/**
	 * Creates a new Byte Space backed by the argument in READ_WRITE mode and a standard page shift of 30.
	 * If the file does not exist, a sparse file will be created if the filesystem supports sparse files
	 * @param path backing file
	 * @return the byte space
	 * @throws IOException
	 * 		if an IO error occurs
	 */
	public static MappedFileSpace of(Path path) throws IOException {
		return new MappedFileSpace(channel(path, false), MapMode.READ_WRITE);
	}
	
	/**
	 * Creates a new Byte Space backed by temporary virtual memory.
	 * A file with the arguments name in the users temp directory will be created or opened,
	 * but will never be written to. However the file can be used to initialize the temporary byte space
	 * 
	 * @param name the temporary file name
	 * @return the byte space
	 * @throws IOException
	 * 		if an IO error occurs
	 */
	public static MappedFileSpace temp(String name) throws IOException {
		return new MappedFileSpace(tempChannel(name), MapMode.PRIVATE);
	}
	
	/**
	 * Creates a new read only Byte Space backed by the argument
	 * 
	 * @param path the read only file
	 * @return the byte space
	 * @throws IOException
	 * 		if an IO error occurs, or the file does not exist
	 */
	public static MappedFileSpace read(Path path) throws IOException {
		return new MappedFileSpace(channel(path, true), MapMode.READ_ONLY);
	}
	
	private static FileChannel channel(Path path, boolean readOnly) throws IOException {
		if (Files.exists(path)) {
			if (readOnly) {
				return FileChannel.open(path, StandardOpenOption.READ);
			} else {
				return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
			}
		} else {
			if (readOnly) {
				throw new IOException("File " + path + " not found");
			} else {
				return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.SPARSE);
			}
		}
	}
	
	private static FileChannel tempChannel(String name) throws IOException {
		return channel(FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"), name), false);
	}
	
	@Override
	protected ByteSpace map(long page , long pageSize) {
		if (pageSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException();
		}
		try {
			return ByteBufferSpace.of(channel.map(mapMode, page << getPageShift() , (int) pageSize));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 *  {inheritDoc}
	 *  
	 *  In addition to the super class behavior, 
	 *  this implementation also  calls force on the underlying file channel.
	 */
	@Override
	public MappedFileSpace force() throws IOException {
		super.force();
		channel.force(true);
		return this;
	}
	
	/**
	 *  {inheritDoc}
	 *  
	 *  In addition to the super class behavior, 
	 *  this implementation also  calls close on the underlying file channel.
	 */
	@Override
	public void close() throws IOException {
		super.close();
		channel.close();
	}
	
	@Override
	public long capacity() {
		return Long.MAX_VALUE;
	}
	
	
}
