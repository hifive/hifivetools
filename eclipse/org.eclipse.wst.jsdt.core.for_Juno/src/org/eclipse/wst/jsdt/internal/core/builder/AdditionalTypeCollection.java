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

public class AdditionalTypeCollection extends ReferenceCollection {

char[][] definedTypeNames;

protected AdditionalTypeCollection(char[][] definedTypeNames, char[][][] qualifiedReferences, char[][] simpleNameReferences) {
	super(qualifiedReferences, simpleNameReferences);
	this.definedTypeNames = definedTypeNames; // do not bother interning member type names (ie. 'A$M')
}
}

