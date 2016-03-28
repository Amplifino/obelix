package com.amplifino.obelix.maps;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.amplifino.counters.Counts;
import com.amplifino.obelix.injections.RawInjections;
import com.amplifino.obelix.segments.SegmentTypes;
import com.amplifino.obelix.sets.InfiniteMap;
import com.amplifino.obelix.space.ByteSpace;
import com.amplifino.obelix.space.HeapSpace;

@RunWith(Parameterized.class)
public class SlowMapTest {
	
	private static final long SAMPLESIZE = 5_000;
	private final ByteSpace space;
	private final InfiniteMap<String, String> map;
	private Counts spaceSnapshot;
	private Counts mapSnapshot;
	private Random luckGenerator = new Random(9876);
	private Map<String, String> checkMap = new ConcurrentHashMap<>();
	
	public SlowMapTest(Function<ByteSpace, InfiniteMap<String, String>> generator) {
		this.space = createSpace();
		this.map = generator.apply(space);
		spaceSnapshot = space.counts();
		mapSnapshot = map.counts();
	}
	
	@Parameters
	public static List<Function<ByteSpace, InfiniteMap<String, String>>[]> testMaps() {
		return Stream.<Function<ByteSpace, InfiniteMap<String,String>>> of(
			 space -> builder(space).segmentType(SegmentTypes.NOREUSE).build(MapTypes.PAIRSEGMENT),
			 space -> builder(space).segmentType(SegmentTypes.SIMPLE).build(MapTypes.PAIRSEGMENT),
			 space -> builder(space).build(MapTypes.PAIRSEGMENT),
			 space -> builder(space).build(MapTypes.VALUESEGMENTWITHKEYINDEX)	
		).map( f -> new Function[]  { f })
		.collect(Collectors.toList());
	}
	
	
	private static ByteSpace createSpace() {
		//return DirectorySpace.on(FileSystems.getDefault().getPath(System.getProperty("user.home"), ".obelix" ));
		return new HeapSpace();
	}
	
	private static MapBuilder<String, String> builder(ByteSpace space) {
		return MapBuilder.<String,String>on(space)
				.keyInjection(RawInjections.strings())
				.valueInjection(RawInjections.strings());
	}
	
	private boolean maybe() {
		synchronized (luckGenerator) {
			return luckGenerator.nextBoolean();
		}
	}
	
	@Test
	public void test() throws IOException  {
		new Random(1234).longs(SAMPLESIZE)
			.parallel()
			.forEach( random -> {
				String key = "" + random;
				String value = "" + (random * random);
				checkMap.put(key, value);
				map.put(key, value);
		});
		new HashSet<>(checkMap.entrySet()).stream()
			.parallel()
			.forEach( entry -> {
				if (maybe()) {
					checkMap.remove(entry.getKey());
					map.remove(entry.getKey());
				} else {
					String newValue = "" + (Long.parseLong(entry.getKey()) *  Long.parseLong(entry.getValue()));
					entry.setValue(newValue);
					map.put(entry.getKey(), newValue);
				}
			});
		check("Update/Remove");
		space.close();
	}
	
	void check(String header) {
		print(header);
		boolean check = checkMap.entrySet().stream()
				.parallel()
				.allMatch(entry -> map.get(entry.getKey()).get().equals(entry.getValue()));
		assertTrue(check);
		print(header + " check");
		boolean reverseCheck = map.graph()
				.parallel()
				.allMatch(pair -> checkMap.get(pair.key()).equals(pair.value()));
		assertTrue(reverseCheck);
		print(header + " reverse check");
	}
	
	
	void print(String header) {
		Counts mapCounts = map.counts();
		Counts spaceCounts = space.counts();
		System.out.println(header);
		mapCounts.delta((Counts) mapSnapshot).print();
		spaceCounts.delta(spaceSnapshot).print();
		System.out.println();
		mapSnapshot = mapCounts;
		spaceSnapshot = spaceCounts;
	}

}
