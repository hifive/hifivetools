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
package org.eclipse.wst.jsdt.internal.core.search.matching;

import org.eclipse.wst.jsdt.core.IField;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.search.SearchPattern;
import org.eclipse.wst.jsdt.internal.core.util.Util;

public class FieldPattern extends VariablePattern {

// declaring type
protected char[] declaringQualification;
protected char[] declaringSimpleName;

// type
protected char[] typeQualification;
protected char[] typeSimpleName;

protected static char[][] REF_CATEGORIES = { REF };
protected static char[][] REF_AND_DECL_CATEGORIES = { REF, FIELD_DECL };
protected static char[][] DECL_CATEGORIES = { FIELD_DECL };
protected static char[][] VAR_REF_AND_DECL_CATEGORIES = { REF, VAR_DECL };
protected static char[][] VAR_DECL_CATEGORIES = { VAR_DECL };
protected boolean isVar;

public static char[] createIndexKey(char[] fieldName) {
	return fieldName;
}

public FieldPattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	boolean isVar,
	char[] name,
	char[] declaringQualification,
	char[] declaringSimpleName,
	char[] typeQualification,
	char[] typeSimpleName,
	int matchRule, IField field) {

	super(FIELD_PATTERN, findDeclarations, readAccess, writeAccess, name, matchRule,field);

	this.isVar=isVar;
	this.declaringQualification = isCaseSensitive() ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive() ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.typeQualification = isCaseSensitive() ? typeQualification : CharOperation.toLowerCase(typeQualification);
	this.typeSimpleName = (isCaseSensitive() || isCamelCase())  ? typeSimpleName : CharOperation.toLowerCase(typeSimpleName);

	((InternalSearchPattern)this).mustResolve = mustResolve();
}
/*
 * Instanciate a field pattern with additional information for generics search
 */
public FieldPattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	boolean isVar,
	char[] name,
	char[] declaringQualification,
	char[] declaringSimpleName,
	char[] typeQualification,
	char[] typeSimpleName,
	String typeSignature,
	int matchRule, IField field) {

	this(findDeclarations, readAccess, writeAccess, isVar, name, declaringQualification, declaringSimpleName, typeQualification, typeSimpleName, matchRule,field);

	// store type signatures and arguments
	if (typeSignature != null) {
		this.typeSignatures = Util.splitTypeLevelsSignature(typeSignature);
		setTypeArguments(Util.getAllTypeArguments(this.typeSignatures));
	}
}
public void decodeIndexKey(char[] key) {
	this.name = key;
}
public SearchPattern getBlankPattern() {
	return new FieldPattern(false, false, false, isVar, null, null, null, null, null, R_EXACT_MATCH | R_CASE_SENSITIVE,null);
}
public char[] getIndexKey() {
	return this.name;
}
public char[][] getIndexCategories() {

	if (this.isVar) {
		if (this.findReferences)
			return this.findDeclarations || this.writeAccess ? VAR_REF_AND_DECL_CATEGORIES
					: REF_CATEGORIES;
		if (this.findDeclarations)
			return VAR_DECL_CATEGORIES;
	}
	else {
		if (this.findReferences)
			return this.findDeclarations || this.writeAccess ? REF_AND_DECL_CATEGORIES
					: REF_CATEGORIES;
		if (this.findDeclarations)
			return DECL_CATEGORIES;

	}
	return CharOperation.NO_CHAR_CHAR;
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	return true; // index key is not encoded so query results all match
}
protected boolean mustResolve() {
	if (this.declaringSimpleName != null || this.declaringQualification != null) return true;
	if (this.typeSimpleName != null || this.typeQualification != null) return true;

	return super.mustResolve();
}
protected StringBuffer print(StringBuffer output) {
	if (this.findDeclarations) {
		output.append(this.findReferences
			? "FieldCombinedPattern: " //$NON-NLS-1$
			: "FieldDeclarationPattern: "); //$NON-NLS-1$
	} else {
		output.append("FieldReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null) output.append(declaringQualification).append('.');
	if (declaringSimpleName != null)
		output.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) output.append("*."); //$NON-NLS-1$
	if (name == null) {
		output.append("*"); //$NON-NLS-1$
	} else {
		output.append(name);
	}
	if (typeQualification != null)
		output.append(" --> ").append(typeQualification).append('.'); //$NON-NLS-1$
	else if (typeSimpleName != null) output.append(" --> "); //$NON-NLS-1$
	if (typeSimpleName != null)
		output.append(typeSimpleName);
	else if (typeQualification != null) output.append("*"); //$NON-NLS-1$
	return super.print(output);
}
}
