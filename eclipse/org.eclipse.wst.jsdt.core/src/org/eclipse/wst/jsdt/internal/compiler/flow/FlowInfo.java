/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.compiler.flow;

import org.eclipse.wst.jsdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;

public abstract class FlowInfo {

	public int tagBits; // REACHABLE by default
	public final static int REACHABLE = 0;
	public final static int UNREACHABLE = 1;
	public final static int NULL_FLAG_MASK = 2;

	public final static int UNKNOWN = 0;
	public final static int NULL = 1;
	public final static int NON_NULL = -1;

	public static final UnconditionalFlowInfo DEAD_END; // Represents a dead branch status of initialization
	static {
		DEAD_END = new UnconditionalFlowInfo();
		DEAD_END.tagBits = UNREACHABLE;
	}

/**
 * Add other inits to this flow info, then return this. The operation semantics
 * are to match as closely as possible the application to this flow info of all
 * the operations that resulted into otherInits.
 * @param otherInits other inits to add to this
 * @return this, modified according to otherInits information
 */
abstract public FlowInfo addInitializationsFrom(FlowInfo otherInits);


/**
 * Compose other inits over this flow info, then return this. The operation
 * semantics are to wave into this flow info the consequences of a possible
 * path into the operations that resulted into otherInits. The fact that this
 * path may be left unexecuted under peculiar conditions results into less
 * specific results than {@link #addInitializationsFrom(FlowInfo)
 * addInitializationsFrom}.
 * @param otherInits other inits to compose over this
 * @return this, modified according to otherInits information
 */
abstract public FlowInfo addPotentialInitializationsFrom(FlowInfo otherInits);

	public FlowInfo asNegatedCondition() {

		return this;
	}

	public static FlowInfo conditional(FlowInfo initsWhenTrue, FlowInfo initsWhenFalse){

		// if (initsWhenTrue.equals(initsWhenFalse)) return initsWhenTrue; -- could optimize if #equals is defined
		return new ConditionalFlowInfo(initsWhenTrue, initsWhenFalse);
	}

/**
 * Check whether a given local variable is known to be unable to gain a definite
 * non null or definite null status by the use of an enclosing flow info. The
 * semantics are that if the current flow info marks the variable as potentially
 * unknown or else as being both potentially null and potentially non null,
 * then it won't ever be promoted as definitely null or definitely non null. (It
 * could still get promoted to definite unknown).
 * @param local the variable to ckeck
 * @return true iff this flow info prevents local from being promoted to
 *         definite non null or definite null against an enclosing flow info
 */
public boolean cannotBeDefinitelyNullOrNonNull(LocalVariableBinding local) {
	return isPotentiallyUnknown(local) ||
		isPotentiallyNonNull(local) && isPotentiallyNull(local);
}

/**
 * Check whether a given local variable is known to be non null, either because
 * it is definitely non null, or because is has been tested against non null.
 * @param local the variable to ckeck
 * @return true iff local cannot be null for this flow info
 */
public boolean cannotBeNull(LocalVariableBinding local) {
	return isDefinitelyNonNull(local) || isProtectedNonNull(local);
}

/**
 * Check whether a given local variable is known to be null, either because it
 * is definitely null, or because is has been tested against null.
 * @param local the variable to ckeck
 * @return true iff local can only be null for this flow info
 */
public boolean canOnlyBeNull(LocalVariableBinding local) {
	return isDefinitelyNull(local) || isProtectedNull(local);
}

/**
 * Return a deep copy of the current instance.
 * @return a deep copy of this flow info
 */
	abstract public FlowInfo copy();

	public static UnconditionalFlowInfo initial(int maxFieldCount) {
		UnconditionalFlowInfo info = new UnconditionalFlowInfo();
		info.maxFieldCount = maxFieldCount;
		return info;
	}

/**
 * Return the flow info that would result from the path associated to the
 * value false for the condition expression that generated this flow info.
 * May be this flow info if it is not an instance of {@link
 * ConditionalFlowInfo}. May have a side effect on subparts of this flow
 * info (subtrees get merged).
 * @return the flow info associated to the false branch of the condition
 * 			that generated this flow info
 */
abstract public FlowInfo initsWhenFalse();

/**
 * Return the flow info that would result from the path associated to the
 * value true for the condition expression that generated this flow info.
 * May be this flow info if it is not an instance of {@link
 * ConditionalFlowInfo}. May have a side effect on subparts of this flow
 * info (subtrees get merged).
 * @return the flow info associated to the true branch of the condition
 * 			that generated this flow info
 */
	abstract public FlowInfo initsWhenTrue();

