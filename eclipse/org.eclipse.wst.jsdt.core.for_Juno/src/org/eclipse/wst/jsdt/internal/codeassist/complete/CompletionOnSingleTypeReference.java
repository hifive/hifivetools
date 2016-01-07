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
package org.eclipse.wst.jsdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce a type reference containing the completion identifier as a single
 * name reference.
 * e.g.
 *
 *	class X extends Obj[cursor]
 *
 *	---> class X extends <CompleteOnType:Obj>
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.TypeReference;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnSingleTypeReference extends SingleTypeReference {
	public static final int K_TYPE = 0;
	public static final int K_CLASS = 1;
	public static final int K_INTERFACE = 2;
	public static final int K_EXCEPTION = 3;
	
	private int kind = K_TYPE;
	public boolean isCompletionNode;
	public boolean isConstructorType;
	public CompletionOnFieldType fieldTypeCompletionNode;
	
	public CompletionOnSingleTypeReference(char[] source, long pos) {
		this(source, pos, K_TYPE);
	}
	public CompletionOnSingleTypeReference(char[] source, long pos, int kind) {
		super(source, pos);
		isCompletionNode = true;
		this.kind = kind;
	}
	public void aboutToResolve(Scope scope) {
		getTypeBinding(scope);
	}
	/*
	 * No expansion of the completion reference into an array one
	 */
	public TypeReference copyDims(int dim){
		return this;
	}
	protected TypeBinding getTypeBinding(Scope scope) {
	    if (this.fieldTypeCompletionNode != null) {
			throw new CompletionNodeFound(this.fieldTypeCompletionNode, scope);
	    }
		if(isCompletionNode) {
			throw new CompletionNodeFound(this, scope);
		} else {
			return super.getTypeBinding(scope);
		}
	}
	public boolean isClass(){
		return this.kind == K_CLASS;
	}
	public boolean isInterface(){
		return this.kind == K_INTERFACE;
	}
	public boolean isException(){
		return this.kind == K_EXCEPTION;
	}
	public boolean isSuperType(){
		return this.kind == K_CLASS || this.kind == K_INTERFACE;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){
		switch (this.kind) {
			case K_CLASS :
				output.append("<CompleteOnClass:");//$NON-NLS-1$
				break;
			case K_INTERFACE :
				output.append("<CompleteOnInterface:");//$NON-NLS-1$
				break;
			case K_EXCEPTION :
				output.append("<CompleteOnException:");//$NON-NLS-1$
				break;
			default :
				output.append("<CompleteOnType:");//$NON-NLS-1$
				break;
		}
		return output.append(token).append('>');
	}
	
	public TypeBinding resolveTypeEnclosing(BlockScope scope, ReferenceBinding enclosingType) {
	    if (this.fieldTypeCompletionNode != null) {
			throw new CompletionNodeFound(this.fieldTypeCompletionNode, scope);
	    }
		if(isCompletionNode) {
			throw new CompletionNodeFound(this, enclosingType, scope);
		} else {
			return super.resolveTypeEnclosing(scope, enclosingType);
		}
	}
	
	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.Expression#resolve(org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope)
	 */
	public void resolve(BlockScope scope) {
		super.resolve(scope);
		
		throw new CompletionNodeFound(this, scope);
	}
	
	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.TypeReference#resolveType(org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope, boolean)
	 */
	public TypeBinding resolveType(BlockScope scope, boolean checkBounds) {
		super.resolveType(scope, checkBounds);
		
		throw new CompletionNodeFound(this, scope);
	}
	
	/**
	 * 
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.Expression#resolveForAllocation(org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope, org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode)
	 */
	public TypeBinding resolveForAllocation(BlockScope scope, ASTNode location) {
		return this.resolveType(scope);
	}
}
