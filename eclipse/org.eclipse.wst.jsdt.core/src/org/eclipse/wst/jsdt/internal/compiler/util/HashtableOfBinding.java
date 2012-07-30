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
package org.eclipse.wst.jsdt.internal.compiler.util;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Binding;

public final class HashtableOfBinding {
	// to avoid using Enumerations, walk the individual tables skipping nulls
	public char[] keyTable[];
	public Binding valueTable[];

	public int elementSize; // number of elements in the table
	int threshold;
public HashtableOfBinding() {
	this(3);
}
public HashtableOfBinding(int size) {
	this.elementSize = 0;
	this.threshold = size; // size represents the expected number of elements
	int extraRoom = (int) (size * 1.75f);
	if (this.threshold == extraRoom)
		extraRoom++;
	this.keyTable = new char[extraRoom][];
	this.valueTable = new Binding[extraRoom];
}
public boolean containsKey(char[] key) {
	int length = keyTable.length,
		index = CharOperation.hashCode(key) % length;
	int keyLength = key.length;
	char[] currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
			return true;
		if (++index == length) {
			index = 0;
		}
	}
	return false;
}
public Binding get(char[] key) {
	int length = keyTable.length,
		index = CharOperation.hashCode(key) % length;
	int keyLength = key.length;
	char[] currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
			return valueTable[index];
		if (++index == length) {
			index = 0;
		}
	}
	return null;
}
public Binding put(char[] key, Binding value) {
	int length = keyTable.length,
		index = CharOperation.hashCode(key) % length;
	int keyLength = key.length;
	char[] currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
			return valueTable[index] = value;
		if (++index == length) {
			index = 0;
		}
	}
	keyTable[index] = key;
	valueTable[index] = value;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold)
		rehash();
	return value;
}
private void rehash() {
	HashtableOfBinding newHashtable = new HashtableOfBinding(elementSize < 100 ? 100 : elementSize * 2); // double the number of expected elements
	char[] currentKey;
	for (int i = keyTable.length; --i >= 0;)
		if ((currentKey = keyTable[i]) != null)
			newHashtable.put(currentKey, valueTable[i]);

	this.keyTable = newHashtable.keyTable;
	this.valueTable = newHashtable.valueTable;
	this.threshold = newHashtable.threshold;
}
public int size() {
	return elementSize;
}
public String toString() {
	String s = ""; //$NON-NLS-1$
	Binding type;
	for (int i = 0, length = valueTable.length; i < length; i++)
		if ((type = valueTable[i]) != null)
			s += type.toString() + "\n"; //$NON-NLS-1$
	return s;
}
}
