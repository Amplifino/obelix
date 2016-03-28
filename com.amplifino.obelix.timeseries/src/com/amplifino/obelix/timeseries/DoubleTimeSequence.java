package com.amplifino.obelix.timeseries;

import java.util.concurrent.TimeUnit;

import com.amplifino.obelix.sequences.Sequence;

public final class DoubleTimeSequence extends DefaultTimeSequence<Double> {

	public DoubleTimeSequence(Sequence<Double> sequence , TimeUnit timeUnit, int cellDuration) {
		super(sequence , timeUnit, cellDuration);
	}

}
