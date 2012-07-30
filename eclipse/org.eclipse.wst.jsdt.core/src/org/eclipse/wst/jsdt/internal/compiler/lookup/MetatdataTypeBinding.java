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
package org.eclipse.wst.jsdt.internal.compiler.lookup;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.wst.jsdt.internal.compiler.util.Util;
import org.eclipse.wst.jsdt.internal.oaametadata.ClassData;
import org.eclipse.wst.jsdt.internal.oaametadata.Method;
import org.eclipse.wst.jsdt.internal.oaametadata.Parameter;
import org.eclipse.wst.jsdt.internal.oaametadata.Property;

public class MetatdataTypeBinding extends SourceTypeBinding {

	ClassData classData;
	LibraryAPIsScope libraryScope;
	boolean methodsBuilt=false;
	boolean fieldsBuilt=false;
	
public MetatdataTypeBinding(char[][] compoundName, PackageBinding fPackage, ClassData classData,  LibraryAPIsScope scope) {
	this.compoundName = compoundName;
	this.fPackage = fPackage;
	this.fileName = scope.getFileName();
	this.sourceName = compoundName[0];

	this.classData=classData;
	// expect the fields & methods to be initialized correctly later
	this.fields = Binding.NO_FIELDS;
	this.methods = Binding.NO_METHODS;

	this.scope=this.libraryScope = scope;
	

}

  

private void buildFields() {
	FieldBinding prototype = new FieldBinding(TypeConstants.PROTOTYPE, TypeBinding.UNKNOWN, modifiers | ExtraCompilerModifiers.AccUnresolved, this);
	Property[] classFields = this.classData.getFields();
	int size = classFields.length;
	if (size == 0) {
		 setFields(new FieldBinding[]{prototype});
		return;
	}

	// iterate the field declarations to create the bindings, lose all duplicates
	FieldBinding[] fieldBindings = new FieldBinding[size+1];
	HashtableOfObject knownFieldNames = new HashtableOfObject(size);
	boolean duplicate = false;
	int count = 0;
	for (int i = 0; i < size; i++) {
		Property field =classFields[i];

		char [] fieldName=field.name.toCharArray();
		int modifiers=0;
		if (field.isStatic())
			modifiers|=ClassFileConstants.AccStatic;
			TypeBinding fieldTypeBinding = libraryScope.resolveType(field.dataType);
			
			FieldBinding fieldBinding = new  FieldBinding(fieldName, fieldTypeBinding, modifiers | ExtraCompilerModifiers.AccUnresolved, this);
			fieldBinding.id = count;
			// field's type will be resolved when needed for top level types
//			checkAndSetModifiersForField(fieldBinding, field);

			if (knownFieldNames.containsKey(fieldName)) {
				duplicate = true;
				FieldBinding previousBinding = (FieldBinding) knownFieldNames.get(fieldName);
//				if (previousBinding != null) {
//					for (int f = 0; f < i; f++) {
//						InferredAttribute previousField = inferredType.attributes[f];
//						if (previousField.binding == previousBinding) {
//							libraryScope.problemReporter().duplicateFieldInType(this, previousField);
//							previousField.binding = null;
//							break;
//						}
//					}
//				}
				knownFieldNames.put(fieldName, null); // ensure that the duplicate field is found & removed
//				libraryScope.problemReporter().duplicateFieldInType(this, field);
//				field.binding = null;
			} else {
				knownFieldNames.put(fieldName, fieldBinding);
				// remember that we have seen a field with this name
				if (fieldBinding != null)
					fieldBindings[count++] = fieldBinding;
			}
	}
	fieldBindings[count++]=prototype;
	// remove duplicate fields
	if (duplicate) {
		FieldBinding[] newFieldBindings = new FieldBinding[fieldBindings.length];
		// we know we'll be removing at least 1 duplicate name
		size = count;
		count = 0;
		for (int i = 0; i < size; i++) {
			FieldBinding fieldBinding = fieldBindings[i];
			if (knownFieldNames.get(fieldBinding.name) != null) {
				fieldBinding.id = count;
				newFieldBindings[count++] = fieldBinding;
			}
		}
		fieldBindings = newFieldBindings;
	}
	if (count != fieldBindings.length)
		System.arraycopy(fieldBindings, 0, fieldBindings = new FieldBinding[count], 0, count);
	setFields(fieldBindings);
}





private void buildMethods() {

	int methodsSize = (this.classData.methods!=null)?this.classData.methods.length :0;
	int constructorsSize = (this.classData.constructors!=null)?this.classData.constructors.length :0;

	if (constructorsSize + methodsSize ==0 ) {
		setMethods(Binding.NO_METHODS);
		return;
	}

	int count =  0;
	MethodBinding[] methodBindings = new MethodBinding[methodsSize+constructorsSize];
	// create bindings for source methods
	for (int i = 0; i < methodsSize; i++) {
		    Method method = this.classData.methods[i];
			MethodBinding methodBinding = createMethodBinding(method,false);

		    
			methodBindings[count++] = methodBinding;
	}
	for (int i = 0; i < constructorsSize; i++) {
	    Method method = this.classData.constructors[i];
		MethodBinding methodBinding = createMethodBinding(method,true);
		methodBindings[count++] = methodBinding;
    }
	if (count != methodBindings.length)
		System.arraycopy(methodBindings, 0, methodBindings = new MethodBinding[count], 0, count);
	tagBits &= ~TagBits.AreMethodsSorted; // in case some static imports reached already into this type
	setMethods(methodBindings);
}



private MethodBinding createMethodBinding(Method method, boolean isConstructor) {
	int modifiers =  ExtraCompilerModifiers.AccUnresolved;
	if (method.isStatic())
		modifiers|=ClassFileConstants.AccStatic;
	modifiers|=ClassFileConstants.AccPublic;
	MethodBinding methodBinding =null;
	if (isConstructor)
	{
		methodBinding =new MethodBinding(modifiers, null, this);
		methodBinding.tagBits|=TagBits.IsConstructor;
	}
	else
	{
		TypeBinding returnType = (method.returns!=null ) ? this.libraryScope.resolveType(method.returns.dataType) : TypeBinding.UNKNOWN;
//		TypeBinding returnType =
//		 (method instanceof FunctionDeclaration && ((FunctionDeclaration)method).returnType!=null && method.inferredMethod!=null)?method.inferredType.resolveType(this,((FunctionDeclaration)method).returnType):TypeBinding.ANY;
		
		methodBinding =
				new MethodBinding(modifiers, method.name.toCharArray(), returnType, null, this);
		methodBinding.createFunctionTypeBinding(this.libraryScope);

	}

	methodBinding.oaaMethod=method;	
	return methodBinding;
}



public int kind() {
	return Binding.TYPE;
}
public char[] computeUniqueKey(boolean isLeaf) {
	char[] uniqueKey = super.computeUniqueKey(isLeaf);
	if (uniqueKey.length == 2) return uniqueKey; // problem type's unique key is "L;"
	if (Util.isClassFileName(this.fileName)
			||org.eclipse.wst.jsdt.internal.core.util.Util.isMetadataFileName(new String(this.fileName))) 
		return uniqueKey; // no need to insert compilation unit name for a .class file

	// insert compilation unit name if the type name is not the main type name
	int end = CharOperation.lastIndexOf('.', this.fileName);
	if (end != -1) {
		int start = CharOperation.lastIndexOf('/', this.fileName) + 1;
		char[] mainTypeName = CharOperation.subarray(this.fileName, start, end);
		start = CharOperation.lastIndexOf('/', uniqueKey) + 1;
		if (start == 0)
			start = 1; // start after L
		end = CharOperation.indexOf('$', uniqueKey, start);
		if (end == -1)
			end = CharOperation.indexOf('<', uniqueKey, start);
		if (end == -1)
			end = CharOperation.indexOf(';', uniqueKey, start);
		char[] topLevelType = CharOperation.subarray(uniqueKey, start, end);
		if (!CharOperation.equals(topLevelType, mainTypeName)) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(uniqueKey, 0, start);
			buffer.append(mainTypeName);
			buffer.append('~');
			buffer.append(topLevelType);
			buffer.append(uniqueKey, end, uniqueKey.length - end);
			int length = buffer.length();
			uniqueKey = new char[length];
			buffer.getChars(0, length, uniqueKey, 0);
			return uniqueKey;
		}
	}
	return uniqueKey;
}
void faultInTypesForFieldsAndMethods() {
	// check @Deprecated annotation
//	getAnnotationTagBits(); // marks as deprecated by side effect
	ReferenceBinding enclosingType = this.enclosingType();
	if (enclosingType != null && enclosingType.isViewedAsDeprecated() && !this.isDeprecated())
		this.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	fields();
	methods();

//	for (int i = 0, length = this.memberTypes.length; i < length; i++)
//		((SourceTypeBinding) this.memberTypes[i]).faultInTypesForFieldsAndMethods();
}