	/**
	 * Check status of definite assignment for a field.
	 */
	 abstract public boolean isDefinitelyAssigned(FieldBinding field);

	/**
	 * Check status of definite assignment for a local.
	 */
	public abstract boolean isDefinitelyAssigned(LocalVariableBinding local);

/**
 * Check status of definite non-null value for a given local variable.
 * @param local the variable to ckeck
 * @return true iff local is definitely non null for this flow info
 */
	public abstract boolean isDefinitelyNonNull(LocalVariableBinding local);

/**
 * Check status of definite null value for a given local variable.
 * @param local the variable to ckeck
 * @return true iff local is definitely null for this flow info
 */
public abstract boolean isDefinitelyNull(LocalVariableBinding local);

/**
 * Check status of definite unknown value for a given local variable.
 * @param local the variable to ckeck
 * @return true iff local is definitely unknown for this flow info
 */
public abstract boolean isDefinitelyUnknown(LocalVariableBinding local);

	/**
	 * Check status of potential assignment for a field.
	 */
	 abstract public boolean isPotentiallyAssigned(FieldBinding field);

	/**
	 * Check status of potential assignment for a local variable.
	 */

	 abstract public boolean isPotentiallyAssigned(LocalVariableBinding field);

/**
 * Check status of potential null assignment for a local. Return true if there
 * is a reasonable expectation that the variable be non null at this point.
 * @param local LocalVariableBinding - the binding for the checked local
 * @return true if there is a reasonable expectation that local be non null at
 * this point
 */
public abstract boolean isPotentiallyNonNull(LocalVariableBinding local);

/**
 * Check status of potential null assignment for a local. Return true if there
 * is a reasonable expectation that the variable be null at this point. This
 * includes the protected null case, so as to augment diagnostics, but does not
 * really check that someone deliberately assigned to null on any specific
 * path
 * @param local LocalVariableBinding - the binding for the checked local
 * @return true if there is a reasonable expectation that local be null at
 * this point
 */
public abstract boolean isPotentiallyNull(LocalVariableBinding local);

/**
 * Return true if the given local may have been assigned to an unknown value.
 * @param local the local to check
 * @return true if the given local may have been assigned to an unknown value
 */
public abstract boolean isPotentiallyUnknown(LocalVariableBinding local);

/**
 * Return true if the given local is protected by a test against a non null
 * value.
 * @param local the local to check
 * @return true if the given local is protected by a test against a non null
 */
public abstract boolean isProtectedNonNull(LocalVariableBinding local);

/**
 * Return true if the given local is protected by a test against null.
 * @param local the local to check
 * @return true if the given local is protected by a test against null
 */
public abstract boolean isProtectedNull(LocalVariableBinding local);

/**
 * Record that a local variable got checked to be non null.
 * @param local the checked local variable
 */
abstract public void markAsComparedEqualToNonNull(LocalVariableBinding local);

/**
 * Record that a local variable got checked to be null.
 * @param local the checked local variable
 */
abstract public void markAsComparedEqualToNull(LocalVariableBinding local);

	/**
	 * Record a field got definitely assigned.
	 */
	abstract public void markAsDefinitelyAssigned(FieldBinding field);

	/**
	 * Record a local got definitely assigned to a non-null value.
	 */
	abstract public void markAsDefinitelyNonNull(LocalVariableBinding local);

	/**
	 * Record a local got definitely assigned to null.
	 */
	abstract public void markAsDefinitelyNull(LocalVariableBinding local);

