/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core.index;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.search.IJavaScriptSearchScope;
import org.eclipse.wst.jsdt.core.search.SearchPattern;
import org.eclipse.wst.jsdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.wst.jsdt.internal.compiler.util.SimpleSet;
import org.eclipse.wst.jsdt.internal.core.search.indexing.ReadWriteMonitor;

/**
 * An <code>Index</code> maps document names to their referenced words in various categories.
 *
 * Queries can search a single category or several at the same time.
 *
 * Indexes are not synchronized structures and should only be queried/updated one at a time.
 */

public class Index {

public String containerPath;
public ReadWriteMonitor monitor;

protected DiskIndex diskIndex;
protected MemoryIndex memoryIndex;

/**
 * Mask used on match rule for indexing.
 */
static final int MATCH_RULE_INDEX_MASK =
	SearchPattern.R_EXACT_MATCH |
	SearchPattern.R_PREFIX_MATCH |
	SearchPattern.R_PATTERN_MATCH |
	SearchPattern.R_REGEXP_MATCH |
	SearchPattern.R_CASE_SENSITIVE |
	SearchPattern.R_CAMELCASE_MATCH;

public static boolean isMatch(char[] pattern, char[] word, int matchRule) {
	if (pattern == null) return true;
	int patternLength = pattern.length;
	int wordLength = word.length;
	if (patternLength == 0) return matchRule != SearchPattern.R_EXACT_MATCH;
	if (wordLength == 0) return (matchRule & SearchPattern.R_PATTERN_MATCH) != 0 && patternLength == 1 && pattern[0] == '*';

	// First test camel case if necessary
	boolean isCamelCase = (matchRule & SearchPattern.R_CAMELCASE_MATCH) != 0;
	if (isCamelCase &&  pattern[0] == word[0] && CharOperation.camelCaseMatch(pattern, word)) {
		return true;
	}

	// need to mask some bits of pattern rule (bug 79790)
	matchRule &= ~SearchPattern.R_CAMELCASE_MATCH;
	switch(matchRule & MATCH_RULE_INDEX_MASK) {
		case SearchPattern.R_EXACT_MATCH :
			if (!isCamelCase) {
				return patternLength == wordLength && CharOperation.equals(pattern, word, false);
			}
			// fall through prefix match if camel case failed
		case SearchPattern.R_PREFIX_MATCH :
			return patternLength <= wordLength && CharOperation.prefixEquals(pattern, word, false);
		case SearchPattern.R_PATTERN_MATCH :
			return CharOperation.match(pattern, word, false);
		case SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE :
			if (!isCamelCase) {
				return pattern[0] == word[0] && patternLength == wordLength && CharOperation.equals(pattern, word);
			}
			// fall through prefix match if camel case failed
		case SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE :
			return pattern[0] == word[0] && patternLength <= wordLength && CharOperation.prefixEquals(pattern, word);
		case SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE :
			return CharOperation.match(pattern, word, true);
		case SearchPattern.R_REGEXP_MATCH: {
			return Pattern.compile(new String(pattern), Pattern.CASE_INSENSITIVE).matcher(new String(word)).matches();
		}
		case SearchPattern.R_REGEXP_MATCH | SearchPattern.R_CASE_SENSITIVE: {
			return Pattern.matches(new String(pattern), new String(word));
		}
	}
	return false;
}


public Index(String fileName, String containerPath, boolean reuseExistingFile) throws IOException {
	this.containerPath = containerPath;
	this.monitor = new ReadWriteMonitor();

	this.memoryIndex = new MemoryIndex();
	this.diskIndex = new DiskIndex(fileName);
	this.diskIndex.initialize(reuseExistingFile);
}
public void addIndexEntry(char[] category, char[] key, String containerRelativePath) {
	this.memoryIndex.addIndexEntry(category, key, containerRelativePath);
}
public String containerRelativePath(String documentPath) {
	int index = documentPath.indexOf(IJavaScriptSearchScope.JAR_FILE_ENTRY_SEPARATOR);
	if (index == -1) {

		IPath containerPath = new Path(this.containerPath);
		IPath docPath = new Path(documentPath);

		if(containerPath.makeAbsolute().equals(docPath.makeAbsolute())) {
			return documentPath;
		}

		index = this.containerPath.length();
		if (documentPath.length() <  index)
			throw new IllegalArgumentException("Document path " + documentPath + " must be relative to " + this.containerPath); //$NON-NLS-1$ //$NON-NLS-2$
		else if (documentPath.length()==index)
			index--;
	}
	return documentPath.substring(index + 1);
}
public File getIndexFile() {
	return this.diskIndex == null ? null : this.diskIndex.indexFile;
}
public boolean hasChanged() {
	return this.memoryIndex.hasChanged();
}
/**
 * Returns the entries containing the given key in a group of categories, or null if no matches are found.
 * The matchRule dictates whether its an exact, prefix or pattern match, as well as case sensitive or insensitive.
 * If the key is null then all entries in specified categories are returned.
 */
public EntryResult[] query(char[][] categories, char[] key, int matchRule) throws IOException {
	if (this.memoryIndex.shouldMerge() && monitor.exitReadEnterWrite()) {
		try {
			save();
		} finally {
			monitor.exitWriteEnterRead();
		}
	}

	HashtableOfObject results;
	int rule = matchRule & MATCH_RULE_INDEX_MASK;
	if (this.memoryIndex.hasChanged()) {
		results = this.diskIndex.addQueryResults(categories, key, rule, this.memoryIndex);
		results = this.memoryIndex.addQueryResults(categories, key, rule, results);
	} else {
		results = this.diskIndex.addQueryResults(categories, key, rule, null);
	}
	if (results == null) return null;

	EntryResult[] entryResults = new EntryResult[results.elementSize];
	int count = 0;
	Object[] values = results.valueTable;
	for (int i = 0, l = values.length; i < l; i++) {
		EntryResult result = (EntryResult) values[i];
		if (result != null)
			entryResults[count++] = result;
	}
	return entryResults;
}
/**
 * Returns the document names that contain the given substring, if null then returns all of them.
 */
public String[] queryDocumentNames(String substring) throws IOException {
	SimpleSet results;
	if (this.memoryIndex.hasChanged()) {
		results = this.diskIndex.addDocumentNames(substring, this.memoryIndex);
		this.memoryIndex.addDocumentNames(substring, results);
	} else {
		results = this.diskIndex.addDocumentNames(substring, null);
	}
	if (results.elementSize == 0) return null;

	String[] documentNames = new String[results.elementSize];
	int count = 0;
	Object[] paths = results.values;
	for (int i = 0, l = paths.length; i < l; i++)
		if (paths[i] != null)
			documentNames[count++] = (String) paths[i];
	return documentNames;
}
public void remove(String containerRelativePath) {
	this.memoryIndex.remove(containerRelativePath);
}
public void save() throws IOException {
	// must own the write lock of the monitor
	if (!hasChanged()) return;

	int numberOfChanges = this.memoryIndex.docsToReferences.elementSize;
	this.diskIndex = this.diskIndex.mergeWith(this.memoryIndex);
	this.memoryIndex = new MemoryIndex();
	if (numberOfChanges > 1000)
		System.gc(); // reclaim space if the MemoryIndex was very BIG
}
public void startQuery() {
	if (this.diskIndex != null)
		this.diskIndex.startQuery();
}
public void stopQuery() {
	if (this.diskIndex != null)
		this.diskIndex.stopQuery();
}
public String toString() {
	return "Index for " + this.containerPath; //$NON-NLS-1$
}
}
