/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core.hierarchy;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryField;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryType;

public class HierarchyBinaryType implements IBinaryType {
	private int modifiers;
	private char[] sourceName;
	private char[] name;
	private char[] enclosingTypeName;
	private char[] superclass;
	private char[] genericSignature;

public HierarchyBinaryType(int modifiers, char[] qualification, char[] sourceName, char[] enclosingTypeName){

	this.modifiers = modifiers;
	this.sourceName = sourceName;
	if (enclosingTypeName == null){
		this.name = CharOperation.concat(qualification, sourceName, '/');
	} else {
		this.name = CharOperation.concat(qualification, '/', enclosingTypeName, '$', sourceName); //rebuild A$B name
		this.enclosingTypeName = CharOperation.concat(qualification, enclosingTypeName,'/');
		CharOperation.replace(this.enclosingTypeName, '.', '/');
	}
	CharOperation.replace(this.name, '.', '/');
}
/**
 * Answer the resolved name of the enclosing type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the receiver is a top level type.
 *
 * For example, java.lang.String is java/lang/String.
 */
public char[] getEnclosingTypeName() {
	return this.enclosingTypeName;
}
/**
 * Answer the receiver's fields or null if the array is empty.
 */
public IBinaryField[] getFields() {
	return null;
}
/**
 * @see org.eclipse.wst.jsdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return null;
}
public char[] getGenericSignature() {
	return this.genericSignature;
}
/**
 * Answer the receiver's nested types or null if the array is empty.
 *
 * This nested type info is extracted from the inner class attributes.
 * Ask the name environment to find a member type using its compound name.
 */
public IBinaryNestedType[] getMemberTypes() {
	return null;
}
/**
 * Answer the receiver's methods or null if the array is empty.
 */
public IBinaryMethod[] getMethods() {
	return null;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 */
public int getModifiers() {
	return this.modifiers;
}
/**
 * Answer the resolved name of the type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec.
 *
 * For example, java.lang.String is java/lang/String.
 */
public char[] getName() {
	return this.name;
}

public char[] getSourceName() {
	return this.sourceName;
}

/**
 * Answer the resolved name of the receiver's superclass in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if it does not have one.
 *
 * For example, java.lang.String is java/lang/String.
 */
public char[] getSuperclassName() {
	return this.superclass;
}
public boolean isAnonymous() {
	return false; // index did not record this information (since unused for hierarchies)
}

/**
 * Answer whether the receiver contains the resolved binary form
 * or the unresolved source form of the type.
 */
public boolean isBinaryType() {
	return true;
}
public boolean isLocal() {
	return false;  // index did not record this information (since unused for hierarchies)
}
public boolean isMember() {
	return false;  // index did not record this information (since unused for hierarchies)
}

public void recordSuperType(char[] superTypeName, char[] superQualification){

	// index encoding of p.A$B was B/p.A$, rebuild the proper name
	if (superQualification != null){
		int length = superQualification.length;
		if (superQualification[length-1] == '$'){
			char[] enclosingSuperName = CharOperation.lastSegment(superQualification, '.');
			superTypeName = CharOperation.concat(enclosingSuperName, superTypeName);
			superQualification = CharOperation.subarray(superQualification, 0, length - enclosingSuperName.length - 1);
		}
	}

	char[] encodedName = CharOperation.concat(superQualification, superTypeName, '/');
	CharOperation.replace(encodedName, '.', '/');
	this.superclass = encodedName;
	
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	if (this.modifiers == ClassFileConstants.AccPublic) {
		buffer.append("public "); //$NON-NLS-1$
	}
	switch (TypeDeclaration.kind(this.modifiers)) {
		case TypeDeclaration.CLASS_DECL :
			buffer.append("class "); //$NON-NLS-1$
			break;
	}
	if (this.name != null) {
		buffer.append(this.name);
	}
	if (this.superclass != null) {
		buffer.append("\n  extends "); //$NON-NLS-1$
		buffer.append(this.superclass);
	}
	return buffer.toString();
}

/**
 * @see org.eclipse.wst.jsdt.internal.compiler.env.IBinaryType
 */
public char[] sourceFileName() {
	return null;
}
// TODO (jerome) please verify that we don't need the tagbits for the receiver
public long getTagBits() {
	return 0;
}
}
