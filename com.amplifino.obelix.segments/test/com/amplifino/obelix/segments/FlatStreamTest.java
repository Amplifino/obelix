package com.amplifino.obelix.segments;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class FlatStreamTest {
	
	private int invocationCount;
	
	public boolean tryAdd(String string) {
		invocationCount++;
		return true;
	}
	
	@Test
	@Ignore
	public void test() {
		System.out.println(System.getProperty("java.version"));
		Stream<String> stream = new StreamProvider().stream();
		Optional<String> result = stream.filter(this::tryAdd).findFirst();
		assertEquals("a", result.get());
		assertEquals(1, invocationCount);
		
	}
	
	private class StreamProvider {
		
		
		Stream<String> stream() {
			return Stream.of("aaaaaaaaaaaaaa").flatMap(this::flatten);
		}
		
		private Stream<String> flatten(String in) {
			return IntStream.range(1, in.length())
				.mapToObj(val -> in.substring(0,val));
		}
	}

}
