package com.amplifino.obelix.btrees;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.amplifino.counters.Counters;
import com.amplifino.obelix.pairs.LongValuePair;
import com.amplifino.obelix.pairs.OrderedPair;
import com.amplifino.obelix.stores.Block;

class BranchNode<K,V> extends AbstractNode<K,V> implements Branch<K,V> {
	
	private final ConcurrentBlock<LongValuePair<K>> lock;
	private final BranchSplitState<K> splitState;
	private final long tag;
	
	BranchNode(Branch<K,V> parent, long tag) {
		super(parent);
		this.tag = tag;
		this.lock = parent().branchBlock(tag);
		this.splitState = new BranchSplitState<>(lock, parent.counters());
	}
	
	private BranchNode(BranchNode<K,V> peer, long tag, Stream <LongValuePair<K>> entries) {
		this(peer.parent(), tag);
		try {
			lock.put(block -> entries.forEach( entry -> doAdd(block, entry)));
		} catch (SplitException e) {
			throw new IllegalStateException(e);
		}
	}
	
	BranchNode(Branch<K,V> parent, long tag, long firstTag ,  RealNode<K,V> second) {
		this(parent, tag);
		RealNode<K,V> first = parent().node(this, firstTag);
		second.parent(this);
		try {
			K firstFirstKey = first.firstKey();
			K secondFirstKey = second.firstKey();
			lock.put(block -> {
				doAdd(block, LongValuePair.of(firstFirstKey, first.tag()));
				doAdd(block, LongValuePair.of(secondFirstKey, second.tag()));
			});
		} catch (SplitException e) {
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public long allocateBranch() {
		return parent().allocateBranch();
	}

	@Override
	public long allocateLeaf() {
		return parent().allocateLeaf();
	}
	
	@Override
	public ConcurrentBlock<LongValuePair<K>> branchBlock(long tag) {
		return parent().branchBlock(tag);
	}	

	@Override
	public Counters<BTreeCounters> counters() {
		return parent().counters();
	}
	
	@Override
	public K firstKey() throws SplitException {
		return lock.get(block -> block.get(0).key());
	}
	
	@Override
	public void free(long tag) {
		parent().free(tag);
	}
	
	@Override
	public Optional<V> get(K key) throws SplitException {
		return splitState.retryGet(() -> node(key).get(key));		
	}
	
	@Override
	public ConcurrentBlock<OrderedPair<K, V>> leafBlock(long tag) {
		return parent().leafBlock(tag);
	}
	
	public Stream<OrderedPair<K,V>> next(Optional<K> key) throws SplitException {
		return splitState.retryGet(() -> node(key.get()).next(key));
	}
	
	public Optional<K> trySplit(Optional<K> startKey, Optional<K> endKey) throws SplitException {
		 Optional<K> result = lock.get(block -> trySplit(block, startKey, endKey));
		 if (result.isPresent()) {
			 return result;
		 } else {
			return splitState.retryGet(() -> node(startKey).trySplit(startKey, endKey));
		 }
	}
	
	public Stream<OrderedPair<K,V>> next(RealNode<K,V> child) throws SplitException {
		OptionalLong tag = lock.get(block -> next(block, child));
		if (tag.isPresent()) {
			return parent().node(this, tag.getAsLong()).start(Optional.empty());
		} else {
			return parent().next(this);
		}
	}
	
	@Override
	public RealNode<K, V> node(Branch<K,V> parent, long tag) {
		return parent().node(parent, tag);
	}

	@Override
	public void put(K key, V value) throws SplitException {
		splitState.retryPut(() -> node(key).put(key, value));
	}

	@Override
	public void refresh(long tag) {
		parent().refresh(tag);
	}
	
	@Override
	public void remove(K key) throws SplitException {
		splitState.retryPut(() -> node(key).remove(key));
	}
	
	@Override
	public void remove(RealNode<K,V> child) throws SplitException {
		lock.put(block -> remove(block, child));
	}
	
	@Override
	public BranchNode<K,V> split(int split) throws SplitException {
		Stream<LongValuePair<K>> toMove = lock.get( block -> spinOff(block, split));
		return new BranchNode<>(this, parent().allocateBranch() , toMove);
	}
	
	@Override
	public void split(SplitAction<K,V> action) throws SplitException {
		if (action.existingNode().parent() != this) {
			throw new IllegalArgumentException();
		}
		lock.put(block -> split(block, action));
	}
	
	@Override 
	public Stream<OrderedPair<K,V>> start(Optional<K> key) throws SplitException {
		return splitState.retryGet(() -> node(key).start(key));
	}

	@Override
	public long tag() {
		return tag;
	}

	@Override
	public void truncate(int split)  {
		parent().counters().increment(BTreeCounters.BRANCHSPLITS);
		lock.truncate(split);		
	}

	private final void doAdd(Block<LongValuePair<K>> block, LongValuePair<K> pair) {
		if (!block.add(pair)) {
			throw new IllegalStateException();
		}
	}

	private long get(Block<LongValuePair<K>> block, int index) {
		return block.get(index).value();
	}

	private int index(Block<LongValuePair<K>> block, RealNode<K,V> child) {		
		for (int i = 0 ; i < block.size(); i++) {
			LongValuePair<K> pair = block.get(i);
			if (pair.value() == child.tag()) {
				return i;
			}
		}
		throw new IllegalArgumentException();
	}

	private OptionalLong next(Block<LongValuePair<K>> block, RealNode<K,V> child)  {
		int index = index(block, child);
		if (index < block.size() - 1) {
			return OptionalLong.of(get(block, index + 1));
		} else {
			return OptionalLong.empty();
		}
	}

	private Node<K,V> node(Optional<K> key) throws SplitException {
		if (key.isPresent()) {
			return node(key.get());
		} else {
			return lock.get(block -> parent().node(this, block.get(0).value()));
		}
	}
	private Node<K,V> node(K key) throws SplitException {
		return lock.get(block -> node(block, key));
	}
	
	private Node<K,V> node(Block<LongValuePair<K>> block,K key)  {
		return parent().node(this, tag(block, key));
	}
	
	private void remove(Block<LongValuePair<K>> block, RealNode<K,V> child) throws SplitException {
		boolean empty = block.size() == 1;
		if (empty) {		
			parent().remove(this);
			lock.invalidate();
		}
		block.remove(index(block, child));
		if (empty) {
			parent().free(tag);
		}
	}
	
	private Stream<LongValuePair<K>> spinOff(Block<LongValuePair<K>> block, int split) {
		lock.invalidate();
		return IntStream.range(split, block.size()).mapToObj(block::get);
	}

	private void split(Block<LongValuePair<K>> block, SplitAction<K,V> splitAction) throws SplitException {
		LongValuePair<K> tryEntry = LongValuePair.of(splitAction.splitKey(), Long.MAX_VALUE);
		if (!block.canTake(tryEntry)) {
			forwardSplit(block, splitAction);
			return;
		}
		LongValuePair<K> newEntry = splitAction.prepare();
		if (block.add(index(block, splitAction.existingNode()) + 1, newEntry)) {
			splitAction.commit();
			OrderedPair<K,V> reason = splitAction.reason();
			node(block, reason.key()).put(reason.key(), reason.value());			
		} else {
			splitAction.undo();
			forwardSplit(block, splitAction);
		}
	}
	
	private void forwardSplit(Block<LongValuePair<K>> block, SplitAction<K,V> splitAction) throws SplitException {
		int split = block.size() / 2;
		parent().split(new SplitAction<>(this, split , block.get(split).key(), splitAction.reason()));
	}
	
	private long tag(Block<LongValuePair<K>> block, K key) {
		return block.get(index(block, key)).value();
	}
	
	private int index (Block<LongValuePair<K>> block, K key) {
		if (block.isEmpty()) {
			throw new IllegalStateException("" + key);
		} 
		int low = 1;
        int high = block.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            LongValuePair<K> pair = block.get(mid);
            int cmp = parent().compare(pair.key(), key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return low - 1;
    }
	
	private Optional<K> trySplit(Block<LongValuePair<K>> block, Optional<K> startKey, Optional<K> endKey) {
		if ((block.get(0).value() & 1) == 0) {
			return Optional.empty();
		}
		int start = startKey.map(key -> index(block, key)).orElse(0);
		int end = endKey.map(key -> index(block, key)).orElse(block.size() - 1);
		if (start == end) {
			return Optional.empty();
		}
		if (end < start) {
			throw new IllegalStateException();
		}
		int split = (start + end + 1) >>> 1;
		K splitKey = block.get(split).key();
		if (endKey.isPresent()) {
			if (compare(splitKey, endKey.get()) > 0 ) {
				throw new IllegalStateException();
			}
		}
		if (!endKey.filter( k -> compare(splitKey, k) ==  0).isPresent()) {
			return Optional.of(splitKey);
		} else {
			return Optional.empty();
		}
	}
}
