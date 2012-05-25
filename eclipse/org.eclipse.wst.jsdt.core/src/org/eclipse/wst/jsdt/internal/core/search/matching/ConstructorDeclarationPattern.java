/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core.search.matching;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.search.SearchPattern;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * <p>Pattern used to find and store constructor declarations.</p>
 */
public class ConstructorDeclarationPattern extends ConstructorPattern {
	public int modifiers;
	public char[][] parameterTypes;
	public char[][] parameterNames;

	public ConstructorDeclarationPattern(char[] declaringSimpleName, int matchRule) {
		this(matchRule);
		this.declaringSimpleName = (this.isCaseSensitive || this.isCamelCase) ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
		this.findDeclarations = true;
		this.findReferences = false;
		this.parameterCount = -1;
	}
	
	ConstructorDeclarationPattern(int matchRule) {
		super(matchRule);
	}
	
	public SearchPattern getBlankPattern() {
		return new ConstructorDeclarationPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
	}
	public char[][] getIndexCategories() {
		return DECL_CATEGORIES;
	}
	public boolean matchesDecodedKey(SearchPattern decodedPattern) {
		ConstructorDeclarationPattern pattern = (ConstructorDeclarationPattern) decodedPattern;
	
		return (this.parameterCount == pattern.parameterCount || this.parameterCount == -1 || this.varargs)
			&& matchesName(this.declaringSimpleName, pattern.declaringSimpleName);
	}
	
	/**
	 * <p>Decodes an index key made with {@link #createDeclarationIndexKey(char[], int, char[][], char[][], int)} into the
	 * parameters of this pattern.</p>
	 * 
	 * @see org.eclipse.wst.jsdt.internal.core.search.matching.ConstructorPattern#decodeIndexKey(char[])
	 * 
	 * @see #createDeclarationIndexKey(char[], int, char[][], char[][], int)
	 */
	public void decodeIndexKey(char[] key) {
		//decode type name
		int last = key.length - 1;
		int slash = CharOperation.indexOf(SEPARATOR, key, 0);
		this.declaringSimpleName = CharOperation.subarray(key, 0, slash);
		
		int start = slash + 1;
		slash = CharOperation.indexOf(SEPARATOR, key, start);
		last = slash - 1;
		
		//decode parameter count
		this.parameterCount = 0;
		int power = 1;
		for (int i = last; i >= start; i--) {
			if (i == last) {
				this.parameterCount = key[i] - '0';
			} else {
				power *= 10;
				this.parameterCount += power * (key[i] - '0');
			}
		}
		
		// initialize optional fields
		this.modifiers = 0;
		this.parameterTypes = null;
		this.parameterNames = null;
		
		/* if no parameters just decode modifiers
		 * else decode parameters and modifiers
		 */
		start = slash + 1;
		if (this.parameterCount == 0) {
			slash = slash + 3;
			last = slash - 1;
			
			this.modifiers = key[last-1] + (key[last]<<16);
		} else if (this.parameterCount > 0){
			slash = CharOperation.indexOf(SEPARATOR, key, start);
			last = slash - 1;
			
			
			this.parameterTypes = CharOperation.splitOn(PARAMETER_SEPARATOR, key, start, slash);
			
			start = slash + 1;
			slash = CharOperation.indexOf(SEPARATOR, key, start);
			last = slash - 1;
			
			if (slash != start) {
				this.parameterNames = CharOperation.splitOn(PARAMETER_SEPARATOR, key, start, slash);
			}
			
			slash = slash + 3;
			last = slash - 1;
			
			this.modifiers = key[last-1] + (key[last]<<16);
		} else {
			this.modifiers = ClassFileConstants.AccPublic;
		}
	}
	
