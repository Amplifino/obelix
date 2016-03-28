package com.amplifino.obelix.maps;

import java.util.function.Function;
import java.util.Arrays;

public enum MapTypes {
	/**
	 * O(n) for add, update and remove
	 */
	PAIRSEGMENT(Visitor::visitPairSegment),
	/**
	 * O(n) for add, update and remove
	 * Better for large values
	 */
	VALUESEGMENTWITHKEYINDEX(Visitor::visitValueSegmentWithKeyIndex),
	/**
	 * O(1) for add, update and remove for small load factors
	 * O(n) when load factor approaches 1
	 */
	PAIRSEGMENTWITHSYNONYMHASH(Visitor::visitPairSegmentWithSynonymHash),
	/**
	 * O(1) for add, update and remove for small load factors
	 * O(load factor) for larger load factors;
	 */
	PAIRSEGMENTWITHBINHASH(Visitor::visitPairSegmentWithBinHash);
	
	private final Function<Visitor<?>, Object> visitAction;
	
	MapTypes(Function<Visitor<?>,Object> action) {
		this.visitAction = action;
	}
	
	@SuppressWarnings("unchecked")
	<T> T visit(Visitor<T> visitor) {
		return (T) visitAction.apply(visitor);
	}
	
	short tag() {
		return (short) (ordinal() + 1);
	}

	static MapTypes tag(short tag) {
		return Arrays.stream(values())
			.filter(type -> type.tag() == tag)
			.findFirst()
			.orElseThrow(IllegalArgumentException::new);
	}
	
	interface Visitor<T>  {
		T visitPairSegment();
		T visitValueSegmentWithKeyIndex();
		T visitPairSegmentWithSynonymHash();
		T visitPairSegmentWithBinHash();
	}
}
