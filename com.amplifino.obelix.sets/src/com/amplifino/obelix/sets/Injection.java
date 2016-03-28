package com.amplifino.obelix.sets;

import java.util.Objects;
import java.util.function.Function;

import org.osgi.annotation.versioning.ConsumerType;
/**
 * represents an Injection
 * 
 * <p>an Injection or injective function is a function whose inverse is also a function.
 * or in math  f(x) = f(y) implies x = y </p>
 * 
 * <p> In the method documentation we will use y = f(x) for the function and
 * y = finverse(x) for the inverted function
 *  
 * @param <S> the type of elements of the source set
 * @param <T> the type of elements of the target set
 */
@ConsumerType
public interface Injection<S,T> {
	
	/**
	 * returns f(in)
	 * @param the function argument
	 * @return the function result
	 */
	T map(S in);
	/**
	 * applies the inverse function to the argument y = finverse(x)
	 * @param in the inverse function argument
	 * @return the inverse function result
	 */
	S unmap(T in);
	
	/**
	 * Returns the injection obtained by applying the receiver first and then the argument
	 * @param second injection
	 * @param <FT> the target type of the returned injection
	 * @return the composed injection
	 */
	default <FT> Injection<S,FT> andThen(Injection<T,FT> second)  {
		return new ComposedInjection<>(this, second);
	}
	
	/**
	 * Returns the injection obtained by first applying the argument and then the receiver
	 * @param first injection
	 * @param <IS> the source type of the returned injection
	 * @return the composed injection
	 */
	default <IS> Injection<IS,T> compose(Injection<IS,S> first)  {
		return new ComposedInjection<>(first, this);
	}
	
	/**
	 * returns an injection builder
	 * @param serializer the injection function
	 * @return a builder
	 */
	static <S,T> Builder<S,T> mapper(Function<? super S,? extends T> serializer) {
		return new Builder<>(Objects.requireNonNull(serializer));
	}
	
	/**
	 * Returns an Identity Injection
	 * @param type domain element type
	 * @return the identity Injection
	 */
	static <S> Injection<S,S> identity(Class<S> type) {
		return mapper(Function.<S>identity()).unmapper(Function.identity());
	}
	
	/**
	 * Injection Builder
	 *
	 * @param <S> the domain element type
	 * @param <T> the target element type
	 */
	final class Builder<S,T> {
		
		private final Function<? super S,? extends T> serializer;
		
		private Builder(Function<? super S, ? extends T> serializer) {
			this.serializer = serializer;
		}
		/**
		 * return a new injection
		 * @param deserializer the inverse function
		 * @return the new injection
		 */
		public FunctionInjection<S,T> unmapper(Function<? super T,? extends S> deserializer) {
			return new FunctionInjection<>(serializer, Objects.requireNonNull(deserializer));
		}
	}
	
	/**
	 * Default injection implementation
	 * 
	 */
	final class FunctionInjection<S,T> implements Injection<S,T> {
		private final Function<? super S, ? extends T> serializer;
		private final Function<? super T, ? extends S> deserializer;
		
		private FunctionInjection(Function<? super S,? extends T> serializer, Function<? super T,? extends S> deserializer) {
			this.serializer = serializer;
			this.deserializer = deserializer;
		}
		
		@Override
		public T map(S in) {
			return serializer.apply(in);
		}
		@Override
		public S unmap(T in) {
			return deserializer.apply(in);
		}
	}
	
	/**
	 * Composed Injection Implementation
	 */
	final class ComposedInjection<S, T, IS> implements Injection<S,T> {
		private final Injection<S, IS> first;
		private final Injection<IS, T> second;
		
		private ComposedInjection(Injection<S,IS> first, Injection<IS,T> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public T map(S in) {
			return second.map(first.map(in));
		}

		@Override
		public S unmap(T in) {
			return first.unmap(second.unmap(in));
		}

	}
	
}