// NOTE: the type of each field of a source type is resolved when needed
public FieldBinding[] fields() {
	if ((this.tagBits & TagBits.AreFieldsComplete) == 0)
	{
       if (!fieldsBuilt)
       {
    	   buildFields();
    	   fieldsBuilt=true;
       }
		
		int failed = 0;
		FieldBinding[] resolvedFields = this.fields;
		try {
			// lazily sort fields
			if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
				int length = this.fields.length;
				if (length > 1)
					ReferenceBinding.sortFields(this.fields, 0, length);
				this.tagBits |= TagBits.AreFieldsSorted;
			}
			for (int i = 0, length = this.fields.length; i < length; i++) {
				if (resolveTypeFor(this.fields[i]) == null) {
					// do not alter original field array until resolution is over, due to reentrance (143259)
					if (resolvedFields == this.fields) {
						System.arraycopy(this.fields, 0, resolvedFields = new FieldBinding[length], 0, length);
					}
					resolvedFields[i] = null;
					failed++;
				}
			}
		} finally {
			if (failed > 0) {
				// ensure fields are consistent reqardless of the error
				int newSize = resolvedFields.length - failed;
				if (newSize == 0)
					return this.fields = Binding.NO_FIELDS;

				FieldBinding[] newFields = new FieldBinding[newSize];
				for (int i = 0, j = 0, length = resolvedFields.length; i < length; i++) {
					if (resolvedFields[i] != null)
						newFields[j++] = resolvedFields[i];
				}
				this.fields = newFields;
			}
		}
		this.tagBits |= TagBits.AreFieldsComplete;
	}
	return this.fields;
}

