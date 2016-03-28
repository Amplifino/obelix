package com.amplifino.obelix.btrees;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.stores.Block;

class LeafNode<K, V> extends AbstractNode<K, V> implements Leaf<K, V> {

	private final ConcurrentBlock<OrderedPair<K, V>> lock;
	private final long tag;
	
	private LeafNode(Branch<K, V> parent, long tag, Stream<OrderedPair<K, V>> stream) {
		this(parent, tag);
		try {
			lock.put( block -> init(block, stream));
		} catch (SplitException ex) {
			throw new IllegalStateException(ex);
		}
	}

	LeafNode(Branch<K, V> parent, long tag) {
		super(parent);
		this.tag = tag;
		this.lock = parent().leafBlock(tag);
	}
	
	LeafNode(Branch<K, V> parent, long tag, OrderedPair<K, V> entry) {
		this(parent, tag, Stream.of(entry));
	}
	
	@Override
	public K firstKey() throws SplitException {
		return lock.get(block -> block.first().get().key());
	}

	@Override
	public Optional<V> get(K key) throws SplitException {
		return lock.get(block -> get(block, key));
	}

	@Override
	public Stream<OrderedPair<K, V>> next(Optional<K> key) throws SplitException {
		Optional<Stream<OrderedPair<K, V>>> result = lock.get(block -> next(block,key));
		if (result.isPresent()) {
			return result.get();
		} else {
			return parent().next(this);
		}
	}

	@Override
	public void put(K key, V value) throws SplitException {
		lock.put(block -> put(block, OrderedPair.of(key,value)));
	}

	@Override
	public void remove(K key) throws SplitException {
		lock.put(block -> remove(block, key));
	}
	
	@Override
	public LeafNode<K, V> split(int split) throws SplitException {
		Stream<OrderedPair<K,V>> toMove = lock.get(block -> spinOff(block, split));
		return new LeafNode<>(this.parent(), parent().allocateLeaf(), toMove);
	}
	
	@Override
	public Stream<OrderedPair<K, V>> start(Optional<K> key) throws SplitException {
		Optional<Stream<OrderedPair<K,V>>>  result = lock.get(block -> start(block, key));
		if (result.isPresent()) {
			return result.get();
		} else {
			return parent().next(this);
		}
	}

	@Override
	public long tag() {
		return tag;
	}
	
	@Override
	public void truncate(int split) {
		parent().counters().increment(BTreeCounters.LEAFSPLITS);
		lock.truncate(split);
	}
	
	@Override
	public Optional<K> trySplit(Optional<K> startKey, Optional<K> endKey) {
		return Optional.empty();
	}

	private Optional<V> get(Block<OrderedPair<K,V>> block, K key ) {
		parent().counters().increment(BTreeCounters.GETS);
		int index = index(block, key);
		if (index >= 0) {
			return Optional.of(block.get(index).value());
		} else {
			return Optional.empty();
		}
	}

	private int index(Block<OrderedPair<K, V>> block, K key) {
		int low = 0;
		int high = block.size() - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			int cmp = parent().compare(block.get(mid).key(), key);
			if (cmp < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return -low-1;
	}

	private final void init(Block<OrderedPair<K,V>> block, Stream<OrderedPair<K,V>> stream) {
		stream.forEach( pair -> {
			if (!block.add(pair)) {
				throw new IllegalArgumentException("Initial entry list to large");
			}
		});
	}

	private Optional<Stream<OrderedPair<K, V>>> next(Block<OrderedPair<K, V>> block,  Optional<K> key) {
		int index = key
			.map(k -> index(block, k))
			.map(i -> i >= 0 ?  i + 1 : -i-1)
			.orElse(0);
		if (index < block.size()) {
			return Optional.of(subList(block, index).stream());
		} else {
			return Optional.empty();
		}
	}

	private void put(Block<OrderedPair<K,V>> block, OrderedPair<K,V> pair) throws SplitException {
		if (!tryPut(block,pair)) {
			int split = block.size() / 2;
			parent().split(new SplitAction<>(this, split, block.get(split).key(), pair));
		}
	}

	private void remove(Block<OrderedPair<K, V>> block, K key) throws SplitException {
		int index = index(block, key);
		if (index >= 0) {
			boolean empty = block.size() == 1;
			if (empty) {				
				parent().remove(this);
				lock.invalidate();
			}
			block.remove(index);
			if (empty) {
				parent().free(tag);
			}
			parent().counters().increment(BTreeCounters.REMOVALS);
		}
	}

	private Stream<OrderedPair<K,V>> spinOff(Block<OrderedPair<K,V>> block, int split) {
		lock.invalidate();
		return IntStream.range(split, block.size()).mapToObj(block::get);
	}

	private Optional<Stream<OrderedPair<K,V>>> start(Block<OrderedPair<K, V>> block, Optional<K> key) {
		parent().counters().increment(BTreeCounters.GETS);
		int index = key
			.map(k -> index(block, k))
			.map(i -> i >= 0 ? i : -i - 1)
			.orElse(0);
		if (index < block.size()) {
			return Optional.of(subList(block, index).stream());
		} else {
			return Optional.empty();
		}
	}

	private List<OrderedPair<K,V>> subList(Block<OrderedPair<K,V>> block, int index) {
		return IntStream.range(index, block.size()).mapToObj(block::get).collect(Collectors.toList());
	}
	
	private boolean tryPut(Block<OrderedPair<K, V>> block, OrderedPair<K, V> pair) {
		int index = index(block, pair.key());
		if (index >= 0) {
			if (block.set(index, pair)) {
				parent().counters().increment(BTreeCounters.UPDATES);
				return true;
			} else {
				return false;
			}
		} else {
			if (block.add(-index-1, pair)) {
				parent().counters().increment(BTreeCounters.INSERTS);
				return true;
			} else {
				return false;
			}
		}
	}
}
