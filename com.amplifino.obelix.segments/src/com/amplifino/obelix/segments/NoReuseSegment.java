package com.amplifino.obelix.segments;

import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.space.ByteSpace;

public final class NoReuseSegment extends HighWaterMarkSpace implements RecordSpace {

	private NoReuseSegment(ByteSpace space) {
		super(space, SegmentTypes.NOREUSE.tag());
	}
	
	static NoReuseSegment on (ByteSpace space) {
		return new NoReuseSegment(space);
	}

	@Override
	public synchronized long add(byte[] element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		long key = allocate(element.length + Integer.BYTES);
		space().putInt(key, element.length + Integer.BYTES).put(key + Integer.BYTES, element);
		return key;
	}

	@Override
	public void remove(long key) {
		if (key >= highWaterMark()) {
			throw new IllegalArgumentException();
		}
		int length = space().getInt(key);
		if (length <= 0) {
			throw new IllegalStateException();
		}
		synchronized(this) {
			space().put(key, (byte) ((length | Integer.MIN_VALUE) >>> 24));
		}
	}

	@Override
	public byte[] get(long key) {
		if (key >= highWaterMark()) {
			throw new IllegalArgumentException();
		}
		int length = space().getInt(key);
		if (length < Integer.BYTES) {
			throw new NoSuchElementException();
		}
		return space().getBytes(key + Integer.BYTES, length - Integer.BYTES);
	}

	@Override
	public LongStream domain() {
		return StreamSupport.longStream(new Cursor(), false).filter(val -> space().getInt(val) >= Integer.BYTES);
	}
	
	@Override 
	public Stream<LongKeyPair<byte[]>> graph() {
		return StreamSupport.longStream(new Cursor(), false)
			.mapToObj(val -> LongKeyPair.of(val, space().getInt(val)))
			.filter(pair -> pair.value() >= Integer.BYTES)
			.map(pair -> LongKeyPair.of(pair.key(), space().getBytes(pair.key() + Integer.BYTES, pair.value() - Integer.BYTES)));
	}
	
	@Override
	public int spareBits() {
		return 1;
	}
	
	@Override
	protected long allocationUnit() {
		return 1;
	}
	
	
	private class Cursor extends Spliterators.AbstractLongSpliterator {
		
		private long key;
		private final long highWaterMark;
		
		Cursor() {
			super(Long.MAX_VALUE, ORDERED | NONNULL | DISTINCT );
			key = start();
			highWaterMark = highWaterMark();
		}

		@Override
		public boolean tryAdvance(LongConsumer action) {
			if (key >= highWaterMark) {
				return false;
			}
			action.accept(key);
			key += space().getInt(key) & Integer.MAX_VALUE;
			return true;
		} 
	}
}
