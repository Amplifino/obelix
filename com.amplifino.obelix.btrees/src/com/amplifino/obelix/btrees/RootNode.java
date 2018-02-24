package com.amplifino.obelix.btrees;

import java.util.Comparator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.amplifino.counters.Accumulators;
import com.amplifino.obelix.pairs.LongValuePair;
import com.amplifino.obelix.pairs.OrderedPair;

class RootNode<K,V> implements Branch<K,V> {
	
	private final BTree<K,V> bTree;
	private final RootSplitState<K,V> splitState;

	RootNode(BTree<K,V> bTree) {
		this.bTree = bTree;
		long rootTag = bTree.space().getUserHeader();
		Optional<RealNode<K,V>> root = Optional.of(rootTag)
				.filter( tag -> tag != 0)
				.map(tag -> node(this,tag));
		this.splitState = new RootSplitState<>( root , bTree.space(), counters());
	}
	
	@Override
	public long allocateBranch() {
		counters().increment(BTreeCounters.ALLOCATIONS);
		return (space().allocate() << 1) | 1;
	}

	@Override
	public long allocateLeaf() {
		counters().increment(BTreeCounters.ALLOCATIONS);
		return space().allocate() << 1;
	}

	@Override
	public ConcurrentBlock<LongValuePair<K>> branchBlock(long tag) {
		if ((tag & 1) == 0) {
			throw new IllegalArgumentException();
		}
		BlockLock lock = space().get(tag >>> 1);
		return ConcurrentBlock.on(bTree.branchInjection(), lock);
	}

	@Override
	public int compare(K key1, K key2) {
		counters().increment(BTreeCounters.COMPARES);
		return bTree.compare(key1, key2);
	}
	
	@Override
	public Accumulators<BTreeCounters> counters() {
		return bTree.counters();
	}
	
	@Override
	public void free(long tag) {
		counters().increment(BTreeCounters.FREES);
		space().remove(tag >> 1);
	}
	
	@Override
	public Optional<V> get(K key) {
		return splitState.retryGet(root -> get(root, key)); 
	}
	
	public Stream<OrderedPair<K,V>> graph(Optional<K> startKey, Optional<K> endKey) {
		return StreamSupport.stream(() -> new BTreeSplitIterator(startKey, endKey), BTreeSplitIterator.CHARACTERISTICS, false);
	}

	@Override
	public ConcurrentBlock<OrderedPair<K, V>> leafBlock(long tag) {
		if ((tag & 1) != 0) {
			throw new IllegalArgumentException();
		}
		BlockLock rawBlock = space().get(tag >>> 1);
		return ConcurrentBlock.on(bTree.leafInjection(), rawBlock);
	}
	
	@Override
	public Stream<OrderedPair<K,V>> next(Optional<K> key) {
		return splitState.retryGet(root -> next(root, key)).orElse(Stream.empty());
	}
	
	@Override
	public Stream<OrderedPair<K,V>> next(RealNode<K,V> child) {
		return Stream.empty();
	}
	
	@Override
	public Optional<K> trySplit(Optional<K> startKey, Optional<K> endKey) {
		return splitState.retryGet(root -> trySplit(root, startKey, endKey)).orElse(Optional.empty());
	}
	
	private Optional<K> trySplit(RealNode<K,V> root, Optional<K> startKey, Optional<K> endKey) throws SplitException {
		return root.trySplit(startKey, endKey);
	}
	
	@Override
	public RealNode<K, V> node(Branch<K,V> parent, long tag) {
		if ((tag & 1) == 0) {
			return new LeafNode<>(parent, tag);
		} else {
			return new BranchNode<>(parent, tag);
		}
	}

	@Override
	public void put(K key, V value) {
		splitState.retryPut( root -> put(root, key, value));
	}

	@Override
	public void refresh(long tag) {
		space().refresh(tag>>>1);
	}

	@Override
	public void remove(K key) {
		splitState.retryPut( root -> remove(root,key));
	}
	
	@Override
	public void remove(RealNode<K,V> child) throws SplitException {
		clearRoot();
		counters().increment(BTreeCounters.LEVELDECREASES);
	}
	
