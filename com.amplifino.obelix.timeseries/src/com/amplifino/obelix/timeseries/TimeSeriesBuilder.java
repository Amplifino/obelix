package com.amplifino.obelix.timeseries;

import java.time.Instant;

import com.amplifino.obelix.sequences.LongMixer;
import com.amplifino.obelix.sets.Injection;

public final class TimeSeriesBuilder {
	
	private int cellDuration;
	private int cellBits;      
	private int partitionBits;   // in slotDuration units, power of 2
	private int rangeBits;          //  in partitionSize units, power of 2
	private long valueInjection;
	
	private LongMixer mixer() {
		int seriesBits = 64 - rangeBits - partitionBits - cellBits;
		return LongMixer.builder()
				.fromValue( partitionBits , rangeBits)
				.fromKey( 0, seriesBits)
				.fromValue(0, partitionBits)
				.build();
	}
	
	private Injection<Instant, Long> injection(int cellDurationBits) {
		return Injection
				.<Instant, Long>mapper(instant -> instant.getEpochSecond() / cellDuration)
				.unmapper(value -> Instant.ofEpochSecond(value * cellDuration));
	}
	
}
