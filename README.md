# Project Obelix #

Leverage Java 8 parallel streams on persistent data structures.

The core of the project is com.amplifino.obelix.space that provides uniform access to a 64 bit byte address space.
The space can be backed by:

- heap memory (mainly used for testing)
- offheap memory
- memory mapped files (sparse if supported by OS and file system )
- regular files (sparse if supported by OS and file system)

Byte spaces that exceed theoretical or pratical file size limit, 
can be backed by a directory and use multiple files as backing storage.

The second essential part is com.amplifino.obelix.sets that provides high level abstractions based on mathematical set theory.
The abstraction are based in object space, to convert from object space to byte space, **injections** are used.

The other bundles are implementations of the abstractions in sets on top of a bytespace.

A direct useful artifact is com.amplifino.btrees that provides a concurrent, parallel implementation of a btree.

- **concurrent:** Multiple threads can modify the btree at the same time
- **parallel:** provides parallel streams for iterating over a key range.

All subprojects are OSGI bundles, but can also be used in a non OSGI environment.

Eclipse Projects:

- **cnf**: Bndtools Bundle repository
- **com.amplifino.obelix.btrees:** a concurrent and parallel BTree implementation
- **com.amplifino.obelix.guards:** locks arbitrary long values.
- **com.amplifino.obelix.indexes:** an index implementation on top of a SortedMap.
- **com.amplifino.obelix.injections:** conversions between objects and byte arrays
- **com.amplifino.obelix.maps:** various InfiniteMap implementations
- **com.amplifino.obelix.segments:** implementation of a byte store
- **com.amplifino.obelix.sequences:** map sequences to byte space
- **com.amplifino.obelix.sets:** basic abstractions for persistent data structures
- **com.amplifino.obelix.sortedmaps:** sorted maps
- **com.amplifino.obelix.space:** byte spaces
- **com.amplifino.obelix.stores:** generic store abstractions.
- **com.amplifino.obelix.timeseries:** ultra fast timeseries using direct addressing from time space to byte space.
