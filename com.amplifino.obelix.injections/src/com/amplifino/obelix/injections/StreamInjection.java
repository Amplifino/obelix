package com.amplifino.obelix.injections;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amplifino.obelix.sets.Injection;

public final class StreamInjection<T> implements Injection<Stream<T>, byte[] > {

	private final Injection<T,byte[]> injection;
	
	private StreamInjection(Injection<T,byte[]> injection) {
		this.injection = injection;
	}
	
	public static <T> StreamInjection<T> of(Injection<T,byte[]> injection) {
		return new StreamInjection<>(injection);
	}
	
	@Override
	public byte[] map(Stream<T> stream) {
		List<byte[]> mapped = stream.map(injection::map).collect(Collectors.toList());
		int byteCount = mapped.stream().mapToInt(bytes -> bytes.length).sum();
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + Integer.BYTES * mapped.size() + byteCount).putInt(mapped.size());
		mapped.forEach( bytes -> {
			buffer.putInt(bytes.length).put(bytes);
		});
		return buffer.array();
	}

	@Override
	public Stream<T> unmap(byte[] in) {
		ByteBuffer buffer = ByteBuffer.wrap(in);
		int size = buffer.getInt();
		Stream.Builder<T> builder = Stream.builder();
		for (int i = 0 ; i < size ; i++) {
			int length = buffer.getInt();
			byte[] raw = new byte[length];
			buffer.get(raw);
			builder.add(injection.unmap(raw));
		}
		return builder.build();
 	}


}