public MethodBinding[] getDefaultAbstractMethods() {
	int count = 0;
	for (int i = this.methods.length; --i >= 0;)
		if (this.methods[i].isDefaultAbstract())
			count++;
	if (count == 0) return Binding.NO_METHODS;

	MethodBinding[] result = new MethodBinding[count];
	count = 0;
	for (int i = this.methods.length; --i >= 0;)
		if (this.methods[i].isDefaultAbstract())
			result[count++] = this.methods[i];
	return result;
}

public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
		long range;
		if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
//			nextMethod: 
				for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
//				if (method.parameters.length == argCount) {
//					TypeBinding[] toMatch = method.parameters;
//					for (int iarg = 0; iarg < argCount; iarg++)
//						if (toMatch[iarg] != argumentTypes[iarg])
//							continue nextMethod;
					return method;
//				}
			}
		}
	} else {
	       if (!methodsBuilt)
	       {
	    	   buildMethods();
	    	   methodsBuilt=true;
	       }

		// lazily sort methods
		if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
			int length = this.methods.length;
			if (length > 1)
				ReferenceBinding.sortMethods(this.methods, 0, length);
			this.tagBits |= TagBits.AreMethodsSorted;
		}
		long range;
		if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0) {
//			nextMethod: 
				for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (resolveTypesFor(method) == null || method.returnType == null) {
					methods();
					return getExactConstructor(argumentTypes);  // try again since the problem methods have been removed
				}
//				if (method.parameters.length == argCount) {
//					TypeBinding[] toMatch = method.parameters;
//					for (int iarg = 0; iarg < argCount; iarg++)
//						if (toMatch[iarg] != argumentTypes[iarg])
//							continue nextMethod;
//					return method;
//				}
				return method;
			}
		}
	}
	return null;
}


