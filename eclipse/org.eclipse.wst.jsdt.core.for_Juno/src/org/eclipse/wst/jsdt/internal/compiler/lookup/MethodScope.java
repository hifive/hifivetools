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
import org.eclipse.wst.jsdt.core.infer.InferredMethod;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.Argument;
import org.eclipse.wst.jsdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;

/**
 * Particular block scope used for methods, constructors or clinits, representing
 * its outermost blockscope. Note also that such a scope will be provided to enclose
 * field initializers subscopes as well.
 */
public class MethodScope extends BlockScope {

	public ReferenceContext referenceContext;
	public boolean isStatic; // method modifier or initializer one

	//fields used during name resolution
	public boolean isConstructorCall = false;
	public FieldBinding initializedField; // the field being initialized
	public int lastVisibleFieldID = -1; // the ID of the last field which got declared
	// note that #initializedField can be null AND lastVisibleFieldID >= 0, when processing instance field initializers.

	// flow analysis
	public int analysisIndex; // for setting flow-analysis id
	public boolean isPropagatingInnerClassEmulation;

	// for local variables table attributes
	public int lastIndex = 0;
	public long[] definiteInits = new long[4];
	public long[][] extraDefiniteInits = new long[4][];


	public static final char [] ARGUMENTS_NAME={'a','r','g','u','m','e','n','t','s'};

	public LocalVariableBinding argumentsBinding ;


	public MethodScope(Scope parent, ReferenceContext context, boolean isStatic) {

		super(METHOD_SCOPE, parent);
		locals = new LocalVariableBinding[5];
		this.referenceContext = context;
		this.isStatic = isStatic;
		this.startIndex = 0;
		argumentsBinding = new LocalVariableBinding(ARGUMENTS_NAME,TypeBinding.UNKNOWN,0,true);
		argumentsBinding.declaringScope=this;
	}

	/* Spec : 8.4.3 & 9.4
	 */
	private void checkAndSetModifiersForConstructor(MethodBinding methodBinding) {

		int modifiers = methodBinding.modifiers;
		final ReferenceBinding declaringClass = methodBinding.declaringClass;
	
//		if (((ConstructorDeclaration) referenceContext).isDefaultConstructor) {
		if ((methodBinding.modifiers&ExtraCompilerModifiers.AccIsDefaultConstructor)>0) {
			// certain flags are propagated from declaring class onto constructor
			final int DECLARING_FLAGS = ClassFileConstants.AccPublic|ClassFileConstants.AccProtected;
			final int VISIBILITY_FLAGS = ClassFileConstants.AccPrivate|ClassFileConstants.AccPublic|ClassFileConstants.AccProtected;
			int flags;
			if ((flags = declaringClass.modifiers & DECLARING_FLAGS) != 0) {
				modifiers &= ~VISIBILITY_FLAGS;
				modifiers |= flags; // propagate public/protected
			}
		}

		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & ExtraCompilerModifiers.AccJustFlag;

		// check for incompatible modifiers in the visibility bits, isolate the visibility bits
		int accessorBits = realModifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate);
		if ((accessorBits & (accessorBits - 1)) != 0) {
		
			// need to keep the less restrictive so disable Protected/Private as necessary
			if ((accessorBits & ClassFileConstants.AccPublic) != 0) {
				if ((accessorBits & ClassFileConstants.AccProtected) != 0)
					modifiers &= ~ClassFileConstants.AccProtected;
				if ((accessorBits & ClassFileConstants.AccPrivate) != 0)
					modifiers &= ~ClassFileConstants.AccPrivate;
			} else if ((accessorBits & ClassFileConstants.AccProtected) != 0 && (accessorBits & ClassFileConstants.AccPrivate) != 0) {
				modifiers &= ~ClassFileConstants.AccPrivate;
			}
		}

//		// if the receiver's declaring class is a private nested type, then make sure the receiver is not private (causes problems for inner type emulation)
//		if (declaringClass.isPrivate() && (modifiers & ClassFileConstants.AccPrivate) != 0)
//			modifiers &= ~ClassFileConstants.AccPrivate;

