package com.amplifino.obelix.sets;

import java.util.AbstractMap;
import java.util.Map;

import org.junit.Test;

import com.amplifino.obelix.pairs.OrderedPair;

import static org.junit.Assert.*;

public class OrderedPairTest {

	private final OrderedPair<String, String> pair = OrderedPair.of("key", "value");
	private final Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("key", "value");
	
	@Test
	public void equalTest() {
		System.out.println(-"Amplifino".hashCode());
		System.out.println(Integer.MAX_VALUE);
		challenge(OrderedPair.of("key","value"));
		challenge(OrderedPair.of(entry));
		challenge(OrderedPair.of("key", "").with("value"));
		OrderedPair<String, String> swapped = OrderedPair.of(pair.value(), pair.key());
		// Map.Entry defines hash code as key.hashCode ^ value.hashCode , so swapped has same hashCode 
		assertEquals(pair.hashCode(), swapped.hashCode());
		assertNotEquals(pair, swapped);
		assertNotEquals(swapped, pair);
		OrderedPair<String, String> other = OrderedPair.of("x" , "y");
		assertNotEquals(pair.hashCode(), other.hashCode());
		assertNotEquals(pair, other);
		assertNotEquals(other, pair);
	}
	
	private void challenge(OrderedPair<String, String> challenge) {
		assertEquals(pair.hashCode(), challenge.hashCode());
		assertEquals(pair, challenge);
		assertEquals(challenge, pair);
		assertEquals(entry.hashCode(), challenge.asEntry().hashCode());
		assertEquals(entry, challenge.asEntry());
		assertEquals(challenge.asEntry(), entry);
		assertEquals(OrderedPair.of(entry).hashCode(), challenge.hashCode());
		assertEquals(challenge, OrderedPair.of(entry));
		assertEquals(OrderedPair.of(entry), challenge);
	}
	
	@Test
	public void arrayTest() {
		OrderedPair<byte[], byte[]> pair1 = OrderedPair.of(new byte[] { 1 , 2 } , new byte[] { 3, 4 });
		OrderedPair<byte[], byte[]> pair2 = OrderedPair.of(new byte[] { 1 , 2 } , new byte[] { 3, 4 });
		assertEquals(pair1.hashCode(), pair2.hashCode());
		assertEquals(pair1, pair2);
		assertEquals(pair2, pair1);
		OrderedPair<byte[], byte[]> pair3 = OrderedPair.of(new byte[] { 1 } , new byte[] { 3});
		assertNotEquals(pair1.hashCode(), pair3.hashCode());
		assertNotEquals(pair1, pair3);
		assertNotEquals(pair3, pair1);
		
	}
}
