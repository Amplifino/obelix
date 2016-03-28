package com.amplifino.obelix.maps;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.amplifino.counters.Counters;
import com.amplifino.counters.Counts;
import com.amplifino.obelix.guards.LockResource;
import com.amplifino.obelix.hash.HashSpace;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.sets.Index;

public final class SynonymHashCluster<T> implements Index<Long, T> {

	private final HashSpace<Optional<T>> hashSpace;
	private final ToLongFunction<T> hashFunction;
	private final Counters<HashCounters> counters = Counters.of(HashCounters.class);
	private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	public SynonymHashCluster(HashSpace<Optional<T>> hashSpace, ToLongFunction<T> hashFunction) {
		this.hashSpace = hashSpace;
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
	
	private LockResource readLock() {
		return LockResource.lock(lock.readLock());
	}
	
	private LockResource writeLock() {
		return LockResource.lock(lock.writeLock());
	}
	
	@Override
	public Stream<T> range(Long hash) {
		counters.increment(HashCounters.GETS);
		try (LockResource lock = readLock()) {
			return initialGet(hash)
					.map( entry -> Stream.concat(Stream.of(entry.get()), synonyms(entry.hashValue() + 1).map(HashEntry::get)))
					.orElse(Stream.<T>empty());
		} 
	}
	
	@Override
	public Stream<OrderedPair<Long,T>> graph(Long hash) {
		return range(hash).map(t -> OrderedPair.of(hash,t));
	}
	
	public SynonymHashCluster<T> remove(Long hash, T element) {
		counters.increment(HashCounters.REMOVALS);
		try (LockResource lock = writeLock()) {
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
		}
		match.ifPresent(toMove -> {			
			counters.increment(HashCounters.MIGRATES);
			hashSpace.put(freeSlot, Optional.of(toMove.get()));
			toMove.remove();
			fill(toMove.hashValue());
		});
	}
	
	@Override
	public SynonymHashCluster<T> put(Long hash, T element) {
		counters.increment(HashCounters.INSERTS);
		try (LockResource resource = writeLock()) {
			HashEntry entry = hashEntries(hash).findFirst().get();
			if (entry.isAbsent()) {
				hashSpace.put(entry.hashValue(),Optional.of(element));
				return this;
			}
			if (entry.isSynonym()) {
				counters.increment(HashCounters.MIGRATES);
				hashSpace.put(entry.hashValue(), Optional.of(element));
				put(entry.normalSlot(), entry.get());
				return this;
			}
			if (entry.isValid()) {
				counters.increment(HashCounters.COLLISIONS);
				HashEntry newEntry = hashEntries(hash + 1)
						.filter(HashEntry::isAbsent)
						.findFirst()
						.get();
				hashSpace.put(newEntry.hashValue(), Optional.of(element));
				return this;
			}
		}
		throw new IllegalStateException();
	}

	@Override
	public Stream<OrderedPair<Long, T>> graph() {
		return hashSpace.graph()
				.filter(pair -> pair.value().isPresent())
				.map(pair -> OrderedPair.of(pair.key(), pair.value().get()));		
	}

	public Counts counts() {
		return counters.counts();
	}
	
	private class HashEntry {
		private final long hashValue;
		private final Optional<T> reference;
		private OptionalLong normalSlot = OptionalLong.empty();
		
		HashEntry(long hashValue) {
			this.hashValue = hashValue;
			this.reference =  hashSpace.get(hashValue);
		}
		
		boolean isValid() {
			return reference.isPresent() && hashValue == normalSlot();
		}
		
		boolean isSynonym() {
			return reference.isPresent() && hashValue != normalSlot();
		}
		
		boolean isAbsent() {
			return !reference.isPresent();
			
		}
		
		long hashValue() {
			return hashValue;
		}
		
		T get() {
			return reference.get();
		}
		
		void remove() {
			hashSpace.put(hashValue, Optional.empty());
		}
		
		boolean matches(T element) {
			return get().equals(element);
		}
		
		long normalSlot() {
			if (!normalSlot.isPresent()) {
				counters.increment(HashCounters.REHASHES);
				normalSlot = OptionalLong.of(hashFunction.applyAsLong(get()) % hashSpace.capacity());
			}
			return normalSlot.getAsLong();
		}
	}
	

}
