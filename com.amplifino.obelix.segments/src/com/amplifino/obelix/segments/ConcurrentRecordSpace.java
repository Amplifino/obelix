package com.amplifino.obelix.segments;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.amplifino.counters.Counts;
import com.amplifino.obelix.guards.LongGuards;
import com.amplifino.obelix.pairs.LongKeyPair;

class ConcurrentRecordSpace implements RecordSpace {
	
	private final RecordIndex index;
	private final LongGuards guards;
	
	ConcurrentRecordSpace(RecordIndex index, int guardCount) {
		this.index = index;
		this.guards = LongGuards.create(guardCount);
	}

	@Override
	public long add(byte[] element) {
		return guards.write(index.create(element.length, guards) , key -> index.replace(key, element));
	}

	@Override
	public void remove(long key) {
		guards.write(key, index::remove);
	}

	@Override
	public int spareBits() {
		return index.spareBits();
	}

	@Override
	public byte[] get(long key) {
		return safeGet(key).orElseThrow(NoSuchElementException::new);
	}

	@Override
	public LongStream domain() {
		return index.keys().filter(this::contains);
	}

	@Override
	public Stream<LongKeyPair<byte[]>> graph() {
		return index.keys()
			.mapToObj(key -> LongKeyPair.of(key, this.safeGet(key)))
			.filter(pair -> pair.value().isPresent())
			.map(pair -> LongKeyPair.of(pair.key(), pair.value().get()));
	}
	
	@Override 
	public Stream<byte[]> range() {
		return graph().map(LongKeyPair::value);
	}
	
	@Override
	public short ownerTag() {
		return index.ownerTag();
	}

	@Override
	public Segment ownerTag(short ownerTag) {
		index.ownerTag(ownerTag);
		return this;
	}

	@Override
	public long next() {
		return index.next();
	}

	@Override
	public Segment next(long next) {
		index.next(next);
		return this;
	}

	@Override
	public Counts counts() {
		return index.counts();
	}
	
	private boolean contains(long key) {
		return guards.read(key,index::contains);
	}

	private Optional<byte[]> safeGet(long key) {
		return guards.read(key,index::get);
	}

}
