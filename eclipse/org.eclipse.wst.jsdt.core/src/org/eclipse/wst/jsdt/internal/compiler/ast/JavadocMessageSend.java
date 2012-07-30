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
package org.eclipse.wst.jsdt.internal.compiler.ast;

import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IJsDocMessageSend;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;


public class JavadocMessageSend extends MessageSend implements IJsDocMessageSend {

	public int tagSourceStart, tagSourceEnd;
	public int tagValue;

	public JavadocMessageSend(char[] name, long pos) {
		this.selector = name;
		this.nameSourcePosition = pos;
		this.sourceStart = (int) (this.nameSourcePosition >>> 32);
		this.sourceEnd = (int) this.nameSourcePosition;
		this.bits |= InsideJavadoc;
	}
	public JavadocMessageSend(char[] name, long pos, JavadocArgumentExpression[] arguments) {
		this(name, pos);
		this.arguments = arguments;
	}

	/*
	 * Resolves type on a Block or Class scope.
	 */
	private TypeBinding internalResolveType(Scope scope) {
		// Answer the signature return type
		// Base type promotion
		this.constant = Constant.NotAConstant;
		if (this.receiver == null) {
			this.actualReceiverType = scope.enclosingSourceType();
		} else if (scope.kind == Scope.CLASS_SCOPE) {
			this.actualReceiverType = this.receiver.resolveType((ClassScope) scope);
		} else {
			this.actualReceiverType = this.receiver.resolveType((BlockScope) scope);
		}

		// will check for null after args are resolved

		TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
		boolean hasArgsTypeVar = false;
		if (this.arguments != null) {
			boolean argHasError = false; // typeChecks all arguments
			int length = this.arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++){
				Expression argument = this.arguments[i];
				if (scope.kind == Scope.CLASS_SCOPE) {
					argumentTypes[i] = argument.resolveType((ClassScope)scope);
				} else {
					argumentTypes[i] = argument.resolveType((BlockScope)scope);
				}
				if (argumentTypes[i] == null) {
					argHasError = true;
				} else if (!hasArgsTypeVar) {
					hasArgsTypeVar = false;
				}
			}
			if (argHasError) {
				return null;
			}
		}

