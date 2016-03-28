package com.amplifino.obelix.sortedmaps;

import java.util.function.Function;
import java.util.Arrays;

public enum SortedMapTypes {
	/**
	 * O(log(n)) for add, update and remove
	 */
	BTREE(Visitor::visitBTree),
	VALUESEGMENTWITHBTREEINDEX(Visitor::visitValueSegmentWithBTreeIndex);
	
	private final Function<Visitor<?>, Object> visitAction;
	
	SortedMapTypes(Function<Visitor<?>,Object> action) {
		this.visitAction = action;
	}
	
	@SuppressWarnings("unchecked")
	<T> T visit(Visitor<T> visitor) {
		return (T) visitAction.apply(visitor);
	}
	
	short tag() {
		return (short) (ordinal() + 256);
	}

	static SortedMapTypes tag(short tag) {
		return Arrays.stream(values())
			.filter(type -> type.tag() == tag)
			.findFirst()
			.orElseThrow(IllegalArgumentException::new);
	}
	
	interface Visitor<T>  {
		T visitBTree();
		T visitValueSegmentWithBTreeIndex();
	}
}
