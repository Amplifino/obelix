package com.amplifino.obelix.segments;

import com.amplifino.obelix.stores.Store;

/**
 * Represents a byte store with a Segment Header
 *
 */
public interface RecordSpace extends Store<byte[]> , Segment {
}
