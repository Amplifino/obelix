package com.amplifino.obelix.sequences;

import org.junit.Test;
import static org.junit.Assert.*;

public class LongMixerTest {

	@Test
	public void test() {
		LongMixer mixer = LongMixer.builder().fromKey(8, 16).fromValue(0, 32).fromKey(0, 8).build();
		assertEquals(0xFFFF00000000FFL, mixer.mix(-1, 0));
		assertEquals(0x33335555555533L, mixer.mix(0x33333333L, 0x55555555L));
		assertEquals(0x83338555555533L, mixer.mix(0x93833333L, 0x85555555L));
	}
}
