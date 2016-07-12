package com.amplifino.obelix.space;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;

/**
 *
 * A byte space
 * 
 * <p>This class represents a 64 bit address space or a subset.
 * ByteSpaces may be backed by any kind of physical memory.
 * This package provides support for memory mapped files, regular files, off heap memory using direct ByteBuffer and java heap space,
 * the latter mainly intended for testing.</p>
 * 
 * <p>The API is inspired on java.nio.ByteBuffer, only retaining the absolute get and put variants.</p>
 * 
 * <p>ByteSpaces should be thread safe. If multiple threads write to the same area the outcome is undefined,
 * but should not cause any side effect on other areas</p>
 * 
 */
@ProviderType
public interface ByteSpace extends AutoCloseable {
	
	/**
	 * 
     * Absolute bulk <i>get</i> method.
     *
     * <p> This method copies <code>length</code> bytes from this
     * space into the given array, starting at the given address in this
     * space and at the given offset in the array.</p>  
     * 
     * @param  position
     * 		   The address from which bytes are copied
     * 
     * @param  bytes
     *         The array into which bytes are to be written
     *
     * @param  start
     *         The offset within the array of the first byte to be
     *         written; must be non-negative and no larger than
     *         <code>bytes.length</code>
     *
     * @param  length
     *         The number of bytes to be written to the given array.
     *         must be non-negative and no larger than <code>bytes.length - start</code>
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace get(long position, byte[] bytes, int start, int length);
	
	/**
	 * 
     * Absolute bulk <i>put</i> method.
     *
     * <p> This method copies <code>length</code> bytes from the given array
     * into this space at the given address, starting at the given offset in the array</p>
     * 
     * @param  position
     * 		   The address to which bytes are copied
     * 
     * @param  bytes
     *         The array from which bytes are to be read
     *
     * @param  start
     *         The offset within the array of the first byte to be
     *         read; must be non-negative and no larger than
     *         <code>bytes.length</code>
     *
     * @param  length
     *         The  number of bytes to be written to the given address.
     *         must be non-negative and no larger than <code>bytes.length - start</code>
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace put(long position, byte[] bytes, int start , int length);
	
	/**
     * Forces any changes made to this space's content to be written to the
     * storage device if applicable.
     *
     * <p> Implementations should document the precise semantics of force.</p>
     * 
     * @throws  IOException
     *        If an I/O error occurs
	 *
     * @return  This buffer
     */
	ByteSpace force() throws IOException;
	
	 /**
     * Closes this space.
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
	@Override
	void close() throws IOException;
	
	/**
	 * Returns this space capacity
	 * 
	 * @return the capacity of this space. Should be interpreted as an unsigned long.
	 */
	long capacity();
	
	/**
	 * 
     * Absolute bulk <i>get</i> method.
     *
     * <p> This method copies <code>bytes.length</code> bytes from this
     * space into the given array, starting at the given address in this
     * space.</p>
     * 
     * @param  position
     * 		   The address from which bytes are copied
     * 
     * @param  bytes
     *         The array into which bytes are to be written
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 * @return This space
	 */	
	default ByteSpace get(long position, byte[] bytes) {
		return get(position, bytes, 0, bytes.length);
	}
	
	/**
	 * 
     * Absolute bulk <i>get</i> method.
     *
     * <p> This method copies <code>length</code> bytes from the
     * the given address in this space.</p>
     * 
     * @param  position
     * 		   The address from which bytes are copied
     * 
     * @param  length
     * 		   The  number of bytes to be copied from the given address.
     *         must be non-negative
     * 
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 * @return the copied byte array.
	 */	
	 byte[] getBytes(long position, int length);
	
	/**
	 * 
     * Absolute bulk <i>get</i> method.
     *
     * <p> This method copies <code>buffer.remaining()</code> bytes from this
     * space into the given ByteBuffer, starting at the given address in this
     * space and at the current position in the buffer</p>
     * 
     * <p> Upon return the buffers position will be the current position + remaining() </p>
     * 
     * @param  position
     * 		   The address from which bytes are copied
     * 
     * @param  buffer
     *         The buffer into which bytes are to be written
     *         
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace get(long position, ByteBuffer buffer);
	
	/**
	 * 
     * Absolute bulk <i>get</i> method.
     *
     * <p> This method copies <code>length</code> bytes from the
     * the given address in this space. No guarantees are made about the
     * nature of the returned ByteBuffer. It may be backed by a heap byte array or 
     * directly by the ByteSpace. It may or may not be a readonly ByteBuffer</p>
     * 
     * @param  position
     * 		   The address from which bytes are copied
     * 
     * @param  length
     * 		   The  number of bytes to be copied from the given address.
     *         must be non-negative
     * 
     * @return  The ByteBuffer wrapping the copied bytes
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */	
	 ByteBuffer get(long position, int length); 
	 
