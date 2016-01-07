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
package org.eclipse.wst.jsdt.internal.core.search.indexing;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.search.SearchDocument;
import org.eclipse.wst.jsdt.internal.core.JavaModelManager;
import org.eclipse.wst.jsdt.internal.core.search.matching.ConstructorDeclarationPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.FieldPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.MethodPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.SuperTypeReferencePattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.TypeDeclarationPattern;

public abstract class AbstractIndexer implements IIndexConstants {

	SearchDocument document;

	public AbstractIndexer(SearchDocument document) {
		this.document = document;
	}
	
	public void addClassDeclaration(
			int modifiers,
			char[] packageName,
			char[] name,
			char[][] enclosingTypeNames,
			char[] superclass,
			boolean secondary) {
		addTypeDeclaration(modifiers, packageName, name, enclosingTypeNames, secondary);

		if (superclass != null) {
			addTypeReference(superclass);
		}
		addIndexEntry(
			SUPER_REF,
			SuperTypeReferencePattern.createIndexKey(
				modifiers, packageName, name, enclosingTypeNames, superclass));
	}
	public void addConstructorDeclaration(char[] typeName, char[][] parameterTypes, char[][] parameterNames, int modifiers) {
		int argCount = parameterTypes == null ? 0 : parameterTypes.length;
		addIndexEntry(CONSTRUCTOR_DECL, ConstructorDeclarationPattern.createDeclarationIndexKey(typeName, argCount, parameterTypes, parameterNames, modifiers));

		if (parameterTypes != null) {
			for (int i = 0; i < argCount; i++)
				addTypeReference(parameterTypes[i]);
		}
	}
	public void addConstructorReference(char[] typeName, int argCount) {
		char[] simpleTypeName = CharOperation.lastSegment(typeName,'.');
		addTypeReference(simpleTypeName);
		addIndexEntry(CONSTRUCTOR_REF, ConstructorPattern.createIndexKey(simpleTypeName, argCount));
		char[] innermostTypeName = CharOperation.lastSegment(simpleTypeName,'$');
		if (innermostTypeName != simpleTypeName)
			addIndexEntry(CONSTRUCTOR_REF, ConstructorPattern.createIndexKey(innermostTypeName, argCount));
	}
	public void addFieldDeclaration(char[] typeName, char[] fieldName, boolean isVar) {
		char [] key = isVar ? VAR_DECL:FIELD_DECL;
		addIndexEntry(key, FieldPattern.createIndexKey(fieldName));
		if (typeName!=null)
		addTypeReference(typeName);
	}
	public void addFieldReference(char[] fieldName) {
		addNameReference(fieldName);
	}
	protected void addIndexEntry(char[] category, char[] key) {
		this.document.addIndexEntry(category, key);
	}
	public void addMethodDeclaration(char[] methodName, char[][] parameterTypes,
				char[] returnType,boolean isFunction) {
		int argCount = parameterTypes == null ? 0 : parameterTypes.length;
		addIndexEntry(isFunction ? FUNCTION_DECL : METHOD_DECL, MethodPattern.createIndexKey(methodName, argCount));

		if (parameterTypes != null) {
			for (int i = 0; i < argCount; i++)
				addTypeReference(parameterTypes[i]);
		}
		if (returnType != null)
			addTypeReference(returnType);
	}
	public void addMethodReference(char[] methodName, int argCount) {
		addIndexEntry(METHOD_REF, MethodPattern.createIndexKey(methodName, argCount));
	}
	public void addNameReference(char[] name) {
		addIndexEntry(REF, name);
	}
	protected void addTypeDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, boolean secondary) {
		char[] indexKey = TypeDeclarationPattern.createIndexKey(modifiers, name, packageName, enclosingTypeNames, secondary);
		if (secondary)
			JavaModelManager.getJavaModelManager().secondaryTypeAdding(
				this.document.getPath(),
				name == null ? CharOperation.NO_CHAR : name,
				packageName == null ? CharOperation.NO_CHAR : packageName);

		addIndexEntry(TYPE_DECL, indexKey);
		
		// add a second key that includes the package name
		indexKey = TypeDeclarationPattern.createIndexKey(modifiers, CharOperation.concat(packageName, name, '.'), packageName, enclosingTypeNames, secondary);
		addIndexEntry(TYPE_DECL, indexKey);
	}
	public void addTypeReference(char[] typeName) {
		if (typeName!=null)
		addNameReference(CharOperation.lastSegment(typeName, '.'));
	}
	public abstract void indexDocument();
}
