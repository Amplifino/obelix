package com.amplifino.obelix.space;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;

/**
 * Byte Space backed by a directory
 * 
 * <p> As most file system limit maximum length, this implementation uses multiple files
 * in a single directory to provide a real 64 bit address space.
 * To match with most common operating system and file systems limits the default factory methods will
 * allocate space in chunks of 8TB sparse files, requiring 1M files to map the whole 64bit address space. 
 * </p>
 *
 */
public final class DirectorySpace extends PagedSpace {

	private final Path directory; 
	private final BiFunction<Path, Long, ByteSpace> generator;
	
	private DirectorySpace(Path directory, int pageShift, BiFunction<Path, Long ,ByteSpace> generator) {
		super(pageShift);
		this.directory = directory;
		this.generator = generator;
	}
	
	private static BiFunction<Path, Long, ByteSpace> defaultGenerator() {
		return (directory, page) -> {
			try {
				return MappedFileSpace.of(directory.resolve("h" + page));
			} catch (IOException e) {
				throw new RuntimeException(e);			
			}			
		};
	}
	/**
	 * returns a Byte Space backed by the given directory
	 * The implementation uses a 43 bit , 8TB page size.
	 * File names are hxxx with xxx the page number ranging from 0 to 1048575 (2^20-1)
	 * The default generator creates MappedFileSpaces
	 * @param directory backing the space
	 * @return almost infinite space
	 */
	static public DirectorySpace on(Path directory) {
		return new DirectorySpace(directory, 43, defaultGenerator()); // 8 TB
	}
	
	/**
	 * returns a Byte Space backed by the given directory
	 * The implementation uses a 43 bit , 8TB page size.
	 * The creation of the pages is delegated to the <code>generator</code>
	 * @param directory backing the space 
	 * @param generator used to create pages
	 * @return almost infinite space
	 */
	static public DirectorySpace on(Path directory, BiFunction<Path, Long, ByteSpace> generator) { 
		return new DirectorySpace(directory, 43, generator); // 8 TB
	}

	/**
	 * returns a Byte Space backed by the given directory
	 * The creation of the pages is delegated to the <code>generator</code>
	 * Maximum file size is <code>2^pageShift</code> 
	 * @param directory backing the space 
	 * @param generator used to create pages
	 * @param pageShift page shift in bits
	 * @return almost infinite space
	 */
	static public DirectorySpace on(Path directory, BiFunction<Path, Long, ByteSpace> generator, int pageShift) { 
		return new DirectorySpace(directory, pageShift, generator); 
	}
	
	protected final ByteSpace map(long page, long pageSize) {
		return generator.apply(directory, page);
	} 
	
	@Override
	public long capacity() {
		return -1L;
	}
		
}
