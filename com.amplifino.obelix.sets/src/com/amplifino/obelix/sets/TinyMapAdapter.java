package com.amplifino.obelix.sets;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
 
class TinyMapAdapter<K,V> implements Map<K,V> {
	private final InfiniteMap<K,V> map;
	private long size;
	private long changeCount;
	
	TinyMapAdapter(InfiniteMap<K,V> map) {
		this.map = map;
		long size = map.domain().count();
		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("map to big"); 
		}
	}

	@Override
	public int size() {
		return (int) size;
	}
	
	private void adjustSize(int delta) {
		size += delta;
		if (size < 0 || size > Integer.MAX_VALUE) {
			throw new IllegalStateException();
		}
		changeCount++;
	}
	
	private void increment() {
		adjustSize(1);
	}
	
	private void decrement() {
		adjustSize(-1);
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(Object key) {
		if (key == null) {
			return false;
		}
		try {
			return map.get((K) key).isPresent();
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public boolean containsValue(Object value) {
		return map.range().anyMatch(v -> Objects.equals(v,value));
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		if (key == null) {
			return null;
		}
		try {
			return map.get((K) key).orElse(null);
		} catch (ClassCastException e) {
			return null;
		}
	}

	@Override
	public V put(K key, V value) {
		V v = get(key);
		map.put(key, value);
		if (v == null) {
			increment();
		}
		return v;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		try {
			V v = get(key);
			map.remove((K) key);
			if (v != null) {
				decrement();
			}
			return v;
		} catch (ClassCastException e) {
			return null;
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()));
	}

	@Override
	public void clear() {
		map.domain().collect(Collectors.toList()).forEach(map::remove);
	}

	@Override
	public Set<K> keySet() {
		return new KeySet();
	}

	@Override
	public Collection<V> values() {
		return new ValueCollection();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}
	
	class KeySet extends AbstractSet<K> {

		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}

		@Override
		public int size() {
			return TinyMapAdapter.this.size();
		}
	}
	
	class KeyIterator implements Iterator<K> {
		
		private final Iterator<K> iterator = map.domain().collect(Collectors.toList()).iterator();
		private long changeCount = TinyMapAdapter.this.changeCount;
		private K current;
		
		@Override
		public boolean hasNext() {
			check();
			return iterator.hasNext();
		}

		@Override
		public K next() {
			check();
			current = iterator.next();
			return current;
		}
		
		@Override
		public void remove() {
			if (current == null) {
				throw new IllegalStateException();
			}
			check();
			TinyMapAdapter.this.remove(current);
			changeCount++;
			current = null;
		}
		
		private void check() {
			if (changeCount != TinyMapAdapter.this.changeCount) {
				throw new ConcurrentModificationException();
			}
		}
	}
	
	class ValueCollection extends AbstractCollection<V> {

		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}

		@Override
		public int size() {
			return TinyMapAdapter.this.size();
		}
		
	}
	
	class ValueIterator implements Iterator<V> {
		
		private final Iterator<K> iterator = new KeyIterator();
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public V next() {
			return map.get(iterator.next()).orElseThrow(ConcurrentModificationException::new);
		}
	}
	
	class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public int size() {
			return TinyMapAdapter.this.size();
		}
	}
	
	class EntryIterator implements Iterator<Map.Entry<K,V>> {
		
		private final Iterator<K> iterator = new KeyIterator();
		private Entry current;
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Map.Entry<K, V> next() {
			K k = iterator.next();
			current = map.get(k)
				.map(v -> new Entry(k, v))
				.orElseThrow(ConcurrentModificationException::new);
			return current;
		}
		
		@Override
		public void remove() {
			if (current == null) {
				throw new IllegalStateException();
			}
			iterator.remove();
		}
	}
	
	class Entry extends AbstractMap.SimpleEntry<K, V> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Entry(K key, V value) {
			super(key, value);
		}
		
		@Override
		public V setValue(V newValue) {
			V oldValue = super.setValue(newValue);
			map.put(getKey(), newValue);
			return oldValue;
		}
		
	}
	
}
