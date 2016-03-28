package com.amplifino.obelix.pairs;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

/**
 * 
 * OrderedPair represents an element of a graph 
 *
 * @param <K> the type of elements of the domain
 * @param <V> the type of elements of the range
 */
@ProviderType
public interface OrderedPair<K,V>  {
	/**
	 * returns this pair's domain element
	 * @return the domain element
	 */
	K key();
	
	/**
	 * returns this pair's range element
	 * @return the range element
	 */
	V value();
	
	/**
	 * returns a new pair whose key is the current key, and whose value is the new value
	 * @param newValue the new value
	 * @param <T> the type of the new pair's second element
	 * @return the new pair
	 */
	<T> OrderedPair<K, T> with(T newValue);
	
	/**
	 * returns a {@code Map.Entry<K,V> } equivalent to the receiver
	 * @return the entry
	 */
	default Map.Entry<K, V> asEntry() {
		return new AbstractMap.SimpleImmutableEntry<>(key(),value());
	}
	
	/**
	 * returns a pair consisting of the arguments
	 * @param key the pair's key
	 * @param value the pair's value
	 * @param <K> the type of the pair's first element
	 * @param <V> the type of the pair's second element
	 * @return the new pair
	 */
	static <K,V> OrderedPair<K,V> of(K key, V value) {
		return new DefaultOrderedPair<>(key, value);
	}

	/**
	 * returns a pair based on the argument
	 * @param entry the source
	 * @param <K> the type of the pair's first element
	 * @param <V> the type of the pair's second element
	 * @return the new pair
	 */
	static <K,V> OrderedPair<K,V> of(Map.Entry<K, V> entry) {
		return new MapEntryPair<>(entry);
	}
	
	/**
	 * returns a function that when applied to the domain will generate the graph of the argument
	 * @param generator the base function
	 * @param <K> the type of elements of the argument's source set
	 * @param <V> the type of elements of the argument's target set
	 * @return the wrapped function
	 */
	static <K,V> Function<K, OrderedPair<K,V>> graph(Function<? super K, ? extends V> generator) {
		return key -> OrderedPair.of(key, generator.apply(key));
	}
	
	static <K,V> Function<V, OrderedPair<K,V>> withKey(K key) {
		return value -> OrderedPair.of(key, value);
	}
	
	/**
	 * returns a function that generates a Stream of OrderedPairs from a Stream of values
	 * 
	 * <p>If {@code orders} is a stream of purchase orders, and each purchase
     * order contains a collection of line items, then the following produces a
     * stream containing an OrderPair with Order as first element and line item as second
     * <pre>{@code
     *     orders.flatMap(order -> OrderedPair.flatten( order -> order.getLineItems().stream())...
     * }</pre>
     *
	 * @param generator the base function
	 * @param <K> the type of the pair's first element
	 * @param <V> the type of the pair's second element
	 * @return the wrapped function
	 */
	static <K,V> Function<K, Stream<OrderedPair<K,V>>> flatten(Function<? super K, Stream<? extends V>> generator) {
		return key -> generator.apply(key).map(value -> OrderedPair.of(key,value));
	}
	
	/**
	 * returns a function that generates a Stream of OrderedPairs from a Collection generating function
	 * 	  
	 * <p>If {@code orders} is a stream of purchase orders, and each purchase
     * order contains a collection of line items, then the following produces a
     * stream containing an OrderPair with Order as first element and line item as second
     * <pre>{@code
     *     orders.flatMap(OrderedPair.stream(Order::getLineItems))...
     * }</pre>
     * or combined with mapValue to obtain Order Product pairs:
     * <pre>{@code
     *     orders.flatMap(OrderedPair.stream(Order::getLineItems)).map(OrderedPair.mapValue(LineItem::getProduct))
     * }</pre>
     * @param generator the base function
     * @param <K> the type of the pair's first element
	 * @param <V> the type of the pair's second element
	 * @return the wrapped function
	 */
	static <K,V> Function<K, Stream<OrderedPair<K,V>>> stream(Function<? super K, Collection<? extends V>> generator) {
		return flatten(generator.andThen(Collection::stream));
	}
	
	static<K,V,T> Function<OrderedPair<K,V>, T> mapPair(BiFunction<K, V, T> biFunction) {
		return pair -> biFunction.apply(pair.key(), pair.value());
	}
	
	static<K,V,T> Function<OrderedPair<K,V>, OrderedPair<T,V>> mapKey(Function<K, T> mapper) {
		return pair -> OrderedPair.of(mapper.apply(pair.key()), pair.value());
	}
	
	static<K,V,T> Function<OrderedPair<K,V>, OrderedPair<K,T>> mapValue(Function<V, T> mapper) {
		return pair -> OrderedPair.of(pair.key(), mapper.apply(pair.value()));
	}
	
	static <K,V> Predicate<OrderedPair<K,V>> filterPair(BiPredicate<K, V> predicate) {
		return pair -> predicate.test(pair.key(), pair.value());
	}
	
	static <K,V> Predicate<OrderedPair<K,V>> filterKey(Predicate<K> predicate) {
		return pair -> predicate.test(pair.key());
	}
	
	static <K,V> Predicate<OrderedPair<K,V>> filterValue(Predicate<V> predicate) {
		return pair -> predicate.test(pair.value());
	}
}
