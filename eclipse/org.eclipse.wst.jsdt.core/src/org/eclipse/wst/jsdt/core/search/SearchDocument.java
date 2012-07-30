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

import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.internal.core.search.indexing.InternalSearchDocument;

/**
 * A search document encapsulates a content to be either indexed or searched in.
 * A search particpant creates a search document.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public abstract class SearchDocument extends InternalSearchDocument {
	private String documentPath;
	private SearchParticipant participant;

	/**
	 * Creates a new search document. The given document path is a string that uniquely identifies the document.
	 * Most of the time it is a workspace-relative path, but it can also be a file system path, or a path inside a zip file.
	 *
	 * @param documentPath the path to the document,
	 * or <code>null</code> if none
	 * @param participant the participant that creates the search document
	 */
	protected SearchDocument(String documentPath, SearchParticipant participant) {
		this.documentPath = documentPath;
		this.participant = participant;
	}

	/**
	 * Adds the given index entry (category and key) coming from this
	 * document to the index. This method must be called from
	 * {@link SearchParticipant#indexDocument(SearchDocument document, org.eclipse.core.runtime.IPath indexPath)}.
	 *
	 * @param category the category of the index entry
	 * @param key the key of the index entry
	 */
	public void addIndexEntry(char[] category, char[] key) {
		super.addIndexEntry(category, key);
	}

	/**
	 * Returns the contents of this document.
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 * <p>
	 * This method must be implemented in subclasses.
	 * </p><p>
	 * Note: some implementation may choose to cache the contents directly on the
	 * document for performance reason. However, this could induce scalability issues due
	 * to the fact that collections of documents are manipulated throughout the search
	 * operation, and cached contents would then consume lots of memory until they are
	 * all released at once in the end.
	 * </p>
	 *
	 * @return the contents of this document,
	 * or <code>null</code> if none
	 */
	public abstract byte[] getByteContents();

	/**
	 * Returns the contents of this document.
	 * Contents may be different from actual resource at corresponding document
	 * path due to preprocessing.
	 * <p>
	 * This method must be implemented in subclasses.
	 * </p><p>
	 * Note: some implementation may choose to cache the contents directly on the
	 * document for performance reason. However, this could induce scalability issues due
	 * to the fact that collections of documents are manipulated throughout the search
	 * operation, and cached contents would then consume lots of memory until they are
	 * all released at once in the end.
	 * </p>
	 *
	 * @return the contents of this document,
	 * or <code>null</code> if none
	 */
	public abstract char[] getCharContents();

	/**
	 * Returns the encoding for this document.
	 * <p>
	 * This method must be implemented in subclasses.
	 * </p>
	 *
	 * @return the encoding for this document,
	 * or <code>null</code> if none
	 */
	public abstract String getEncoding();

	/**
	 * Returns the participant that created this document.
	 *
	 * @return the participant that created this document
	 */
	public final SearchParticipant getParticipant() {
		return this.participant;
	}

	/**
	 * Returns the path to the original document to publicly mention in index
	 * or search results. This path is a string that uniquely identifies the document.
	 * Most of the time it is a workspace-relative path, but it can also be a file system path,
	 * or a path inside a zip file.
	 *
	 * @return the path to the document
	 */
	public final String getPath() {
		return this.documentPath;
	}
	/**
	 * Removes all index entries from the index for the given document.
	 * This method must be called from
	 * {@link SearchParticipant#indexDocument(SearchDocument document, org.eclipse.core.runtime.IPath indexPath)}.
	 */
	public void removeAllIndexEntries() {
		super.removeAllIndexEntries();
	}

	public boolean isVirtual() {
		return false;

	}
	public IJavaScriptElement getJavaElement() {
		return null;

	}
}
