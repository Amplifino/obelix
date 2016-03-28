package com.amplifino.obelix.sortedmaps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.amplifino.counters.Counts;
import com.amplifino.obelix.injections.RawInjections;
import com.amplifino.obelix.sets.InfiniteMap;
import com.amplifino.obelix.sets.SortedInfiniteMap;
import com.amplifino.obelix.sortedmaps.SortedMapBuilder;
import com.amplifino.obelix.sortedmaps.SortedMapTypes;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.space.DirectorySpace;
import com.amplifino.obelix.space.HeapSpace;

@RunWith(Parameterized.class)
public class ConcurrentMapTest {
	
	private static final long SAMPLESIZE = 100_000;
	private static final boolean HEAP = true;
	private final ByteSpace space;
	private final InfiniteMap<String, String> map;
	private Counts spaceSnapshot;
	private Counts mapSnapshot;
	private Map<String, String> checkMap = new ConcurrentHashMap<>();
	private final Random random = new Random(9876);
	
	public ConcurrentMapTest(Function<ByteSpace, InfiniteMap<String, String>> generator) {
		this.space = createSpace();
		this.map = generator.apply(space);
		spaceSnapshot = space.counts();
		mapSnapshot = map.counts();
	}
	
	@Parameters
	public static List<Function<ByteSpace, InfiniteMap<String, String>>[]> testMaps() {
		return Stream.<Function<ByteSpace, InfiniteMap<String,String>>> of(
			space -> createBTree(space),
			space -> createIndexed(space),
			space -> InfiniteMap.wrap(new ConcurrentSkipListMap<>())
		).map(f -> new Function[] { f} )
		.collect(Collectors.toList());
	}
	
	private static SortedInfiniteMap<String,String> createBTree(ByteSpace space) {
		return SortedMapBuilder.<String,String>on(space)
				.keyInjection(RawInjections.strings())
				.valueInjection(RawInjections.strings())
				.build(SortedMapTypes.BTREE);		
	}
	
	private static SortedInfiniteMap<String,String> createIndexed(ByteSpace space) {
		return SortedMapBuilder.<String,String>on(space)
				.keyInjection(RawInjections.strings())
				.valueInjection(RawInjections.strings())
				.build(SortedMapTypes.VALUESEGMENTWITHBTREEINDEX);		
	}
	
	private static ByteSpace createSpace() {
		if (HEAP) {
			return new HeapSpace();
		} else {
			return DirectorySpace.on(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" ));
		}
	}
	
	private boolean maybe() {
		return random.nextBoolean();
	}
	
	@Test
	public void test() throws IOException  {
		new Random(1234).longs(SAMPLESIZE)
			.parallel()
			//.peek(System.out::println)
			.forEach( random -> {
				String key = "" + random;
				String value = "" + (random * random);
				checkMap.put(key, value);
				map.put(key, value);
		});
		check ("Initial Put " + SAMPLESIZE);
		Iterator<Map.Entry<String, String>> iterator = new HashSet<>(checkMap.entrySet()).iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if (maybe()) {
				checkMap.remove(entry.getKey());
				map.remove(entry.getKey());
			} else {
				String newValue = "" + (Long.parseLong(entry.getKey()) *  Long.parseLong(entry.getValue()));
				entry.setValue(newValue);
				map.put(entry.getKey(), newValue);
			}
		}
		check("Update/Remove");
		space.close();
	}
	
	void check(String header) {
		print(header);
		List<Map.Entry<String, String>> check = checkMap.entrySet().stream()
				.parallel()
				.filter(entry -> !map.get(entry.getKey()).filter(value -> value.equals(entry.getValue())).isPresent())
				.collect(Collectors.toList());
		assertEquals(0, check.size());
		print(header + " check");
		boolean reverseCheck = map.graph()
				.parallel()
				.allMatch(pair -> checkMap.get(pair.key()).equals(pair.value()));
		assertTrue(reverseCheck);
		assertEquals(checkMap, map.asTinyMap());
		print(header + " reverse check");
	}
	
	
	void print(String header) {
		System.out.println(header);
		mapSnapshot = map.counts().delta(mapSnapshot, Counts::print);
		spaceSnapshot = space.counts().delta(spaceSnapshot, Counts::print);		
	}

}