		methodBinding.modifiers = modifiers;
	}

	/* Spec : 8.4.3 & 9.4
	 */
	private void checkAndSetModifiersForMethod(MethodBinding methodBinding) {

		int modifiers = methodBinding.modifiers;
		final ReferenceBinding declaringClass = methodBinding.declaringClass;
	
		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & ExtraCompilerModifiers.AccJustFlag;

		// set the requested modifiers for a method in an interface/annotation
//		if (declaringClass.isInterface()) {
//			if ((realModifiers & ~(ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract)) != 0) {
//				if ((declaringClass.modifiers & ClassFileConstants.AccAnnotation) != 0)
//					problemReporter().illegalModifierForAnnotationMember((AbstractMethodDeclaration) referenceContext);
//				else
//					problemReporter().illegalModifierForInterfaceMethod((AbstractMethodDeclaration) referenceContext);
//			}
//			return;
//		}

		// check for incompatible modifiers in the visibility bits, isolate the visibility bits
		int accessorBits = realModifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate);
		if ((accessorBits & (accessorBits - 1)) != 0) {
			
			// need to keep the less restrictive so disable Protected/Private as necessary
			if ((accessorBits & ClassFileConstants.AccPublic) != 0) {
				if ((accessorBits & ClassFileConstants.AccProtected) != 0)
					modifiers &= ~ClassFileConstants.AccProtected;
				if ((accessorBits & ClassFileConstants.AccPrivate) != 0)
					modifiers &= ~ClassFileConstants.AccPrivate;
			} else if ((accessorBits & ClassFileConstants.AccProtected) != 0 && (accessorBits & ClassFileConstants.AccPrivate) != 0) {
				modifiers &= ~ClassFileConstants.AccPrivate;
			}
		}

		/* DISABLED for backward compatibility with javac (if enabled should also mark private methods as final)
		// methods from a final class are final : 8.4.3.3
		if (methodBinding.declaringClass.isFinal())
			modifiers |= AccFinal;
		*/
