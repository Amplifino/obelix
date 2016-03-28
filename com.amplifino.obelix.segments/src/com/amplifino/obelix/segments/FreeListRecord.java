package com.amplifino.obelix.segments;

import java.util.Iterator;
import java.util.OptionalLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.space.ByteSpace;
 
class FreeListRecord {
	
	private final FreeList freeList;
	private final long key;
	private final long position;
	private Header header;
	
	FreeListRecord(FreeList freeList, long key) {
		this.freeList = freeList;
		this.key = key;
		this.position = key << 3;
		this.header = Header.of(space().getLong(position));
	}

	private ByteSpace space() {
		return freeList.space();
	}
	
	private LongKeyPair<SmartPointer> get(int offset) {
		long entryKey = position + Header.BYTES + offset;
		return LongKeyPair.of(entryKey,  SmartPointer.of(space().getLong(entryKey)));
	}
	
	LongKeyPair<SmartPointer> first() {
		return get(0);
	}
	
	LongKeyPair<SmartPointer> last() {
		return get(header.length() - SmartPointer.BYTES);
	}
	
	Stream<LongKeyPair<SmartPointer>> stream() {
		return IntStream.iterate(SmartPointer.BYTES, val -> val + SmartPointer.BYTES)
			.limit(header.length() / SmartPointer.BYTES)
			.map(value -> header.length() - value)
			.mapToObj(this::get)
			.filter(pair -> !pair.value().isMarked());
	}
	
	boolean tryAdd(SmartPointer pointer) {
		if (Header.BYTES + header.length() + SmartPointer.BYTES <= (header.slotSize() << 3)) {
			add(pointer);
			return true;
		} else {
			return false;
		}
	}
	
	private void add(SmartPointer pointer) {
		space().putLong(position + Header.BYTES + header.length(), pointer.value());
		header = header.length(header.length() + SmartPointer.BYTES);
		space().putLong(position, header.value());
	}
	
	FreeListRecord create(long newKey) {
		if (Header.BYTES + header.length() != (header.slotSize() << 3)) {
			throw new IllegalStateException();
		}
		FreeListRecord record = new FreeListRecord(freeList, newKey);
		record.add(SmartPointer.wrap(key).marked());
		return record;
	}
	
	void removeLast() {
		if (isEmpty()) {
			throw new IllegalStateException();
		}
		header = header.length(header.length() - SmartPointer.BYTES);
		space().putLong(position, header.value());
	}
	
	boolean isEmpty() {
		return header.length() == 0;
	}
	
	OptionalLong find(long minimumSize) {
		Iterator<LongKeyPair<SmartPointer>> iterator = stream()
			.filter(pair -> pair.value().isValid())
			.iterator();
		while (iterator.hasNext()) {
			LongKeyPair<SmartPointer> pair = iterator.next();
			if (freeList.trySlot(pair, minimumSize)) {
				return OptionalLong.of(pair.value().get());
			}
		}
		return OptionalLong.empty(); 
	}
	
	void free(long freeSlot) {
		header = header.free();
		space().putLong(position, header.value());
		space().putLong(freeSlot, SmartPointer.wrap(key).valid().value());
	}
}