	@Override
	public void split(SplitAction<K,V> action) throws SplitException {
		splitState.newRoot(Optional.of(action.existingNode()), root -> {
			action.prepare();
			action.commit();			
			long rootTag = allocateBranch(); 	
			RealNode<K,V> newRoot = new BranchNode<> (this, rootTag, action.existingNode().tag(), action.newNode());
			newRoot.put(action.reason().key(), action.reason().value());
			return Optional.of(newRoot);
		});
		counters().increment(BTreeCounters.LEVELINCREASES);
	}

	@Override 
	public Stream<OrderedPair<K,V>> start(Optional<K> key) {
		return splitState.retryGet(root -> root.start(key)).orElse(Stream.empty());
	}
	
	private void clearRoot() throws SplitException {
		splitState.newRoot(splitState.root(), root -> Optional.empty());		
	}
	
	private V get(RealNode<K,V> root, K key) throws SplitException {
		return root.get(key).orElse(null);
	}
	
	private Stream<OrderedPair<K,V>> next(RealNode<K,V> root, Optional<K> key) throws SplitException {
		return root.next(key);
	}
	
	private void put (Optional<RealNode<K,V>> root , K key, V value) throws SplitException {
		if (root.isPresent()) {
			root.get().put(key, value);
		} else {
			splitState.newRoot(root , r -> Optional.of(new LeafNode<>(this, allocateLeaf(), OrderedPair.of(key,value))));
			counters().increment(BTreeCounters.LEVELINCREASES).increment(BTreeCounters.INSERTS);
		}
	}
	
	private void remove(Optional<RealNode<K,V>> root, K key)  throws SplitException {
		if (root.isPresent()) {
			root.get().remove(key);
		}
	}
	
	private ConcurrentBlockSpace space() {
		return bTree.space();
	}

	private class BTreeSplitIterator extends Spliterators.AbstractSpliterator<OrderedPair<K,V>> {
		
		private static final int CHARACTERISTICS = ORDERED | DISTINCT | NONNULL | SORTED | CONCURRENT;
		private Optional<K> startKey;
		private Optional<K> endKey;
		private Spliterator<OrderedPair<K,V>> spliterator;

		BTreeSplitIterator(Optional<K> startKey, Optional<K> endKey) {
			super(Long.MAX_VALUE, CHARACTERISTICS);
			this.startKey = startKey;
			this.endKey = endKey;
			spliterator = start(startKey).filter(this::filter).spliterator();
		}
		
		BTreeSplitIterator(BTreeSplitIterator splitee , K splitKey) {
			super(Long.MAX_VALUE, CHARACTERISTICS);
			this.startKey = splitee.startKey;
			this.endKey = Optional.of(splitKey);
			this.spliterator = splitee.spliterator;
		}
		
		@Override
		public Comparator<OrderedPair<K,V>> getComparator() {
			return Comparator.comparing(OrderedPair::key, bTree.comparator());
		}
	
		private void setLastKey(OrderedPair<K,V> pair) {
			this.startKey = Optional.of(pair.key());
		}
		
		private boolean filter(OrderedPair<K,V> pair) {
			return !endKey.filter(key -> bTree.comparator().compare(pair.key(), key) >= 0).isPresent();
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super OrderedPair<K,V>> action) {
			Consumer<OrderedPair<K,V>> totalAction = this::setLastKey;
			totalAction = totalAction.andThen(action);
			if (spliterator.tryAdvance(totalAction)) {
					return true;
			}
			spliterator = next(startKey).filter(this::filter).spliterator();
			return spliterator.tryAdvance(totalAction);
		}

		@Override
		public Spliterator<OrderedPair<K,V>> trySplit() {
			Optional<K> splitKey = RootNode.this.trySplit(startKey, endKey);
			if (splitKey.isPresent()) {			
				Spliterator<OrderedPair<K,V>> result = new BTreeSplitIterator(this, splitKey.get());
				this.startKey = splitKey;
				this.spliterator = start(splitKey).filter(this::filter).spliterator();
				return result;
			} else {
				return null;
			}
		}
	}
}
	