	/**
	 * <p>Creates a constructor index key based on the given information to be placed in the index.</p>
	 * 
	 * @param typeName Name of the type the constructor is for
	 * @param parameterCount Number of parameters for the constructor, or -1 for a default constructor
	 * @param parameterTypes Type names of the parameters, should be same length as <code>parameterCount</code>
	 * @param parameterNames Names of the parameters, should be same length as <code>parameterCount</code>
	 * @param modifiers Modifiers to the constructor such as public/private
	 * 
	 * @return Constructor index key based on the given information to be used in an index
	 */
	public static char[] createDeclarationIndexKey(
			char[] typeName,
			int parameterCount,
			char[][] parameterTypes,
			char[][] parameterNames,
			int modifiers) {
		
		char[] countChars;
		char[] parameterTypesChars = null;
		char[] parameterNamesChars = null;
		
		//use pre-made char array for arg counts less then 10, else build a new one
		countChars = parameterCount < 10 ? COUNTS[parameterCount] : ("/" + String.valueOf(parameterCount)).toCharArray(); //$NON-NLS-1$
		
		if (parameterCount > 0) {
			//get param types
			if (parameterTypes != null && parameterTypes.length == parameterCount) {
				char[][] parameterTypeErasures = new char[parameterCount][];
				for (int i = 0; i < parameterTypes.length; i++) {
					parameterTypeErasures[i] = getTypeErasure(parameterTypes[i]);
				}
				parameterTypesChars = CharOperation.concatWith(parameterTypeErasures, PARAMETER_SEPARATOR, false);
			}
			
			//get param names
			if (parameterNames != null && parameterNames.length == parameterCount) {
				parameterNamesChars = CharOperation.concatWith(parameterNames, PARAMETER_SEPARATOR);
			}
		}
		
		//get lengths
		int typeNameLength = typeName == null ? 0 : typeName.length;
		int countCharsLength = countChars.length;
		int parameterTypesLength = parameterTypesChars == null ? 0 : parameterTypesChars.length;
		int parameterNamesLength = parameterNamesChars == null ? 0 : parameterNamesChars.length;
		
		int resultLength = typeNameLength + countCharsLength;
		
		//add length for parameters and separators
		if (parameterCount > 0) {
			resultLength += parameterTypesLength + parameterNamesLength + 2; //SEPARATOR=1 + SEPARATOR=1
		}
		
		//add length for modifiers and separator
		resultLength += 3;
		
		//create result char array
		char[] result = new char[resultLength];
		
		//add type name to result
		int pos = 0;
		if (typeNameLength > 0) {
			System.arraycopy(typeName, 0, result, pos, typeNameLength);
			pos += typeNameLength;
		}
		
		//add param count to result
		if (countCharsLength > 0) {
			System.arraycopy(countChars, 0, result, pos, countCharsLength);
			pos += countCharsLength;
		}
		
		// if params add to result
		if (parameterCount > 0) {
			//add param types
			result[pos++] = SEPARATOR;
			if (parameterTypesLength > 0) {
				System.arraycopy(parameterTypesChars, 0, result, pos, parameterTypesLength);
				
				pos += parameterTypesLength;
			}
			
			//add param names
			result[pos++] = SEPARATOR;
			if (parameterNamesLength > 0) {
				System.arraycopy(parameterNamesChars, 0, result, pos, parameterNamesLength);
				pos += parameterNamesLength;
			}

		}
		
		//add modifiers
		result[pos++] = SEPARATOR;
		result[pos++] = (char) modifiers;
		result[pos++] = (char) (modifiers>>16);
		
		return result;
	}
	
	private static char[] getTypeErasure(char[] typeName) {
		char[] typeErasurename = new char[0];
		if(typeName != null) {
			int index;
			if ((index = CharOperation.indexOf('<', typeName)) == -1) return typeName;
			
			int length = typeName.length;
			typeErasurename = new char[length - 2];
			
			System.arraycopy(typeName, 0, typeErasurename, 0, index);
			
			int depth = 1;
			for (int i = index + 1; i < length; i++) {
				switch (typeName[i]) {
					case '<':
						depth++;
						break;
					case '>':
						depth--;
						break;
					default:
						if (depth == 0) {
							typeErasurename[index++] = typeName[i];
						}
						break;
				}
			}
			
			System.arraycopy(typeErasurename, 0, typeErasurename = new char[index], 0, index);
		}
		return typeErasurename;
	}
}