public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
	// sender from refScope calls recordTypeReference(this)
//	int argCount = argumentTypes.length;
	boolean foundNothing = true;

	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) { // have resolved all arg types & return type of the methods
	       long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
//			nextMethod:
				for (int imethod = (int)range, end = (int)(range >> 32); imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				foundNothing = false; // inner type lookups must know that a method with this name exists
//				if (method.parameters.length == argCount) {
//					TypeBinding[] toMatch = method.parameters;
//					for (int iarg = 0; iarg < argCount; iarg++)
//						if (toMatch[iarg] != argumentTypes[iarg])
//						{
//						if (toMatch[iarg].id!=TypeIds.T_any && argumentTypes[iarg].id!=TypeIds.T_any)
//						continue nextMethod;
//					}
//					return method;
//				}
				return method;
			}
		}
	} else {
	       if (!methodsBuilt)
	       {
	    	   buildMethods();
	    	   methodsBuilt=true;
	       }
		// lazily sort methods
		if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
			int length = this.methods.length;
			if (length > 1)
				ReferenceBinding.sortMethods(this.methods, 0, length);
			this.tagBits |= TagBits.AreMethodsSorted;
		}

		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			// check unresolved method
			int start = (int) range, end = (int) (range >> 32);
			for (int imethod = start; imethod <= end; imethod++) {
				MethodBinding method = this.methods[imethod];
				if (resolveTypesFor(method) == null || method.returnType == null) {
					methods();
					return getExactMethod(selector, argumentTypes, refScope); // try again since the problem methods have been removed
				}
			}
			// check dup collisions
			boolean isSource15 = this.scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
			for (int i = start; i <= end; i++) {
				MethodBinding method1 = this.methods[i];
				for (int j = end; j > i; j--) {
					MethodBinding method2 = this.methods[j];
					boolean paramsMatch = isSource15
						? method1.areParametersEqual(method2)
						: method1.areParametersEqual(method2);
					if (paramsMatch) {
						methods();
						return getExactMethod(selector, argumentTypes, refScope); // try again since the problem methods have been removed
					}
				}
			}
			return this.methods[start];
//			nextMethod: for (int imethod = start; imethod <= end; imethod++) {
//				FunctionBinding method = this.methods[imethod];
//				TypeBinding[] toMatch = method.parameters;
//				if (toMatch.length == argCount) {
//					for (int iarg = 0; iarg < argCount; iarg++)
//						if (toMatch[iarg] != argumentTypes[iarg])
//							continue nextMethod;
//					return method;
//				}
//			}
		}
	}

	if (foundNothing) {
		if (this.superclass != null && this.superclass!=this) {
			if (refScope != null)
				refScope.recordTypeReference(this.superclass);
			return this.superclass.getExactMethod(selector, argumentTypes, refScope);
		}
	}
	return null;
}


public FieldBinding getField(char[] fieldName, boolean needResolve) {

	if ((this.tagBits & TagBits.AreFieldsComplete) != 0)
		return ReferenceBinding.binarySearch(fieldName, this.fields);

    if (!fieldsBuilt)
    {
 	   buildFields();
 	  fieldsBuilt=true;
    }

	
	// lazily sort fields
	if ((this.tagBits & TagBits.AreFieldsSorted) == 0) {
		int length = this.fields.length;
		if (length > 1)
			ReferenceBinding.sortFields(this.fields, 0, length);
		this.tagBits |= TagBits.AreFieldsSorted;
	}
	// always resolve anyway on source types
	FieldBinding field = ReferenceBinding.binarySearch(fieldName, this.fields);
	if (field != null) {
		FieldBinding result = null;
		try {
			result = resolveTypeFor(field);
			return result;
		} finally {
			if (result == null) {
				// ensure fields are consistent reqardless of the error
				int newSize = this.fields.length - 1;
				if (newSize == 0) {
					this.fields = Binding.NO_FIELDS;
				} else {
					FieldBinding[] newFields = new FieldBinding[newSize];
					int index = 0;
					for (int i = 0, length = this.fields.length; i < length; i++) {
						FieldBinding f = this.fields[i];
						if (f == field) continue;
						newFields[index++] = f;
					}
					this.fields = newFields;
				}
			}
		}
	}
	return null;
}