	/**
	 * 
     * Absolute <i>get</i> method.
     *
     * <p> This method copies a single byte from the
     * the given address in this space.</p>
     * 
     * @param  position
     * 		   The address from which bytes are copied
     *       
     * @return  The byte value
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	byte get(long position);
		
	/**
	 * 
     * Absolute <i>get</i> method.
     *
     * <p> Reads two bytes at the given address in this space,
     * composing them into a short value using big endian byte order,
     * </p> 
     * 
     * @param  position
     * 		   The address from which bytes are copied
     *       
     * @return  The short value
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	 short getShort(long position);
	
	/**
	 * 
     * Absolute <i>get</i> method.
     *
     * <p> Reads two bytes at the given address in this space,
     * composing them into a character value using big endian byte order,
     * </p> 
     * 
     * @param  position
     * 		   The address from which bytes are copied
     *       
     * @return  The character value
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	char getChar(long position);
	
	/**
	 * 
     * Absolute <i>get</i> method.
     *
     * <p> Reads four bytes at the given address in this space,
     * composing them into a integer value using big endian byte order,
     * </p> 
     * 
     * @param  position
     * 		   The address from which bytes are copied
     *       
     * @return  The integer value
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	int getInt(long position);
	
	/**
	 * 
     * Absolute <i>get</i> method.
     *
     * <p> Reads eight bytes at the given address in this space,
     * composing them into a long value using big endian byte order,
     * </p> 
     * 
     * @param  position
     * 		   The address from which bytes are copied
     *       
     * @return  The long value
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	long getLong(long position);
		
	/**
	 * 
     * Absolute <i>get</i> method.
     *
     * <p> Reads four bytes at the given address in this space,
     * composing them into a float value using big endian byte order,
     * </p> 
     * 
     * @param  position
     * 		   The address from which bytes are copied
     *       
     * @return  The float value
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	 float getFloat(long position);
	
	/**
	 * 
     * Absolute <i>get</i> method.
     *
     * <p> Reads eight bytes at the given address in this space,
     * composing them into a double value using big endian byte order,
     * </p> 
     * 
     * @param  position
     * 		   The address from which bytes are copied
     *       
     * @return  The double value
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	 double getDouble(long position);
	
	/**
	 * 
     * Absolute bulk<i>get</i> method.
     *
     * <p> Reads {@code length} bytes at the given address in this space,
     * and return them in a stream of chunks. </p>
     * <p> The default implementation returns data in chunks of {@code maxChunkSize}
     * but implementations are free to return data in smaller chunks.
     * The returned stream is sequential by default, but fully supports parallel operations  </p> 
     * 
     * @param  position
     * 		   The address from which bytes are copied
     * 
     * @param  length
     * 			length of byte to read
     *       
     * @param  maxChunkSize
     * 			maximum capacity of a returned ByteBuffer
     * 
     * @return  a stream of ByteBuffers
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	default Stream<ByteBuffer> get(long position , long length, int maxChunkSize) {
		if (maxChunkSize <= 0) {
			throw new IllegalArgumentException();
		}
		if (length < 0 || position + length <= 0) {
			throw new IllegalArgumentException();
		}
		long chunks = length / maxChunkSize;
		int remainder = (int) (length - chunks * maxChunkSize);
		Stream<ByteBuffer> base = LongStream.range(0, chunks)
			.mapToObj( chunk -> get(position + chunk * maxChunkSize, maxChunkSize));
		if (remainder == 0) {
			return base;
		} else {
			return Stream.concat(base, Stream.of(get(position + chunks * maxChunkSize , remainder)));
		}
	}
	
	/**
	 * 
     * Absolute bulk<i>put</i> method.
     *
     * <p> Transfers bytes from a stream to this space</p>
     * <p> Method intended for parallel execution while still limiting memory allocation. 
     * 
     * @param  entries
     * 		   stream streaming over addresses and byte[] buffers to copy
     * 
     * @return  this space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
     *          
	 */
	default ByteSpace put(Stream<Map.Entry<Long, Supplier<byte[]>>> entries) {
		entries.forEach( entry -> put(entry.getKey(), entry.getValue().get()));	
		return this;
	}
	
