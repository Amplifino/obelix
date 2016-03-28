package com.amplifino.obelix.segments;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.space.ByteSpace;

class HeaderRecord {

	private final ByteSpace space;
	private final long key;
	private Header header;

	private HeaderRecord(ByteSpace space, long key, Header header) {
		this.space = space;
		this.key = key;
		this.header = header;
	}
	
	static HeaderRecord of(ByteSpace space, long key) {
		return new HeaderRecord(space, key , Header.of(space.getLong(position(key))));
	}
	
	static HeaderRecord create(ByteSpace space, long key, long slotSize , byte[] payload) {
		Header header = Header.of(slotSize, payload.length);
		return new HeaderRecord(space, key, header).payload(payload);
	}
	
	static HeaderRecord marked(ByteSpace space, long key, long slotSize) {
		Header header = Header.marked(slotSize, 0);
		space.putLong(position(key), header.value());
		return new HeaderRecord(space, key, header);
	}
	
	static long position(long key) {
		return key << 3;
	}
	
	long key() {
		return key;
	}
	
	boolean isValid() {
		return header.isValid();
	}
	
	boolean isFree() {
		return header.isFree();
	}
	
	boolean isMarked() {
		return header.isMarked();
	}
	
	boolean canTake(long length) {
		return header.canTake(length);
	}
	
	byte[] payload() {
		return space.getBytes(position(key + Header.LONGS), header.length());
	}
	
	HeaderRecord payload(byte[] value) {
		header = header.length(value.length).valid();
		space.putLong(position(key), header.value());
		space.put(position(key + Header.LONGS), value);
		return this;
	}
	
	void mark() {
		header = header.marked();
		space.putLong(position(key), header.value());
	}
	
	void free() {
		header = header.free();
		space.putLong(position(key), header.value());
	}
	
	long slotSize() {
		return header.slotSize();
	}
	
	LongKeyPair<byte[]> asPair() {
		return LongKeyPair.of(key, payload());
	}
}
