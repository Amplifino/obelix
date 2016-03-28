/**
 * Sets
 * 
 * <p>This packages provides basic java interfaces for set theory
 * </p>
 * 
 * <p>Mathematical Sets are not explicitly modelled in this package.
 * They are generally represented by a Stream. 
 * </p> 
 * <p>The main package focus is on Relations.
 * </p>
 * <p>
 * A relation is defined as the ordered triplet (source, target, graph)
 * The graph is a subset of the cardinal product of source and target
 * </p>
 * <p>
 * Each relation defines the following methods
 * </p>
 * <p>
 * domain: subset of source for which the relation is defined
 * range: subset of target that is covered by the relation
 * graph: set of OrderedPairs that define the relation
 * </p>
 */

@Version("1.0.0")
package com.amplifino.obelix.sets;

import org.osgi.annotation.versioning.*;