public MethodBinding[] getMethods(char[] selector) {
	if ((this.tagBits & TagBits.AreMethodsComplete) != 0) {
		long range;
		if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
			int start = (int) range, end = (int) (range >> 32);
			int length = end - start + 1;
			MethodBinding[] result;
			System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
			return result;
		} else {
			return Binding.NO_METHODS;
		}
	}
	
    if (!methodsBuilt)
    {
 	   buildMethods();
 	  methodsBuilt=true;
    }
	
	// lazily sort methods
	if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
		int length = this.methods.length;
		if (length > 1)
			ReferenceBinding.sortMethods(this.methods, 0, length);
		this.tagBits |= TagBits.AreMethodsSorted;
	}
	MethodBinding[] result;
	long range;
	if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0) {
		int start = (int) range, end = (int) (range >> 32);
		for (int i = start; i <= end; i++) {
			MethodBinding method = this.methods[i];
			if (resolveTypesFor(method) == null || method.returnType == null) {
				methods();
				return getMethods(selector); // try again since the problem methods have been removed
			}
		}
		int length = end - start + 1;
		System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
	} else {
		return Binding.NO_METHODS;
	}
	boolean isSource15 = this.libraryScope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
	for (int i = 0, length = result.length - 1; i < length; i++) {
		MethodBinding method = result[i];
		for (int j = length; j > i; j--) {
			boolean paramsMatch = isSource15
				? method.areParametersEqual(result[j])
				: method.areParametersEqual(result[j]);
			if (paramsMatch) {
				methods();
				return getMethods(selector); // try again since the duplicate methods have been removed
			}
		}
	}
	return result;
}

/**
 * Returns true if a type is identical to another one,
 * or for generic types, true if compared to its raw type.
 */
