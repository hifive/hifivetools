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

import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.wst.jsdt.internal.compiler.problem.AbortMethod;

public class MethodDeclaration extends AbstractMethodDeclaration implements IFunctionDeclaration {

	public TypeReference returnType;

	/**
	 * FunctionDeclaration constructor comment.
	 */
	public MethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public FlowInfo analyseCode(
		Scope classScope,
		FlowContext initializationContext,
		FlowInfo flowInfo) {

		// starting of the code analysis for methods
		if (ignoreFurtherInvestigation)
			return flowInfo;
		try {
			if (binding == null)
				return flowInfo;

			if (!this.binding.isUsed() &&
					(this.binding.isPrivate()
						|| (((this.binding.modifiers & (ExtraCompilerModifiers.AccOverriding|ExtraCompilerModifiers.AccImplementing)) == 0) && this.binding.declaringClass.isLocalType()))) {
				if (!classScope.referenceCompilationUnit().compilationResult.hasSyntaxError) {
					scope.problemReporter().unusedPrivateMethod(this);
				}
			}

//			// skip enum implicit methods
//			if (binding.declaringClass.isEnum() && (this.selector == TypeConstants.VALUES || this.selector == TypeConstants.VALUEOF))
//				return flowInfo;

			// may be in a non necessary <clinit> for innerclass with static final constant fields
			if (binding.isAbstract())
				return flowInfo;

			ExceptionHandlingFlowContext methodContext =
				new ExceptionHandlingFlowContext(
					initializationContext,
					this,
					null,
					scope,
					FlowInfo.DEAD_END);

			// tag parameters as being set
			if (this.arguments != null) {
				for (int i = 0, count = this.arguments.length; i < count; i++) {
					flowInfo.markAsDefinitelyAssigned(this.arguments[i].getBinding());
				}
			}
			// propagate to statements
			if (statements != null) {
				boolean didAlreadyComplain = false;
				for (int i = 0, count = statements.length; i < count; i++) {
					Statement stat = statements[i];
					if (!stat.complainIfUnreachable(flowInfo, scope, didAlreadyComplain)) {
						if (stat instanceof  AbstractMethodDeclaration) {
							((AbstractMethodDeclaration)stat).analyseCode(this.scope, null, flowInfo.copy());
						} else
							flowInfo = stat.analyseCode(scope, methodContext, flowInfo);
					} else {
						didAlreadyComplain = true;
					}
				}
			}
			// check for missing returning path
			TypeBinding returnTypeBinding = binding.returnType;
			boolean isJsDocInferredReturn = (binding.tagBits&TagBits.IsInferredJsDocType)!=0;
			if ((returnTypeBinding == TypeBinding.VOID || returnTypeBinding == TypeBinding.UNKNOWN) || isAbstract()) {
				this.needFreeReturn =
					(flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0;
			} else {
				if (flowInfo != FlowInfo.DEAD_END) {
					if ((this.inferredMethod==null || !this.inferredMethod.isConstructor) &&
						!isJsDocInferredReturn)
						scope.problemReporter().shouldReturn(returnTypeBinding, this);
				}
			}
			this.scope.reportUnusedDeclarations();
			// check unreachable catch blocks
			if (JavaScriptCore.IS_ECMASCRIPT4)
				methodContext.complainIfUnusedExceptionHandlers(this);
		} catch (AbortMethod e) {
			this.ignoreFurtherInvestigation = true;
		}
		return flowInfo;
	}

	public boolean isMethod() {

		return true;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {

		//fill up the method body with statement
		if (ignoreFurtherInvestigation)
			return;
		parser.parse(this, unit);
	}

	public void resolveStatements() {

		// ========= abort on fatal error =============

		super.resolveStatements();
	}

	public void traverse(
		ASTVisitor visitor,
		 Scope classScope) {

		if (visitor.visit(this, classScope)) {
			if (this.javadoc != null) {
				this.javadoc.traverse(visitor, scope);
			}
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
	public void traverse(
			ASTVisitor visitor,
			BlockScope blockScope) {

			if (visitor.visit(this, blockScope)) {
				if (arguments != null) {
					int argumentLength = arguments.length;
					for (int i = 0; i < argumentLength; i++)
						arguments[i].traverse(visitor, scope);
				}
				if (statements != null) {
					int statementsLength = statements.length;
					for (int i = 0; i < statementsLength; i++)
						statements[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, blockScope);
		}

	public int getASTType() {
		return IASTNode.FUNCTION_DECLARATION;
	
	}
}
