package com.amplifino.obelix.segments;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.Spliterators;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.amplifino.obelix.guards.LongGuards;
import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.space.ByteSpace;

abstract class HeaderSegment extends HighWaterMarkSpace implements RecordIndex {

	protected HeaderSegment(ByteSpace space, short typeTag) {
		super(space, typeTag);
	}
	
	private long position(long key) {
		return key << 3;
	}
	
	private long key(long position) {
		return position >>> 3;
	}
	
	HeaderRecord record(long key) {
		return HeaderRecord.of(space(), key);
	}
	
	private Optional<byte[]> payload(long key) {
		return Optional.of(record(key)).filter(HeaderRecord::isValid).map(HeaderRecord::payload);
	}
	
	void put(long key, Header header) {
		space().putLong(position(key), header.value());
	}
	
	@Override
	protected synchronized long allocate(long size) {
		long key = super.allocate(size);
		HeaderRecord.marked(space(), key, size);
		return key;
	}
	
	void put(long key, Header header, byte[] value) {
		put(key, header.length(value.length).valid());
		space().put(position(key + 1), value);
	}
	
	@Override
	public final Optional<byte[]> get(long key) {
		if (key < start()) {
			throw new IllegalArgumentException();
		}
		HeaderRecord record = record(key);
		if (record.isValid()) {
			return Optional.of(record.payload());
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public final boolean contains(long key) {
		return record(key).isValid();
	}
	
	@Override
	public LongStream keys() {
		return StreamSupport.longStream(new Cursor(), false);
	}
	
	public final Stream<LongKeyPair<byte[]>> graph() {
		return keys()
			.mapToObj(key -> LongKeyPair.of(key, this.payload(key)))
			.filter(pair -> pair.value().isPresent())
			.map(pair -> LongKeyPair.of(pair.key(), pair.value().get()));
	}

	abstract OptionalLong findFree(long size, LongGuards guards);
	abstract void free(long key);
	
	private void free(HeaderRecord record) {
		record.free();
		free(record.key());
	}
	
	@Override
	public long create(int size, LongGuards guards) {
		return findFree(size, guards).orElseGet(() -> allocate(slotSize(size)));
	}
	
	@Override
	public final void remove(long key) {
		free(record(key));
	}
	
	@Override
	public final boolean replace(long key, byte[] value) {
		HeaderRecord record = record(key);
		if (record.canTake(value.length)) {
			record.payload(value);
			return true;
		} else {
			free(record);
			return false;
		}
	}

	private long slotSize(int length) {
		return Header.LONGS + key(((long) length + Long.BYTES - 1));
	}
	
	@Override
	protected final long allocationUnit() {
		return Long.BYTES;
	};
	
	@Override
	public int spareBits() {
		return 3;
	}
	
	private class Cursor extends Spliterators.AbstractLongSpliterator {
		
		private long key;
		private final long highWaterMark;
		
		Cursor() {
			super(Long.MAX_VALUE, ORDERED | NONNULL | DISTINCT );
			highWaterMark = highWaterMark();
			key = start();
		}

		@Override
		public boolean tryAdvance(LongConsumer action) {
			if (key >= highWaterMark) {
				return false;
			}
			action.accept(key);
			long delta = record(key).slotSize();
			if (delta <= 0) {
				throw new IllegalStateException();
			}
			key += delta;
			return true;
		} 
	}
}
