package com.amplifino.obelix.injections;

import java.nio.ByteBuffer;

import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.Injection;

public final class PairInjection<K,V> implements Injection<OrderedPair<K,V> , byte[] > {

	private final Injection<K,byte[]> keyInjection;
	private final Injection<V,byte[]> valueInjection;
	
	private PairInjection(Injection<K,byte[]> keyInjection, Injection<V,byte[]> valueInjection) {
		this.keyInjection = keyInjection;
		this.valueInjection = valueInjection;
	}
	
	public static <K,V> PairInjection<K,V> of(Injection<K,byte[]> keyInjection, Injection<V,byte[]> valueInjection) {
		return new PairInjection<>(keyInjection, valueInjection);
	}
	
	@Override
	public byte[] map(OrderedPair<K, V> pair) {
		byte[] rawKey = keyInjection.map(pair.key());
		byte[] rawValue = valueInjection.map(pair.value());
		return ByteBuffer.allocate(Integer.BYTES + rawKey.length + rawValue.length)
			.putInt(rawKey.length)
			.put(rawKey)
			.put(rawValue)
			.array();
	}

	@Override
	public OrderedPair<K, V> unmap(byte[] in) {
		ByteBuffer buffer = ByteBuffer.wrap(in);
		int keyLength = buffer.getInt();
		byte[] rawKey = new byte[keyLength];
		buffer.get(rawKey);
		byte[] rawValue = new byte[buffer.remaining()];
		buffer.get(rawValue);
		return OrderedPair.of(keyInjection.unmap(rawKey), valueInjection.unmap(rawValue));
 	}

}
