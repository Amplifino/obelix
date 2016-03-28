package com.amplifino.obelix.segments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.amplifino.counters.Counts;
import com.amplifino.obelix.space.HeapSpace;
import com.google.common.base.Stopwatch;

public class FreeListSegmentTest {
	
	private RecordSpace segment;
	private List<Long> keys;
	private Stopwatch watch = Stopwatch.createStarted();
	private Counts snapshot;

	@Test
	public void test() {
		segment = RecordSpaceBuilder.on(new HeapSpace()).build(SegmentTypes.FREELIST);
		snapshot = segment.counts();
		int sampleSize = 100_000;
		keys = new ArrayList<>();
		for (int i = 0 ; i < sampleSize ; i++) {
			keys.add(segment.add(payload()));
		}
		check("After add");
		List<Long> survivors = new ArrayList<>();
		for (int i = 0 ; i < sampleSize ; i++) {
			if (i % 2 == 0) {
				segment.remove(keys.get(i));
			} else {
				survivors.add(keys.get(i));
			}
		}
		keys = survivors;
		check("After remove");
		keys.add(segment.add(payload()));
		check("After remove, add 1");
		keys.add(segment.add(payload()));
		check("After remove, add 2");
		keys.add(segment.add(largerPayload()));
		check("After remove, larger add");
		for (int i = 0 ; i < sampleSize / 4 ; i++) {
			keys.add(segment.add(payload()));
		}
		check("After 1/4 add");
		for (int i = 0 ; i < sampleSize / 4 + 100; i++) {
			keys.add(segment.add(payload()));
		}
		check("After freelist clear add");
		long handle = segment.add(smallerPayload());
		segment.remove(handle);
		check("After small add");
		handle = keys.remove(keys.size() - 1);
		keys.add(segment.replace(handle, largerPayload()));
		check("After final large replace");
		
	}
	
	private void check(String header) {
		print(header, keys);
		//printStats();
		segment.counts().delta(snapshot).print();
		boolean check = keys.stream()
				.map(segment::get)
				.allMatch( bytes -> Arrays.equals(bytes, payload()) || Arrays.equals(bytes, largerPayload()));
		assertTrue(check);
		assertEquals(keys.size() , segment.graph().count());
		snapshot = segment.counts();
		System.out.println();
	}
	
	private  void print(String header, List<Long> list) {
		System.out.println(header + " : " + watch + " last key " + list.get(list.size() - 1));
	}
	
	private byte[] payload() {
		return "aaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes();
	}
	
	private byte[] largerPayload() {
		return "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes();
	}
	
	private byte[] smallerPayload() {
		return "a".getBytes();
	}
}