//		// static members are only authorized in a static member or top level type
//		if (((realModifiers & ClassFileConstants.AccStatic) != 0) && declaringClass.isNestedType() && !declaringClass.isStatic())
//			problemReporter().unexpectedStaticModifierForMethod(declaringClass, (AbstractMethodDeclaration) referenceContext);

		methodBinding.modifiers = modifiers;
	}

	MethodBinding createMethod(InferredMethod inferredMethod,SourceTypeBinding declaringClass) {
        boolean isConstructor=inferredMethod.isConstructor;
        if (isConstructor && declaringClass!=inferredMethod.inType.binding)
        	isConstructor=false;
		 MethodBinding binding = createMethod((AbstractMethodDeclaration) inferredMethod.getFunctionDeclaration(),inferredMethod.name,declaringClass, isConstructor,false); 
		 if (inferredMethod.isConstructor || declaringClass!=inferredMethod.inType.binding)
			 binding.allocationType=inferredMethod.inType.binding;
		 return binding;
	}

	public MethodBinding createMethod(AbstractMethodDeclaration method,char[] name,SourceTypeBinding declaringClass, boolean isConstructor, boolean isLocal) {

		MethodBinding methodBinding=null;
		// is necessary to ensure error reporting
		this.referenceContext = method;
		method.scope = this;
		int modifiers = method.modifiers | ExtraCompilerModifiers.AccUnresolved;
		if ((method.modifiers &(ClassFileConstants.AccPrivate | ClassFileConstants.AccProtected))==0)
			modifiers|=ClassFileConstants.AccPublic;
		if (method.inferredMethod!=null &&  method.inferredMethod.isStatic)
			modifiers|= ClassFileConstants.AccStatic;
		if (method.isConstructor() || isConstructor) {
			if (method.isDefaultConstructor() || isConstructor) {
				modifiers |= ExtraCompilerModifiers.AccIsDefaultConstructor;
			}
			methodBinding = new MethodBinding(modifiers, name, TypeBinding.UNKNOWN, null, declaringClass);
			methodBinding.tagBits|=TagBits.IsConstructor;
			checkAndSetModifiersForConstructor(methodBinding);
		} else {
			TypeBinding returnType =
				 (method.inferredType!=null)?method.inferredType.resolveType(this,method):TypeBinding.UNKNOWN;
			if (method.inferredType==null && method.inferredMethod!=null && method.inferredMethod.isConstructor
					&& method.inferredMethod.inType!=null) {
				returnType=method.inferredMethod.inType.resolveType(this,method);
			}
			
			//return type still null, return type is unknown
			if (returnType==null) {
				returnType=TypeBinding.UNKNOWN;
			}
			
			if (isLocal && method.selector!=null) {
				methodBinding =
					new LocalFunctionBinding(modifiers, name,returnType, null, declaringClass);
			} else{// not local method
				methodBinding =
					new MethodBinding(modifiers, name,returnType, null, declaringClass);
			}
			
			if (method.inferredMethod!=null) {
				methodBinding.tagBits |= TagBits.IsInferredType;
				if ((method.bits&ASTNode.IsInferredJsDocType)!=0) {
					methodBinding.tagBits |= TagBits.IsInferredJsDocType;
			
				}
			}
			methodBinding.createFunctionTypeBinding(this);
			if (method.inferredMethod!=null && method.inferredMethod.isConstructor) {
				methodBinding.tagBits|=TagBits.IsConstructor;
			}
			checkAndSetModifiersForMethod(methodBinding);
		}
		this.isStatic =methodBinding.isStatic();

		//set arguments
		Argument[] argTypes = method.arguments;
		int argLength = argTypes == null ? 0 : argTypes.length;
		if (argLength > 0 && compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5) {
			if (argTypes[--argLength].isVarArgs())
				methodBinding.modifiers |= ClassFileConstants.AccVarargs;
		}
	
		return methodBinding;
	}

	public FieldBinding findField(
		TypeBinding receiverType,
		char[] fieldName,
		InvocationSite invocationSite,
		boolean needResolve) {

		FieldBinding field = super.findField(receiverType, fieldName, invocationSite, needResolve);
		if (field == null)
			return null;
		if (!field.isValidBinding())
			return field; // answer the error field
		if (field.isStatic())
			return field; // static fields are always accessible

		if (!isConstructorCall || receiverType != enclosingSourceType())
			return field;

		if (invocationSite instanceof SingleNameReference)
			return new ProblemFieldBinding(
				field, // closest match
				field.declaringClass,
				fieldName,
				ProblemReasons.NonStaticReferenceInConstructorInvocation);
		if (invocationSite instanceof QualifiedNameReference) {
			// look to see if the field is the first binding
			QualifiedNameReference name = (QualifiedNameReference) invocationSite;
			if (name.binding == null)
				// only true when the field is the fieldbinding at the beginning of name's tokens
				return new ProblemFieldBinding(
					field, // closest match
					field.declaringClass,
					fieldName,
					ProblemReasons.NonStaticReferenceInConstructorInvocation);
		}
		return field;
	}

	public boolean isInsideConstructor() {

		return (referenceContext instanceof ConstructorDeclaration);
	}

	public boolean isInsideInitializer() {

		return (referenceContext instanceof TypeDeclaration);
	}

	public boolean isInsideInitializerOrConstructor() {

		return (referenceContext instanceof TypeDeclaration)
			|| (referenceContext instanceof ConstructorDeclaration);
	}

	/* Answer the problem reporter to use for raising new problems.
	 *
	 * Note that as a side-effect, this updates the current reference context
	 * (unit, type or method) in case the problem handler decides it is necessary
	 * to abort.
	 */
	public ProblemReporter problemReporter() {

		MethodScope outerMethodScope;
		if ((outerMethodScope = outerMostMethodScope()) == this) {
			ProblemReporter problemReporter = referenceCompilationUnit().problemReporter;
			problemReporter.referenceContext = referenceContext;
			return problemReporter;
		}
		return outerMethodScope.problemReporter();
	}

	public final int recordInitializationStates(FlowInfo flowInfo) {

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0) return -1;

		UnconditionalFlowInfo unconditionalFlowInfo = flowInfo.unconditionalInitsWithoutSideEffect();
		long[] extraInits = unconditionalFlowInfo.extra == null ?
				null : unconditionalFlowInfo.extra[0];
		long inits = unconditionalFlowInfo.definiteInits;
		checkNextEntry : for (int i = lastIndex; --i >= 0;) {
			if (definiteInits[i] == inits) {
				long[] otherInits = extraDefiniteInits[i];
				if ((extraInits != null) && (otherInits != null)) {
					if (extraInits.length == otherInits.length) {
						int j, max;
						for (j = 0, max = extraInits.length; j < max; j++) {
							if (extraInits[j] != otherInits[j]) {
								continue checkNextEntry;
							}
						}
						return i;
					}
				} else {
					if ((extraInits == null) && (otherInits == null)) {
						return i;
					}
				}
			}
		}

		// add a new entry
		if (definiteInits.length == lastIndex) {
			// need a resize
			System.arraycopy(
				definiteInits,
				0,
				(definiteInits = new long[lastIndex + 20]),
				0,
				lastIndex);
			System.arraycopy(
				extraDefiniteInits,
				0,
				(extraDefiniteInits = new long[lastIndex + 20][]),
				0,
				lastIndex);
		}
		definiteInits[lastIndex] = inits;
		if (extraInits != null) {
			extraDefiniteInits[lastIndex] = new long[extraInits.length];
			System.arraycopy(
				extraInits,
				0,
				extraDefiniteInits[lastIndex],
				0,
				extraInits.length);
		}
		return lastIndex++;
	}

	/* Answer the reference method of this scope, or null if initialization scoope.
	*/
	public AbstractMethodDeclaration referenceMethod() {

		if (referenceContext instanceof AbstractMethodDeclaration) return (AbstractMethodDeclaration) referenceContext;
		return null;
	}

	/* Answer the reference type of this scope.
	*
	* It is the nearest enclosing type of this scope.
	*/
	public TypeDeclaration referenceType() {
		if (parent instanceof ClassScope)
		  return ((ClassScope) parent).referenceContext;
		return null;
	}

	String basicToString(int tab) {

		String newLine = "\n"; //$NON-NLS-1$
		for (int i = tab; --i >= 0;)
			newLine += "\t"; //$NON-NLS-1$

		String s = newLine + "--- Method Scope ---"; //$NON-NLS-1$
		newLine += "\t"; //$NON-NLS-1$
		s += newLine + "locals:"; //$NON-NLS-1$
		for (int i = 0; i < localIndex; i++)
			s += newLine + "\t" + locals[i].toString(); //$NON-NLS-1$
		s += newLine + "startIndex = " + startIndex; //$NON-NLS-1$
		s += newLine + "isConstructorCall = " + isConstructorCall; //$NON-NLS-1$
		s += newLine + "initializedField = " + initializedField; //$NON-NLS-1$
		s += newLine + "lastVisibleFieldID = " + lastVisibleFieldID; //$NON-NLS-1$
		s += newLine + "referenceContext = " + referenceContext; //$NON-NLS-1$
		return s;
	}

	public LocalVariableBinding findVariable(char[] variableName) {
		LocalVariableBinding binding = super.findVariable(variableName);
		if (binding==null && CharOperation.equals(variableName,ARGUMENTS_NAME))
			binding=this.argumentsBinding;
		return binding;
	}


}
