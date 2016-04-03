package com.amplifino.obelix.maps;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;
import com.amplifino.obelix.hash.LongHashByteSpace;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.segments.SmartPointer;
import com.amplifino.obelix.sets.Index;

public final class SmartSynonymHashCluster implements Index<Long, Long> {

	private final SmartPointerHashSpace hashSpace;
	private final LongUnaryOperator hashFunction;
	private final Counters<HashCounters> counters = Counters.of(HashCounters.class);
	
	public SmartSynonymHashCluster(LongHashByteSpace hashSpace, LongUnaryOperator hashFunction) {
		this.hashSpace = new SmartPointerHashSpace(hashSpace);
		this.hashFunction = hashFunction;
	}
	
	private Stream<HashEntry> hashEntries(long hash) {
		return LongStream.range(hash , hash + hashSpace.capacity())
			.map(value -> value % hashSpace.capacity())
			.mapToObj(HashEntry::new);
	}
	
	private Optional<HashEntry> initialGet(long hash) {
		return hashEntries(hash)
			.limit(1)
			.filter(HashEntry::isValid)
			.findFirst();
	}
	
	private Stream<HashEntry> occupied(long hash) {
		Stream.Builder<HashEntry> builder = Stream.builder();
		Iterator<HashEntry> iterator = hashEntries(hash).iterator();
		while (iterator.hasNext()) {
			HashEntry entry = iterator.next();
			if (entry.isAbsent()) {
				break;
			} else {
				builder.add(entry);
			}
		}
		return builder.build();
	}
	
	private Stream<HashEntry> synonyms(long hash) {
		return occupied(hash).filter(HashEntry::isSynonym);
	}
	
	public long normalize(long hash) {
		return Long.remainderUnsigned(hash, hashSpace.capacity());
	}
	
	@Override
	public Stream<Long> range(Long hash) {
		counters.increment(HashCounters.GETS);
		return initialGet(hash)
			.map( entry -> Stream.concat(Stream.of(entry.element()), synonyms(entry.hashValue() + 1).map(HashEntry::element)))
			.orElse(Stream.empty());
	}

	@Override
	public Stream<OrderedPair<Long, Long>> graph(Long hash) {
		return range(hash).map(value -> OrderedPair.of(hash, value)); 
	}
	
	@Override
	public SmartSynonymHashCluster remove(Long hash, Long element) {
		counters.increment(HashCounters.REMOVALS);
		Iterator<HashEntry> iterator = hashEntries(hash).iterator();
		Optional<HashEntry> target = Optional.empty();
		while (iterator.hasNext()) {
			HashEntry hashEntry = iterator.next();
			if (hashEntry.isAbsent()) {
				throw new IllegalArgumentException();
			}
			if (hashEntry.matches(element)) {
				target = Optional.of(hashEntry);
				break;
			}
		}
		target.ifPresent( hashEntry -> {
			hashEntry.remove();
			fill(hashEntry.hashValue());
		});
		if (!target.isPresent()) {
			throw new IllegalArgumentException();
		}
		return this;
	}
	
	private void fill(long freeSlot) {
		List<HashEntry> candidates = occupied(freeSlot + 1).collect(Collectors.toList());
		if (candidates.isEmpty()) {
			return;
		}
		Optional<HashEntry> match = candidates.stream()
				.filter(entry -> entry.normalSlot() == freeSlot)
				.findFirst();
		match.ifPresent(toMove -> {			
			counters.increment(HashCounters.MIGRATES);
			hashSpace.put(freeSlot, toMove.get().valid());
			toMove.remove();
			fill(toMove.hashValue());
		});
		if (!match.isPresent()) {
			Set<Long> intouchables = new HashSet<>();
			for (HashEntry entry : candidates) {
				if (entry.isValid()) {
					intouchables.add(entry.hashValue());
				} else {
					if (!intouchables.contains(entry.normalSlot())) {
						match = Optional.of(entry);
						break;
					}
				}
			}
			match.ifPresent(toMove -> {			
				counters.increment(HashCounters.MIGRATES);
				hashSpace.put(freeSlot, toMove.get());
				toMove.remove();
				fill(toMove.hashValue());
			});
		}
	}
	
	@Override
	public SmartSynonymHashCluster put(Long hash, Long element) {
		counters.increment(HashCounters.INSERTS);
		SmartPointer smartElement = SmartPointer.wrap(element);
		HashEntry entry = hashEntries(hash).findFirst().get();
		if (entry.isAbsent()) {
			hashSpace.put(entry.hashValue(), smartElement.valid());
			return this;
		}
		if (entry.isSynonym()) {
			counters.increment(HashCounters.MIGRATES);
			hashSpace.put(entry.hashValue(), smartElement.valid());
			put(entry.normalSlot(), entry.get().get());
			return this;
		}
		if (entry.isValid()) {
			counters.increment(HashCounters.COLLISIONS);
			HashEntry newEntry = hashEntries(hash + 1)
				.filter(HashEntry::isAbsent)
				.findFirst()
				.get();
			hashSpace.put(newEntry.hashValue(), smartElement.marked());
			return this;
		}
		throw new IllegalStateException();
	}

	@Override
	public Stream<OrderedPair<Long, Long>> graph() {
		return hashSpace.graph().map( pair -> OrderedPair.of(pair.key() , pair.value().get()));		
	}

	public Counts counts() {
		return counters.counts();
	}
	
	private class HashEntry {
		private final long hashValue;
		private final SmartPointer reference;
		private OptionalLong normalSlot = OptionalLong.empty();
		
		HashEntry(long hashValue) {
			this.hashValue = hashValue;
			this.reference =  hashSpace.get(hashValue);
		}
		
		boolean isValid() {
			return reference.isValid();
		}
		
		boolean isSynonym() {
			return reference.isMarked();
		}
		
		boolean isAbsent() {
			return reference.isZero();
			
		}
		
		long hashValue() {
			return hashValue;
		}
		
		SmartPointer get() {
			return reference;
		}
		
		long element() {
			return reference.get();
		}
		
		void remove() {
			hashSpace.put(hashValue, SmartPointer.of(0));
		}
		
		boolean matches(Long element) {
			return element() == element.longValue();
		}
		
		long normalSlot() {
			if (!normalSlot.isPresent()) {
				counters.increment(HashCounters.REHASHES);
				normalSlot = OptionalLong.of(hashFunction.applyAsLong(element()) % hashSpace.capacity());
			}
			return normalSlot.getAsLong();
		}
	}
	

}
