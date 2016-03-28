package com.amplifino.obelix.segments;

import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.sets.LongFullFunction;
import com.amplifino.obelix.space.ByteSpace;

class FreeList implements LongFullFunction<SmartPointer> {

	static final long HEADERDWORDS = 1;
	static final long HEADERBYTES = HEADERDWORDS * Long.BYTES;
	
	private final FreeListOwner segment;
	private final long tailOffset;

	FreeList(FreeListOwner segment, long tailOffset) {
		this.segment = segment;
		this.tailOffset = tailOffset;
	}
	
	boolean trySlot(LongKeyPair<SmartPointer> pair , long size) {
		switch(segment.tryAllocate(pair.value().get(), size)) {
			case REJECT:
				return false;
			case REMOVE:
				moveLast(pair.key());
				// intentionally no break
			case KEEP:
				return true;
		}
		throw new IllegalStateException();
	}
	
	ByteSpace space() {
		return segment.space();
	}
	
	Optional<FreeListRecord> tail() {
		return record(space().getLong(tailOffset));
	}
	
	private void tail(long tail) {
		space().putLong(tailOffset, tail);
	}
	
	private Optional<FreeListRecord> record(long key) {
		return key == 0 ? Optional.empty() : Optional.of(new FreeListRecord(this, key));
	}
	
	@Override
	public SmartPointer get(long key) {
		return SmartPointer.of(space().getLong(key));
	}

	@Override
	public LongStream domain() {
		return graph().mapToLong(LongKeyPair::key);
	}
	
	@Override
	public Stream<LongKeyPair<SmartPointer>> graph() {
		return recordStream().flatMap(FreeListRecord::stream);
	}
	
	synchronized OptionalLong find(long minimumSize) {
		Iterator<FreeListRecord> iterator = recordStream().iterator();
		while (iterator.hasNext()) {
			OptionalLong result = iterator.next().find(minimumSize);
			if (result.isPresent()) {
				return result;
			}
		}
		return OptionalLong.empty();		
	}
	
	synchronized void free(long key) {
		SmartPointer value = SmartPointer.wrap(key).valid();
		if (!tryAdd(value)) {
			addRecord(value);
		}
	}
	
	private boolean tryAdd(SmartPointer pointer) {
		return tail().map(tail -> tail.tryAdd(pointer)).orElse(false);
	}
	
	private FreeListRecord addRecord(SmartPointer pointer) {
		long newTailKey = segment.allocateFreeListRecord();
		FreeListRecord newTail = tail()
			.map(freeListRecord -> freeListRecord.create(newTailKey))
			.orElseGet(() -> new FreeListRecord(this, newTailKey));
		if (!newTail.tryAdd(pointer)) {
			throw new IllegalStateException();
		}
		tail(newTailKey);
		return newTail;
	}
	
	private Stream<FreeListRecord> recordStream() {
		return StreamSupport.stream(new FreeListRecordSpliterator(), false);
	}
	
	void moveLast(long freeSlot) {
		Optional<FreeListRecord> tail = tail();
		if (!tail.isPresent() || tail.get().isEmpty()) {
			throw new IllegalStateException();
		}
		LongKeyPair<SmartPointer> pair = tail.get().last();
		if (pair.key() == freeSlot) {
			tail.get().removeLast();
			return;
		}
		if (pair.value().isValid()) {	
			space().putLong(freeSlot,  pair.value().value());
			tail.get().removeLast();
			return;
		}
		if (pair.value().isMarked()) {
			removeTail(tail.get(), freeSlot);
			return;
		}
		throw new IllegalStateException();
	}
	
	private void removeTail(FreeListRecord tail, long freeSlot) {
		tail(tail.first().value().get());
		tail.free(freeSlot);
	}
	
	synchronized void printStats() {
		long records = recordStream().count();
		long freeBlocks = range().count();
		System.out.println("Free List Record Count: " + records + " free record count: " + freeBlocks ) ;
	}
	
	public enum TryResult {
		REJECT,
		REMOVE,
		KEEP;
	}
	
	public interface FreeListOwner {
		ByteSpace space();
		FreeList.TryResult tryAllocate(long pointer, long size);
		long allocateFreeListRecord();
	}

	private class FreeListRecordSpliterator extends Spliterators.AbstractSpliterator<FreeListRecord> {
		
		private Optional<FreeListRecord> next;

		private FreeListRecordSpliterator() {
			super(Long.MAX_VALUE, ORDERED | DISTINCT | NONNULL );
			next = tail();
		}
		
		private void setNext(FreeListRecord record) {
			LongKeyPair<SmartPointer> pair = record.first();
			if (pair.value().isMarked()) {
				next = Optional.of(new FreeListRecord(FreeList.this,  pair.value().get()));
			} else {
				next = Optional.empty();
			}
		}

		private Consumer<FreeListRecord> advancer() {
			return this::setNext;
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super FreeListRecord> action) {
			if (!next.isPresent()) {
				return false;
			}
			advancer().andThen(action).accept(next.get());
			return true;
		}
	}
		
	
}
