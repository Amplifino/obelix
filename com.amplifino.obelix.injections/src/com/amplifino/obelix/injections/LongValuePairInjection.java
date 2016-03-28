package com.amplifino.obelix.injections;

import java.nio.ByteBuffer;

import com.amplifino.obelix.pairs.LongValuePair;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.Injection;

public final class LongValuePairInjection<K,V> implements Injection<LongValuePair<K> , byte[] > {

	private final Injection<K,byte[]> keyInjection;
	
	private LongValuePairInjection(Injection<K,byte[]> keyInjection) {
		this.keyInjection = keyInjection;
	}
	
	public static <K,V> LongValuePairInjection<K,V> of(Injection<K,byte[]> keyInjection) {
		return new LongValuePairInjection<>(keyInjection);
	}
	
	@Override
	public byte[] map(LongValuePair<K> pair) {
		byte[] rawKey = keyInjection.map(pair.key());
		return ByteBuffer.allocate(rawKey.length + Long.BYTES)
			.put(rawKey)
			.putLong(pair.value())
			.array();
	}

	@Override
	public LongValuePair<K> unmap(byte[] in) {
		ByteBuffer buffer = ByteBuffer.wrap(in);
		int keyLength = in.length - Long.BYTES;
		byte[] rawKey = new byte[keyLength];
		buffer.get(rawKey);
		return LongValuePair.of(keyInjection.unmap(rawKey), buffer.getLong());
 	}
	
	public Injection<OrderedPair<K,Long>, byte[]> boxed() {
		Injection<OrderedPair<K, Long>, LongValuePair<K>> injection = 
				Injection.<OrderedPair<K, Long>,LongValuePair<K>>mapper( pair -> LongValuePair.of(pair.key(), pair.value()))
					.unmapper(LongValuePair::boxed);	
		return injection.andThen(this);
	}
}
