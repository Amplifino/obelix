package com.amplifino.obelix.segments;

import java.util.Arrays;
import java.util.function.Function;

public enum SegmentTypes  {
	
	SIMPLE(Visitor::visitSimple),
	NOREUSE(Visitor::visitNoReuse),
	FREELIST(Visitor::visitFreeList);
	
	private final Function<Visitor<?>, Object> visitAction;
		
	private SegmentTypes(Function<Visitor<?>, Object> action) {
		this.visitAction = action; 
	}
	
	@SuppressWarnings("unchecked")
	<T> T visit(Visitor<T> visitor) {
		return (T) visitAction.apply(visitor);
	}
	
	short tag() {
		return (short) (ordinal() + 1);
	}
	
	static SegmentTypes tag(short tag) {
		return Arrays.stream(values())
			.filter(type -> type.tag() == tag)
			.findFirst()
			.orElseThrow(IllegalArgumentException::new);
	}
	
	interface Visitor<T> {
		T visitSimple();
		T visitNoReuse();
		T visitFreeList();
	}
}