	/**
	 * 
     * Absolute bulk <i>put</i> method.
     *
     * <p> This method copies <code>bytes.length</code> bytes from the given array
     * into this space at the given address</p>
     * 
     * @param  position
     * 		   The address to which bytes are copied
     * 
     * @param  bytes
     *         The array from which bytes are to be read
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	default ByteSpace put(long position, byte[] bytes) {
		return put(position, bytes, 0 , bytes.length);
	}
	
	/**
	 * 
     * Absolute bulk <i>put</i> method.
     *
     * <p> This method copies <code>buffer.remaining()</code> bytes from the given ByteBuffer
     * into this space at the given address.</p>
     * 
     * <p> Upon return the buffer's position will be its current position + remaining()
     * 
     * @param  position
     * 		   The address to which bytes are copied
     * 
     * @param  buffer
     *         The buffer from which bytes are to be read
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace put(long position, ByteBuffer buffer);
		
	
	/**
	 * 
     * Absolute <i>put</i> method.
     *
     * <p> This method writes a single byte at the given address in this space</p>
     * 
     * @param  position
     * 		   The address to update
     * 
     * @param  in
     *         The new byte value
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace put(long position, byte in);
	
	/**
	 * 
     * Absolute <i>put</i> method.
     *
     * <p> Writes two bytes containing the given short value, in big endian
     * byte order, at the given address in this space.</p>
     * 
     * @param  position
     * 		   The address to update
     * 
     * @param  in
     *         The short value
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace putShort(long position, short in);
		
	/**
	 * 
     * Absolute <i>put</i> method.
     *
     * <p> Writes two bytes containing the given character value, in big endian
     * byte order, at the given address in this space.</p>
     * 
     * @param  position
     * 		   The address to update
     * 
     * @param  in
     *         The character value
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace putChar(long position, char in);
	
	/**
	 * 
     * Absolute <i>put</i> method.
     *
     * <p> Writes four bytes containing the given integer value, in big endian
     * byte order, at the given address in this space.</p>
     * 
     * @param  position
     * 		   The address to update
     * 
     * @param  in
     *         The integer value
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace putInt(long position, int in);
	
	/**
	 * 
     * Absolute <i>put</i> method.
     *
     * <p> Writes eight bytes containing the given long value, in big endian
     * byte order, at the given address in this space.</p>
     * 
     * @param  position
     * 		   The address to update
     * 
     * @param  in
     *         The long value
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace putLong(long position, long in);
	
	/**
	 * 
     * Absolute <i>put</i> method.
     *
     * <p> Writes four bytes containing the given float value, in big endian
     * byte order, at the given address in this space.</p>
     * 
     * @param  position
     * 		   The address to update
     * 
     * @param  in
     *         The float value
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace putFloat(long position, float in);
	
	/**
	 * 
     * Absolute <i>put</i> method.
     *
     * <p> Writes eight bytes containing the given short value, in big endian
     * byte order, at the given address in this space.</p>
     * 
     * @param  position
     * 		   The address to update
     * 
     * @param  in
     *         The double value
     *
     * @return  This space
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on the parameters do not hold
	 *
	 */
	ByteSpace putDouble(long position, double in);
	
	/**
	 * 
	 * Shift space
	 * 
	 * <p>This method returns a new ByteSpace, backed by this space, whose addresses are shifted by the argument.
	 * ( meaning <code>newSpace.get(address) == oldSpace.get(address+value)</code> for all valid addresses</p>
	 * 
	 * @param value amount to shift
	 * 
	 * @return the shifted space
	 */
	default ByteSpace shift(long value) {
		return value == 0 ? this : new ShiftedSpace(this, value);
	}
	
	/**
	 * 
	 * Limit space
	 * 
	 * <p>This method returns a new ByteSpace, backed by this space, whose capacity is limited to the argument.
	 * Any attempt to read or write to addresses larger than <code>newCapacity</code> will result in an IllegalArgumentException</p>
	 * 
	 * @param newCapacity the new capacity limit
	 * 
	 * @return the limited space
	 * 
	 * @throws IllegalArgumentException
	 * 			if the new capacity is greater than the current capacity
	 * 
	 */
	default ByteSpace capacity(long newCapacity) { 
		if (Long.compareUnsigned(capacity(), newCapacity) < 0) {
			throw new IllegalArgumentException();
		}
		return capacity() == newCapacity ? this : new LimitedSpace(this, newCapacity);
	}
	
	/**
	 *
	 * Slice space
	 * 
	 * <p>This method returns a new ByteSpace, backed by this space, whose addresses are shifted and limited by the arguments.
	 * </p>
	 * 
	 * @param shift amount to shift
	 * 
	 * @param capacity the new capacity
	 * 
	 * @return the sliced space
	 */
	default ByteSpace slice(long shift, long capacity) {
		return new ShiftedLimitedSpace(this, shift, capacity);
	}
	
	/**
	 * get stats
	 * 
	 * <p>ByteSpaces can optionally keep track of operational counters.
	 * This method returns a snapshot of the current counts, 
	 * or an empty snapshot if the implementation does not support statistics.</p>
	 * 
	 * @return the counters snapshot.
	 * 
	 */
	default Counts counts() {
		return Counters.empty(SpaceCounters.class).counts();
	}
}
