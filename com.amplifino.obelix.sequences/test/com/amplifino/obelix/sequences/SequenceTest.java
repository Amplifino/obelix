package com.amplifino.obelix.sequences;

import static org.junit.Assert.assertEquals;

import java.nio.file.FileSystems;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.amplifino.counters.Counts;
import com.amplifino.obelix.injections.RawInjections;
import com.amplifino.obelix.pairs.LongKeyPair;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.space.DirectorySpace;



public class SequenceTest {

	private final static long LENGTH = 10_000_000L;
	private final static double STEP = Math.PI * 2 / LENGTH;
	
	private DoubleSequence cosine;
	private DoubleSequence sine;
	private ByteSpace space;
	private Counts snapshot;
	
	@Before
	public void setup() {
		//this.space = new HeapSpace();
		this.space = DirectorySpace.on(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" ));
		snapshot = space.counts();
		LongMixer mixer = LongMixer.builder()
			.fromKey(0, 32)
			.fromValue(0,29)
			.build();		
		RawSequenceSpace rawSequenceSpace = new RawSequenceSpace(space, mixer, Double.BYTES);
		SequenceSpace<Double> sequenceSpace = new ObjectSequenceSpace<>(rawSequenceSpace, RawInjections.doubles());
		this.cosine = new DoubleSequence(sequenceSpace, 1L);
		this.sine = new DoubleSequence(sequenceSpace, 2L);
		
	}
	
	private LongStream ordinals() {
		return LongStream.range(0, LENGTH  );
	}
	
	private void put(Long value) {
		cosine.put(value, Math.cos(STEP * value));
		sine.put(value, Math.sin(STEP * value));
	}
	@Test
	public void test() {
		
		ordinals()
			.parallel()
			.forEach(this::put);		
		snapshot = space.counts().delta(snapshot, Counts::print);
	
		double cosineSum = ordinals()
			.parallel()
			.mapToDouble(cosine::get)
			.sum();
		snapshot = space.counts().delta(snapshot, Counts::print);
		assertEquals(0d, cosineSum, 0.00000001d);
		double sineSum = ordinals()
			.parallel()
			.mapToDouble(sine::get)
			.sum();
		snapshot = space.counts().delta(snapshot, Counts::print);
		assertEquals(0d, sineSum, 0.00000001d);
		long matches = ordinals()
			.parallel()
			.mapToObj( key -> OrderedPair.of(cosine.get(key), sine.get(key)))
			.mapToDouble(pair -> pair.key() * pair.key() + pair.value() * pair.value())
			.filter(value -> value >= 0.9999)
			.filter(value -> value <= 1.0001)
			.count();
		snapshot = space.counts().delta(snapshot, Counts::print);
		assertEquals(LENGTH, matches);
		Stream<LongKeyPair<Double>> delta = sine.delta(ordinals(), (previous,current) -> (current - previous) / STEP);
		snapshot = space.counts().delta(snapshot, Counts::print);
		matches = delta
				.parallel()
				.map( pair -> OrderedPair.of(pair.value() , cosine.get(pair.key())))
				.filter(pair -> Math.abs(pair.key() - pair.value()) < 0.01)
				.count();
		assertEquals(LENGTH - 1, matches);
		snapshot = space.counts().delta(snapshot, Counts::print);
	}
	
}

