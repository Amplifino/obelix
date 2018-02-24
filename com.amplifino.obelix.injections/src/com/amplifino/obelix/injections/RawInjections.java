package com.amplifino.obelix.injections;

import com.amplifino.obelix.sets.Injection;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class RawInjections {
	
	private static final Injection<String, byte[]> STRINGS = Injection
			.mapper(RawInjections::toUtf8)
			.unmapper(RawInjections::fromUtf8);
	private static final Injection<Long, byte[]> LONGS = Injection
			.<Long, byte[]>mapper(value -> ByteBuffer.allocate(Long.BYTES).putLong(value).array())
			.unmapper(bytes -> ByteBuffer.wrap(bytes).getLong());
	private static final Injection<Integer, byte[]> INTEGERS = Injection
			.<Integer, byte[]>mapper(value -> ByteBuffer.allocate(Integer.BYTES).putInt(value).array())
			.unmapper(bytes -> ByteBuffer.wrap(bytes).getInt());
	private static final Injection<Double, byte[]> DOUBLES = Injection
			.<Double, byte[]>mapper(value -> ByteBuffer.allocate(Double.BYTES).putDouble(value).array())
				.unmapper(bytes -> ByteBuffer.wrap(bytes).getDouble());
	
	private RawInjections(){
		throw new UnsupportedOperationException();
	}
	
	private static String fromUtf8(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	private static byte[] toUtf8(String string) {
		return string.getBytes(StandardCharsets.UTF_8);		
	}
	
	public static Injection<String, byte[]> strings() {
		return STRINGS;
	}
	
	public static Injection<Long, byte[]> longs() {
		return LONGS;
	}
	
	public static Injection<Integer, byte[]> integers() {
		return INTEGERS;
	}
	
	public static Injection<Double, byte[]> doubles() {
		return DOUBLES;
	}
}
