package com.amplifino.obelix.sequences;

import java.util.ArrayList;
import java.util.List;

public final class LongMixer {
	
	private final MixStep[] steps;
	
	private LongMixer(MixStep[] steps) {
		this.steps = steps;
	}
	
	public long mix(long key, long value) {
		long result = 0;
		for (int i = 0 ; i < steps.length ; i++) {
			result = steps[i].apply(result , steps[i].fromKey ? key : value);				
		}
		return result;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	private static class MixStep {
		private boolean fromKey;
		private int offset;
		private int length;
		private long mask;
		
		private MixStep(boolean fromKey, int offset , int length ) {
			this.fromKey = fromKey;
			this.offset = offset;
			this.length = length;
			this.mask = (1L << length) - 1;
		}
		
		long apply(long buffer , long v) {
			return (buffer <<= length) | ((v >>> offset) & mask);
		}
		
		int length() {
			return length;
		}
	}
	
	public static class Builder {
		private final List<MixStep> steps = new ArrayList<>();
		private int runLength;
		
		private void add(MixStep step) {
			runLength += step.length();
			if (runLength > 64) {
				throw new IllegalStateException();
			}
			steps.add(step);
 		}
		
		public Builder fromKey(int offset, int length) {
			add(new MixStep(true, offset, length));
			return this;
		}
		
		public Builder fromValue(int offset, int length) {
			add(new MixStep(false, offset, length));
			return this;
		}
		
		public LongMixer build() {
			return new LongMixer(steps.toArray(new MixStep[steps.size()]));
		}
	}


}