public boolean isEquivalentTo(TypeBinding otherType) {

	if (this == otherType) return true;
	if (otherType == null) return false;
	return false;
}
public ReferenceBinding[] memberTypes() {
	if (this.nextType==null)
		return this.memberTypes;
	
	ReferenceBinding[] moreTypes=this.nextType.memberTypes();
	ReferenceBinding[] combinedTypes=new ReferenceBinding[this.memberTypes.length+moreTypes.length];
	System.arraycopy(this.memberTypes, 0, combinedTypes, 0, this.memberTypes.length);
	System.arraycopy(moreTypes, 0, combinedTypes, this.memberTypes.length, moreTypes.length);

	return combinedTypes;

}
public boolean hasMemberTypes() {
    boolean hasMembers= this.memberTypes!=null &&  this.memberTypes.length > 0;
    if (!hasMembers && this.nextType!=null)
    	hasMembers=this.nextType.hasMemberTypes();
    return hasMembers;
}
// NOTE: the return type, arg & exception types of each method of a source type are resolved when needed
public MethodBinding[] methods() {

	if ((this.tagBits & TagBits.AreMethodsComplete) == 0) {
	    if (!methodsBuilt)
	    {
	 	   buildMethods();
	 	  methodsBuilt=true;
	    }
	    // lazily sort methods
		if ((this.tagBits & TagBits.AreMethodsSorted) == 0) {
			int length = this.methods.length;
			if (length > 1)
				ReferenceBinding.sortMethods(this.methods, 0, length);
			this.tagBits |= TagBits.AreMethodsSorted;
		}
		int failed = 0;
		MethodBinding[] resolvedMethods = this.methods;
		try {
			for (int i = 0, length = this.methods.length; i < length; i++) {
				if (resolveTypesFor(this.methods[i]) == null) {
					// do not alter original method array until resolution is over, due to reentrance (143259)
					if (resolvedMethods == this.methods) {
						System.arraycopy(this.methods, 0,
								resolvedMethods = new MethodBinding[length], 0,
								length);
					}
					resolvedMethods[i] = null; // unable to resolve parameters
					failed++;
				}
			}

			// find & report collision cases
			
			boolean complyTo15 = 
				(this.libraryScope!=null && this.libraryScope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5);
			for (int i = 0, length = this.methods.length; i < length; i++) {
				MethodBinding method = resolvedMethods[i];
				if (method == null)
					continue;
				char[] selector = method.selector;
//				AbstractMethodDeclaration methodDecl = null;
				nextSibling: for (int j = i + 1; j < length; j++) {
					MethodBinding method2 = resolvedMethods[j];
					if (method2 == null)
						continue nextSibling;
					if (!CharOperation.equals(selector, method2.selector))
						break nextSibling; // methods with same selector are contiguous

					if (complyTo15 && method.returnType != null
							&& method2.returnType != null) {
						// 8.4.2, for collision to be detected between m1 and m2:
						// signature(m1) == signature(m2) i.e. same arity, same type parameter count, can be substituted
						// signature(m1) == erasure(signature(m2)) or erasure(signature(m1)) == signature(m2)
						TypeBinding[] params1 = method.parameters;
						TypeBinding[] params2 = method2.parameters;
						int pLength = params1.length;
						if (pLength != params2.length)
							continue nextSibling;

						boolean equalTypeVars = true;
						MethodBinding subMethod = method2;
						if (!equalTypeVars) {
							MethodBinding temp = method2;
							if (temp != null) {
								equalTypeVars = true;
								subMethod = temp;
							}
						}
						boolean equalParams = method
								.areParametersEqual(subMethod);
						if (equalParams && equalTypeVars) {
							// duplicates regardless of return types
						} else if (method.returnType == subMethod.returnType
								&& (equalParams || method
										.areParametersEqual(method2))) {
							// name clash for sure if not duplicates, report as duplicates
						} else if (pLength > 0) {
							// check to see if the erasure of either method is equal to the other
							int index = pLength;
							for (; --index >= 0;) {
								if (params1[index] != params2[index])
									break;
							}
							if (index >= 0 && index < pLength) {
								for (index = pLength; --index >= 0;)
									if (params1[index] != params2[index])
										break;
							}
							if (index >= 0)
								continue nextSibling;
						}
					} else if (!method.areParametersEqual(method2)) { // prior to 1.5, parameter identity meant a collision case
						continue nextSibling;
					}
					// report duplicate
//					if (methodDecl == null) {
//						methodDecl = method.sourceMethod(); // cannot be retrieved after binding is lost & may still be null if method is special
//						if (methodDecl != null && methodDecl.binding != null) { // ensure its a valid user defined method
//							if (isEnumSpecialMethod) {
//								this.libraryScope.problemReporter()
//										.duplicateEnumSpecialMethod(this,
//												methodDecl);
//							} else {
//								this.libraryScope
//										.problemReporter()
//										.duplicateMethodInType(this, methodDecl);
//							}
//							methodDecl.binding = null;
//							// do not alter original method array until resolution is over, due to reentrance (143259)
//							if (resolvedMethods == this.methods) {
//								System
//										.arraycopy(
//												this.methods,
//												0,
//												resolvedMethods = new FunctionBinding[length],
//												0, length);
//							}
//							resolvedMethods[i] = null;
//							failed++;
//						}
//					}
//					AbstractMethodDeclaration method2Decl = method2
//							.sourceMethod();
//					if (method2Decl != null && method2Decl.binding != null) { // ensure its a valid user defined method
//						if (isEnumSpecialMethod) {
//							this.libraryScope.problemReporter()
//									.duplicateEnumSpecialMethod(this,
//											method2Decl);
//						} else {
//							this.libraryScope.problemReporter().duplicateMethodInType(
//									this, method2Decl);
//						}
//						method2Decl.binding = null;
//						// do not alter original method array until resolution is over, due to reentrance (143259)
//						if (resolvedMethods == this.methods) {
//							System
//									.arraycopy(
//											this.methods,
//											0,
//											resolvedMethods = new FunctionBinding[length],
//											0, length);
//						}
//						resolvedMethods[j] = null;
//						failed++;
//					}
				}
//				if (method.returnType == null && methodDecl == null) { // forget method with invalid return type... was kept to detect possible collisions
//					methodDecl = method.sourceMethod();
//					if (methodDecl != null) {
//						methodDecl.binding = null;
//					}
//					// do not alter original method array until resolution is over, due to reentrance (143259)
//					if (resolvedMethods == this.methods) {
//						System.arraycopy(this.methods, 0,
//								resolvedMethods = new FunctionBinding[length], 0,
//								length);
//					}
//					resolvedMethods[i] = null;
//					failed++;
//				}
			}
		} finally {
			if (failed > 0) {
				int newSize = resolvedMethods.length - failed;
				if (newSize == 0) {
					this.methods = Binding.NO_METHODS;
				} else {
					MethodBinding[] newMethods = new MethodBinding[newSize];
					for (int i = 0, j = 0, length = resolvedMethods.length; i < length; i++)
						if (resolvedMethods[i] != null)
							newMethods[j++] = resolvedMethods[i];
					this.methods = newMethods;
				}
			}

			// handle forward references to potential default abstract methods
			//		addDefaultAbstractMethods();
			this.tagBits |= TagBits.AreMethodsComplete;
		}
	}
	if (this.nextType!=null)
	{
		MethodBinding[] moreMethods=this.nextType.methods();
		MethodBinding[] combinedMethods=new MethodBinding[this.methods.length+moreMethods.length];
		System.arraycopy(this.methods, 0, combinedMethods, 0, this.methods.length);
		System.arraycopy(moreMethods, 0, combinedMethods, this.methods.length, moreMethods.length);

		return combinedMethods;

	}
	else
		return this.methods;

}
private FieldBinding resolveTypeFor(FieldBinding field) {
	if ((field.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return field;

	if (isViewedAsDeprecated() && !field.isDeprecated())
		field.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	if (hasRestrictedAccess())
		field.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;
	return field;
}
public MethodBinding resolveTypesFor(MethodBinding method) {
	if ((method.modifiers & ExtraCompilerModifiers.AccUnresolved) == 0)
		return method;
	
	if (isViewedAsDeprecated() && !method.isDeprecated())
		method.modifiers |= ExtraCompilerModifiers.AccDeprecatedImplicitly;
	if (hasRestrictedAccess())
		method.modifiers |= ExtraCompilerModifiers.AccRestrictedAccess;

	Method methodDecl = method.oaaMethod;
	if (methodDecl == null) return null; // method could not be resolved in previous iteration


	boolean foundArgProblem = false;
	Parameter[] arguments = methodDecl.parameters;
	if (arguments != null) {
		int size = arguments.length;
		method.parameters = Binding.NO_PARAMETERS;
		TypeBinding[] newParameters = new TypeBinding[size];
		for (int i = 0; i < size; i++) {
			Parameter arg = arguments[i];
			TypeBinding parameterType = TypeBinding.UNKNOWN;
			parameterType = libraryScope.resolveType(arg.dataType) ;
			
			
//			else 
			if (parameterType == TypeBinding.VOID) {
//				scope.problemReporter().argumentTypeCannotBeVoid(this, methodDecl, arg);
				foundArgProblem = true;
			} else {
				TypeBinding leafType = parameterType.leafComponentType();
				newParameters[i] = parameterType;
			}
		}
		// only assign parameters if no problems are found
		if (!foundArgProblem)
			method.parameters = newParameters;
	}

	boolean foundReturnTypeProblem = false;
	if (foundArgProblem) {
		method.parameters = Binding.NO_PARAMETERS; // see 107004
		// nullify type parameter bindings as well as they have a backpointer to the method binding
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=81134)
		return null;
	}
	if (foundReturnTypeProblem)
		return method; // but its still unresolved with a null return type & is still connected to its method declaration

	method.modifiers &= ~ExtraCompilerModifiers.AccUnresolved;
	return method;
}
public void setFields(FieldBinding[] fields) {
//	if (this.nextType!=null)
//		throw new UnimplementedException("should not get here"); //$NON-NLS-1$

	this.fields = fields;
}
public void setMethods(MethodBinding[] methods) {
//	if (this.nextType!=null)
//		throw new UnimplementedException("should not get here"); //$NON-NLS-1$
	this.methods = methods;
}
public final int sourceEnd() {
return -1;
}
public final int sourceStart() {
	return -1;
}
public ReferenceBinding superclass() {
		return this.superclass;

}
public String toString() {
    StringBuffer buffer = new StringBuffer(30);
    buffer.append("(id="); //$NON-NLS-1$
    if (this.id == TypeIds.NoId)
        buffer.append("NoId"); //$NON-NLS-1$
    else
        buffer.append(this.id);
    buffer.append(")\n"); //$NON-NLS-1$
	if (isDeprecated()) buffer.append("deprecated "); //$NON-NLS-1$
	if (isPublic()) buffer.append("public "); //$NON-NLS-1$
	if (isPrivate()) buffer.append("private "); //$NON-NLS-1$
	if (isStatic() && isNestedType()) buffer.append("static "); //$NON-NLS-1$

	if (isClass()) buffer.append("class "); //$NON-NLS-1$
	else buffer.append("interface "); //$NON-NLS-1$
	buffer.append((this.compoundName != null) ? CharOperation.toString(this.compoundName) : "UNNAMED TYPE"); //$NON-NLS-1$

	buffer.append("\n\textends "); //$NON-NLS-1$
	buffer.append((this.superclass != null) ? this.superclass.debugName() : "NULL TYPE"); //$NON-NLS-1$

	if (enclosingType() != null) {
		buffer.append("\n\tenclosing type : "); //$NON-NLS-1$
		buffer.append(enclosingType().debugName());
	}

	if (this.fields != null) {
		if (this.fields != Binding.NO_FIELDS) {
			buffer.append("\n/*   fields   */"); //$NON-NLS-1$
			for (int i = 0, length = this.fields.length; i < length; i++)
			    buffer.append('\n').append((this.fields[i] != null) ? this.fields[i].toString() : "NULL FIELD"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL FIELDS"); //$NON-NLS-1$
	}

	if (this.methods != null) {
		if (this.methods != Binding.NO_METHODS) {
			buffer.append("\n/*   methods   */"); //$NON-NLS-1$
			for (int i = 0, length = this.methods.length; i < length; i++)
				buffer.append('\n').append((this.methods[i] != null) ? this.methods[i].toString() : "NULL METHOD"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL METHODS"); //$NON-NLS-1$
	}

	if (this.memberTypes != null) {
		if (this.memberTypes != Binding.NO_MEMBER_TYPES) {
			buffer.append("\n/*   members   */"); //$NON-NLS-1$
			for (int i = 0, length = this.memberTypes.length; i < length; i++)
				buffer.append('\n').append((this.memberTypes[i] != null) ? this.memberTypes[i].toString() : "NULL TYPE"); //$NON-NLS-1$
		}
	} else {
		buffer.append("NULL MEMBER TYPES"); //$NON-NLS-1$
	}

	buffer.append("\n\n"); //$NON-NLS-1$
	return buffer.toString();
}
void verifyMethods(MethodVerifier verifier) {
	//verifier.verify(this);

//	for (int i = this.memberTypes.length; --i >= 0;)
//		 ((SourceTypeBinding) this.memberTypes[i]).verifyMethods(verifier);
}

public AbstractMethodDeclaration sourceMethod(MethodBinding binding) {
	return null;
}

public void addMethod(MethodBinding binding)
{
	int length=this.methods.length;
	System.arraycopy(this.methods, 0, this.methods=new MethodBinding[length+1], 0, length);
	this.methods[length]=binding;

}

public void cleanup()
{
this.scope=null;
this.classScope=null;
super.cleanup();
// clean up should be called for each compilation unit, so it shouldnt be necessary to chain the cleanups
//if (this.nextType!=null)
//	this.nextType.cleanup();
}


public boolean contains(ReferenceBinding binding)
{
	return false;
}

public ClassData getClassData()
{
	return this.classData;
}


public LibraryAPIsScope getLibraryAPIsScope()
{
	return this.libraryScope;
}

}
