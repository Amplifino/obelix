package com.amplifino.obelix.sets;

import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

import com.amplifino.obelix.pairs.OrderedPair;

/**
 * A Binary Relation
 * 
 * <p>A Relation is a mathematical concept in set theory defined by an ordered triplet of sets:  (source set, target set, graph set).
 * Definitions: 
 * <ul>
 * <li>The graph is the set of all ordered pairs that define the relation.</li>
 * <li>The domain of a relation is the subset of the source set that participates in the relation.</li>
 * <li>The range of a relation is the subset of the target set that participates in the relation.</li>
 * </ul>
 *
 * @param <K> type of elements of the source set
 * @param <V> type of elements of the target set
 */
@ProviderType
public interface BinaryRelation<K, V> {
	
	/**
	 * returns the subset of the relation's graph when limiting the domain to the singleton containing the argument.
	 * @param key
	 * @return
	 */
	Stream<? extends OrderedPair<K, V>> graph(K key);
	
	/**
	 * returns the graph set
	 * @return a stream representing the graph set
	 */
	Stream<? extends OrderedPair<K, V>> graph();
	
	/**
	 * returns the subset of the relation's range for which (<code>key</code>,v) is an element of graph set
	 * 
	 * @param key the source element
	 * @return a stream representing the range set 
	 */
	default Stream<V> range(K key) {
		return graph(key).map(OrderedPair::value);
	}
	
	/**
	 * returns the graph subset obtained by restricting the domain to its intersection with the argument
	 * @param subset the domain subset
	 * @return a stream representing the graph subset
	 */
	default Stream<? extends OrderedPair<? extends K,? extends V>> graph (Stream<K> subset ) {
		return subset.flatMap(this::graph);
	}

	/**
	 * returns the domain of this relation
	 * @return a stream representing the domain 
	 */
	default Stream<K> domain() {
		return graph().map(OrderedPair::key);
	}
	
	/**
	 * returns the range of this relation
	 * @return a stream representing the range
	 */
	default Stream<V> range() {
		return graph().map(OrderedPair::value);
	}
}
