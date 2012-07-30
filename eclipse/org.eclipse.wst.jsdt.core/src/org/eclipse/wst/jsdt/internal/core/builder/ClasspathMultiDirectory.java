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
package org.eclipse.wst.jsdt.internal.core.builder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.core.util.Util;

class ClasspathMultiDirectory extends ClasspathDirectory {

IContainer sourceFolder;
char[][] inclusionPatterns; // used by builders when walking source folders
char[][] exclusionPatterns; // used by builders when walking source folders
boolean hasIndependentOutputFolder; // if output folder is not equal to any of the source folders

ClasspathMultiDirectory(IContainer sourceFolder, IContainer binaryFolder, char[][] inclusionPatterns, char[][] exclusionPatterns) {
	super(binaryFolder, true, null);

	this.sourceFolder = sourceFolder;
	this.inclusionPatterns = inclusionPatterns;
	this.exclusionPatterns = exclusionPatterns;
	this.hasIndependentOutputFolder = false;

	// handle the case when a state rebuilds a source folder
	if (this.inclusionPatterns != null && this.inclusionPatterns.length == 0)
		this.inclusionPatterns = null;
	if (this.exclusionPatterns != null && this.exclusionPatterns.length == 0)
		this.exclusionPatterns = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathMultiDirectory)) return false;

	ClasspathMultiDirectory md = (ClasspathMultiDirectory) o;
	return sourceFolder.equals(md.sourceFolder) && binaryFolder.equals(md.binaryFolder)
		&& CharOperation.equals(inclusionPatterns, md.inclusionPatterns)
		&& CharOperation.equals(exclusionPatterns, md.exclusionPatterns);
}

protected boolean isExcluded(IResource resource) {
	if (this.exclusionPatterns != null || this.inclusionPatterns != null)
		if (this.sourceFolder.equals(this.binaryFolder))
			return Util.isExcluded(resource, this.inclusionPatterns, this.exclusionPatterns);
	return false;
}

public String toString() {
	return "Source classpath directory " + sourceFolder.getFullPath().toString() + //$NON-NLS-1$
		" with " + super.toString(); //$NON-NLS-1$
}
}
