/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;

/**
 * A JavaScript search match that represents a method reference.
 * The element is the inner-most enclosing member that references this method.
 * <p>
 * This class is intended to be instantiated and subclassed by clients.
 * </p>
 * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public class MethodReferenceMatch extends SearchMatch {
	private boolean constructor;
	private boolean superInvocation;

	/**
	 * Creates a new method reference match.
	 *
	 * @param enclosingElement the inner-most enclosing member that references this method
	 * @param accuracy one of {@link #A_ACCURATE} or {@link #A_INACCURATE}
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param insideDocComment <code>true</code> if this search match is inside a doc
	 * comment, and <code>false</code> otherwise
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 */
	public MethodReferenceMatch(IJavaScriptElement enclosingElement, int accuracy, int offset, int length, boolean insideDocComment, SearchParticipant participant, IResource resource) {
		super(enclosingElement, accuracy, offset, length, participant, resource);
		setInsideDocComment(insideDocComment);
	}

	/**
	 * Creates a new method reference match.
	 *
	 * @param enclosingElement the inner-most enclosing member that references this method
	 * @param accuracy one of {@link #A_ACCURATE} or {@link #A_INACCURATE}
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param constructor <code>true</code> if this search match a constructor
	 * <code>false</code> otherwise
	 * @param insideDocComment <code>true</code> if this search match is inside a doc
	 * comment, and <code>false</code> otherwise
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 *  
	 */
	public MethodReferenceMatch(IJavaScriptElement enclosingElement, int accuracy, int offset, int length, boolean constructor, boolean insideDocComment, SearchParticipant participant, IResource resource) {
		this(enclosingElement, accuracy, offset, length, insideDocComment, participant, resource);
		this.constructor = constructor;
	}

	/**
	 * Creates a new method reference match.
	 *
	 * @param enclosingElement the inner-most enclosing member that references this method
	 * @param accuracy one of {@link #A_ACCURATE} or {@link #A_INACCURATE}
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param constructor <code>true</code> if this search matches a constructor
	 * <code>false</code> otherwise
	 * @param superInvocation <code>true</code> if this search matches a super-type invocation
	 * element <code>false</code> otherwise
	 * @param insideDocComment <code>true</code> if this search match is inside a doc
	 * comment, and <code>false</code> otherwise
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 *  
	 */
	public MethodReferenceMatch(IJavaScriptElement enclosingElement, int accuracy, int offset, int length, boolean constructor, boolean superInvocation, boolean insideDocComment, SearchParticipant participant, IResource resource) {
		this(enclosingElement, accuracy, offset, length, constructor, insideDocComment, participant, resource);
		this.superInvocation = superInvocation;
	}

	/**
	 * Returns whether the reference is on a constructor.
	 *
	 * @return Returns whether the reference is on a constructor or not.
	 *  
	 */
	public final boolean isConstructor() {
		return this.constructor;
	}

	/**
	 * Returns whether the reference is on a message sent from a type
	 * which is a super type of the searched method declaring type.
	 * If <code>true</code>, the method called at run-time may or may not be
	 * the search target, depending on the run-time type of the receiver object.
	 *
	 * @return <code>true</code> if the reference is on a message sent from
	 * a super-type of the searched method declaring class, <code>false </code> otherwise
	 */
	public boolean isSuperInvocation() {
		return this.superInvocation;
	}
}
