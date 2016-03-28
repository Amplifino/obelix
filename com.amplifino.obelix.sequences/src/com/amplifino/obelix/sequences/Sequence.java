package com.amplifino.obelix.sequences;

import java.util.Iterator;
import java.util.function.BinaryOperator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.sets.LongFullFunction;

public interface Sequence<V> extends LongFullFunction<V> {

	Sequence<V> put(long index, V value);
	
	default Stream<LongKeyPair<V>> delta (LongStream domain, BinaryOperator<V> operator) {
		Iterator<LongKeyPair<V>> iterator = graph(domain).iterator();
		if (!iterator.hasNext()) {
			return Stream.empty();
		}
		LongKeyPair<V> previous = iterator.next();
		Stream.Builder<LongKeyPair<V>> builder = Stream.builder();
		while (iterator.hasNext()) {
			LongKeyPair<V> current = iterator.next();
			V delta = operator.apply(previous.value(), current.value());
			builder.add(LongKeyPair.of(current.key() - 1, delta));
			previous = current;
		}
		return builder.build();
	}
}