	/**
	 * Record a local got definitely assigned.
	 */
	abstract public void markAsDefinitelyAssigned(LocalVariableBinding local);

/**
 * Record a local got definitely assigned to an unknown value.
 */
abstract public void markAsDefinitelyUnknown(LocalVariableBinding local);

/**
 * Merge branches using optimized boolean conditions
 */
public static UnconditionalFlowInfo mergedOptimizedBranches(
		FlowInfo initsWhenTrue, boolean isOptimizedTrue,
		FlowInfo initsWhenFalse, boolean isOptimizedFalse,
		boolean allowFakeDeadBranch) {
	UnconditionalFlowInfo mergedInfo;
	if (isOptimizedTrue){
		if (initsWhenTrue == FlowInfo.DEAD_END && allowFakeDeadBranch) {
			mergedInfo = initsWhenFalse.setReachMode(FlowInfo.UNREACHABLE).
				unconditionalInits();
		}
		else {
			mergedInfo =
				initsWhenTrue.addPotentialInitializationsFrom(initsWhenFalse.
					nullInfoLessUnconditionalCopy()).
				unconditionalInits();
		}
	}
	else if (isOptimizedFalse) {
		if (initsWhenFalse == FlowInfo.DEAD_END && allowFakeDeadBranch) {
			mergedInfo = initsWhenTrue.setReachMode(FlowInfo.UNREACHABLE).
				unconditionalInits();
		}
		else {
			mergedInfo =
				initsWhenFalse.addPotentialInitializationsFrom(initsWhenTrue.
					nullInfoLessUnconditionalCopy()).
				unconditionalInits();
		}
	}
	else {
		mergedInfo = initsWhenTrue.
			mergedWith(initsWhenFalse.unconditionalInits());
	}
	return mergedInfo;
}

/**
 * Return REACHABLE if this flow info is reachable, UNREACHABLE
 * else.
 * @return REACHABLE if this flow info is reachable, UNREACHABLE
 *         else
 */
public int reachMode() {
	return this.tagBits & UNREACHABLE;
}

/**
 * Return a flow info that carries the same information as the result of
 * {@link #initsWhenTrue() initsWhenTrue}, but warrantied to be different
 * from this.<br>
 * Caveat: side effects on the result may affect components of this.
 * @return the result of initsWhenTrue or a copy of it
 */
abstract public FlowInfo safeInitsWhenTrue();

/**
 * Set this flow info reach mode and return this.
 * @param reachMode one of {@link #REACHABLE REACHABLE} or {@link #UNREACHABLE UNREACHABLE}
 * @return this, with the reach mode set to reachMode
 */
abstract public FlowInfo setReachMode(int reachMode);

/**
 * Return the intersection of this and otherInits, that is
 * one of:<ul>
 *   <li>the receiver updated in the following way:<ul>
 *     <li>intersection of definitely assigned variables,
 *     <li>union of potentially assigned variables,
 *     <li>similar operations for null,</ul>
 *   <li>or the receiver or otherInits if the other one is non
 *       reachable.</ul>
 * otherInits is not affected, and is not returned either (no
 * need to protect the result).
 * @param otherInits the flow info to merge with this
 * @return the intersection of this and otherInits.
 */
abstract public UnconditionalFlowInfo mergedWith(
		UnconditionalFlowInfo otherInits);

/**
 * Return a copy of this unconditional flow info, deprived from its null
 * info. {@link #DEAD_END DEAD_END} is returned unmodified.
 * @return a copy of this unconditional flow info deprived from its null info
 */
abstract public UnconditionalFlowInfo nullInfoLessUnconditionalCopy();

	public String toString(){

		if (this == DEAD_END){
			return "FlowInfo.DEAD_END"; //$NON-NLS-1$
		}
		return super.toString();
	}

/**
 * Return a new flow info that holds the same information as this would after
 * a call to unconditionalInits, but leaving this info unaffected. Moreover,
 * the result can be modified without affecting this.
 * @return a new flow info carrying this unconditional flow info
 */
abstract public UnconditionalFlowInfo unconditionalCopy();

/**
 * Return a new flow info that holds the same information as this would after
 * a call to {@link #unconditionalInits() unconditionalInits} followed by the
 * erasure of fields specific information, but leaving this flow info unaffected.
 * @return a new flow info carrying the unconditional flow info for local variables
 */
abstract public UnconditionalFlowInfo unconditionalFieldLessCopy();

/**
 * Return a flow info that merges the possible paths of execution described by
 * this flow info. In case of an unconditional flow info, return this. In case
 * of a conditional flow info, merge branches recursively. Caveat: this may
 * be affected, and modifying the result may affect this.
 * @return a flow info that merges the possible paths of execution described by
 * 			this
 */
abstract public UnconditionalFlowInfo unconditionalInits();

/**
 * Return a new flow info that holds the same information as this would after
 * a call to {@link #unconditionalInits() unconditionalInits}, but leaving
 * this info unaffected. Side effects on the result might affect this though
 * (consider it as read only).
 * @return a flow info carrying this unconditional flow info
 */
abstract public UnconditionalFlowInfo unconditionalInitsWithoutSideEffect();
}
