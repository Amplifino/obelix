package com.amplifino.obelix.btrees;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import com.amplifino.counters.Counts;
import com.amplifino.obelix.injections.LongValuePairInjection;
import com.amplifino.obelix.injections.RawInjections;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.BlockSpace;
import com.amplifino.obelix.space.HeapSpace;

public class BTreeTest {

	@Test
	public void test() {
		BlockSpace space = BlockSpace.on(new HeapSpace(),1024, 0);
		BTree<String, Long> tree = BTree.on(
				space, 
				Comparator.<String>naturalOrder(), 
				RawInjections.strings(),
				LongValuePairInjection.of(RawInjections.strings()).boxed());
		assertEquals(0, tree.graph().count());
		tree.put("1", 1L);
		assertEquals(1, tree.graph().count());
		tree.put("1", -1L);
		assertEquals(1, tree.graph().count());
		tree.put("2" , 2L);
		assertEquals(2L, tree.graph().count());
		assertEquals(0L , tree.graph("0", "1").count());
		assertEquals(1L, tree.graph("1", "2").count());
		assertEquals(2L, tree.graph("1", "2.5").count());
		assertEquals(1L, tree.atLeast("1.5").count());
		tree.remove("1");
		assertEquals(1L, tree.graph().count());
	}
	
	@Test
	public void testSplit() {
		BlockSpace space = BlockSpace.on(new HeapSpace(),1024, 0);
		BTree<String, Long> tree = BTree.on(
				space, 
				Comparator.<String>naturalOrder(), 
				RawInjections.strings(), 
				LongValuePairInjection.of(RawInjections.strings()).boxed());
		Counts snapshot = tree.counts();
		Set<String> checkList = Collections.newSetFromMap(new ConcurrentHashMap<>());
		int limit = 999999;
		IntStream.range(0, limit)
			.parallel()
			//.peek(System.out::println)
			.forEach(i -> {
					try {
						String key = String.valueOf(i);
						tree.put(key, Thread.currentThread().getId());
						checkList.add(key);
					} catch (Exception e) {
						System.out.println(i);
						throw e;
					}		
			});
		String key = String.valueOf(limit);
		tree.put(String.valueOf(key), Thread.currentThread().getId());
		checkList.add(key);
		limit++;
		snapshot = tree.counts().delta(snapshot, Counts::print);
		checkList.stream()
			.parallel()
			.filter(k -> !tree.get(k).isPresent())
			.forEach(System.out::println);
		snapshot = tree.counts().delta(snapshot, Counts::print);
		assertEquals((long) limit, tree.graph().parallel().count());
		snapshot = tree.counts().delta(snapshot, Counts::print);
		tree.graph()
			.parallel()
			.map(OrderedPair::key)
			.reduce((previous, next) -> { 
				if (previous.compareTo(next) >= 0 ) {
					throw new IllegalStateException();
				} else { 
					return next;
				}
			});
		snapshot = tree.counts().delta(snapshot, Counts::print);
		System.out.println(tree.graph().parallel().collect(Collectors.groupingBy(pair -> pair.value(), Collectors.counting())));
		snapshot = tree.counts().delta(snapshot, Counts::print);
		/*
		assertTrue(new Random().ints(0, limit)
			.limit(limit/10)
			.parallel()
			.mapToObj(String::valueOf)
			.allMatch( string -> tree.get(string).get().equals(Long.valueOf(string))));
		*/
		IntStream.range(0, limit)
			.parallel()
			//.peek(System.out::println)
			.forEach( i -> tree.remove(String.valueOf(i)));
		snapshot = tree.counts().delta(snapshot, Counts::print);
		assertEquals(0L, tree.graph().parallel().count());
	}
	
	public static void main(String[] args) {
		new BTreeTest().testSplit();
	}
}