		// check receiver type
		if (this.actualReceiverType == null) {
			return null;
		}
		this.actualReceiverType =(this.receiver!=null)? this.receiver.resolvedType:null;
		SourceTypeBinding enclosingType = scope.enclosingSourceType();
		if (enclosingType==null ? false : enclosingType.isCompatibleWith(this.actualReceiverType)) {
			this.bits |= ASTNode.SuperAccess;
		}
		// base type cannot receive any message
		if (this.actualReceiverType.isBaseType()) {
			return null;
		}
		this.binding = scope.getMethod(this.actualReceiverType, this.selector, argumentTypes, this);
		if (!this.binding.isValidBinding()) {
			// Try method in enclosing types
			TypeBinding enclosingTypeBinding = this.actualReceiverType;
			MethodBinding methodBinding = this.binding;
			while (!methodBinding.isValidBinding() && (enclosingTypeBinding.isMemberType() || enclosingTypeBinding.isLocalType())) {
				enclosingTypeBinding = enclosingTypeBinding.enclosingType();
				methodBinding = scope.getMethod(enclosingTypeBinding, this.selector, argumentTypes, this);
			}
			if (methodBinding.isValidBinding()) {
				this.binding = methodBinding;
			} else {
				// Try to search a constructor instead
				enclosingTypeBinding = this.actualReceiverType;
				MethodBinding contructorBinding = this.binding;
				while (!contructorBinding.isValidBinding() && (enclosingTypeBinding.isMemberType() || enclosingTypeBinding.isLocalType())) {
					enclosingTypeBinding = enclosingTypeBinding.enclosingType();
					if (CharOperation.equals(this.selector, enclosingTypeBinding.shortReadableName())) {
						contructorBinding = scope.getConstructor((ReferenceBinding)enclosingTypeBinding, argumentTypes, this);
					}
				}
				if (contructorBinding.isValidBinding()) {
					this.binding = contructorBinding;
				}
			}
		}
		if (!this.binding.isValidBinding()) {
			// implicit lookup may discover issues due to static/constructor contexts. javadoc must be resilient
			switch (this.binding.problemId()) {
				case ProblemReasons.NonStaticReferenceInConstructorInvocation:
				case ProblemReasons.NonStaticReferenceInStaticContext:
				case ProblemReasons.InheritedNameHidesEnclosingName :
				case ProblemReasons.Ambiguous:
					MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
					if (closestMatch != null) {
						this.binding = closestMatch; // ignore problem if can reach target method through it
					}
			}
		}
		if (!this.binding.isValidBinding()) {
			if (this.binding.declaringClass == null) {
				if (this.actualReceiverType instanceof ReferenceBinding) {
					this.binding.declaringClass = (ReferenceBinding) this.actualReceiverType;
				} else {
					return null;
				}
			}
			scope.problemReporter().javadocInvalidMethod(this, this.binding, scope.getDeclarationModifiers());
			// record the closest match, for clients who may still need hint about possible method match
			if (this.binding instanceof ProblemMethodBinding) {
				MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
				if (closestMatch != null) this.binding = closestMatch;
			}
			return this.resolvedType = this.binding == null ? null : this.binding.returnType;
		} else if (hasArgsTypeVar) {
			MethodBinding problem = new ProblemMethodBinding(this.binding, this.selector, argumentTypes, ProblemReasons.NotFound);
			scope.problemReporter().javadocInvalidMethod(this, problem, scope.getDeclarationModifiers());
		} else if (binding.isVarargs()) {
			int length = argumentTypes.length;
			if (!(binding.parameters.length == length && argumentTypes[length-1].isArrayType())) {
				MethodBinding problem = new ProblemMethodBinding(this.binding, this.selector, argumentTypes, ProblemReasons.NotFound);
				scope.problemReporter().javadocInvalidMethod(this, problem, scope.getDeclarationModifiers());
			}
		} else {
			int length = argumentTypes.length;
			for (int i=0; i<length; i++) {
				if (this.binding.parameters[i] != argumentTypes[i]) {
					MethodBinding problem = new ProblemMethodBinding(this.binding, this.selector, argumentTypes, ProblemReasons.NotFound);
					scope.problemReporter().javadocInvalidMethod(this, problem, scope.getDeclarationModifiers());
					break;
				}
			}
		}
		if (isMethodUseDeprecated(this.binding, scope, true)) {
			scope.problemReporter().javadocDeprecatedMethod(this.binding, this, scope.getDeclarationModifiers());
		}

		return this.resolvedType = this.binding.returnType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return (this.bits & ASTNode.SuperAccess) != 0;
	}

	public StringBuffer printExpression(int indent, StringBuffer output){

		if (this.receiver != null) {
			this.receiver.printExpression(0, output);
		}
		output.append('#').append(this.selector).append('(');
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length ; i ++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].printExpression(0, output);
			}
		}
		return output.append(')');
	}

	public TypeBinding resolveType(BlockScope scope) {
		return internalResolveType(scope);
	}

	public TypeBinding resolveType(ClassScope scope) {
		return internalResolveType(scope);
	}

	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode#traverse(org.eclipse.wst.jsdt.internal.compiler.ASTVisitor, org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			if (this.receiver != null) {
				this.receiver.traverse(visitor, blockScope);
			}
			if (this.arguments != null) {
				int argumentsLength = this.arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, blockScope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode#traverse(org.eclipse.wst.jsdt.internal.compiler.ASTVisitor, org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.receiver != null) {
				this.receiver.traverse(visitor, scope);
			}
			if (this.arguments != null) {
				int argumentsLength = this.arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					this.arguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.JSDOC_MESSAGE_SEND;
	
	}
}
