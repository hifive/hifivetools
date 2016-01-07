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
import org.eclipse.wst.jsdt.core.ast.IAssignment;
import org.eclipse.wst.jsdt.core.ast.ILocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.wst.jsdt.internal.compiler.lookup.FunctionTypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.VariableBinding;

public class LocalDeclaration extends AbstractVariableDeclaration implements ILocalDeclaration {

	public LocalVariableBinding binding;

	public LocalDeclaration(
		char[] name,
		int sourceStart,
		int sourceEnd) {

		this.name = name;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.declarationEnd = sourceEnd;
	}

	public IAssignment getAssignment() {
		if (this.initialization == null)
			return null;
		if (initialization instanceof FunctionExpression && ((FunctionExpression) initialization).getMethodDeclaration().getName() == null) {
			return new Assignment(new SingleNameReference(this.name, this.sourceStart, this.sourceEnd), this.initialization, this.initialization.sourceEnd);
		}
		return null;
	}
	
public LocalVariableBinding getBinding() {
	return this.binding;
}

public void setBinding(LocalVariableBinding binding) {
	this.binding=binding;
}
	
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// record variable initialization if any
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0) {
		bits |= ASTNode.IsLocalDeclarationReachable; // only set if actually reached
	}
	if (this.initialization != null) {
		int nullStatus = this.initialization.nullStatus(flowInfo);
		flowInfo =
			this.initialization
				.analyseCode(currentScope, flowContext, flowInfo)
				.unconditionalInits();
		if (!flowInfo.isDefinitelyAssigned(this.binding)){// for local variable debug attributes
			this.bits |= FirstAssignmentToLocal;
		} else {
			this.bits &= ~FirstAssignmentToLocal;  // int i = (i = 0);
		}
		flowInfo.markAsDefinitelyAssigned(binding);
		if ( true){//(this.binding.type.tagBits & TagBits.IsBaseType) == 0) {
			switch(nullStatus) {
				case FlowInfo.NULL :
					flowInfo.markAsDefinitelyNull(this.binding);
					break;
				case FlowInfo.NON_NULL :
					flowInfo.markAsDefinitelyNonNull(this.binding);
					break;
				default:
					flowInfo.markAsDefinitelyUnknown(this.binding);
			}
			// no need to inform enclosing try block since its locals won't get
			// known by the finally block
		}
	}
	if (this.nextLocal!=null)
		flowInfo=this.nextLocal.analyseCode(currentScope, flowContext, flowInfo);
		
	return flowInfo;
}

	public void checkModifiers() {

		//only potential valid modifier is <<final>>
		if (((modifiers & ExtraCompilerModifiers.AccJustFlag) & ~ClassFileConstants.AccFinal) != 0)
			//AccModifierProblem -> other (non-visibility problem)
			//AccAlternateModifierProblem -> duplicate modifier
			//AccModifierProblem | AccAlternateModifierProblem -> visibility problem"

			modifiers = (modifiers & ~ExtraCompilerModifiers.AccAlternateModifierProblem) | ExtraCompilerModifiers.AccModifierProblem;
	}

	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	public int getKind() {
		return LOCAL_VARIABLE;
	}

	public TypeBinding resolveVarType(BlockScope scope)
	{
		TypeBinding variableType = null;

		if (type!=null)
			variableType=type.resolveType(scope, true /* check bounds*/);
		else {
			if (inferredType!=null)
			  variableType=inferredType.resolveType(scope,this);
			else
				variableType=TypeBinding.UNKNOWN;
		}


		checkModifiers();
		return variableType;

	}

	public void resolve(BlockScope scope) {
		resolve0(scope);
		if (this.nextLocal!=null)
			this.nextLocal.resolve(scope);
	}
	
	
	private void resolve0(BlockScope scope) {

		// create a binding and add it to the scope
		TypeBinding variableType = resolveVarType(scope);

		
		if (type!=null)
			variableType=type.resolveType(scope, true /* check bounds*/);
		else {
			if (inferredType!=null)
			{
			  variableType=inferredType.resolveType(scope,this);
			}
			else
				variableType=TypeBinding.UNKNOWN;
		}

		checkModifiers();

		Binding varBinding  = null;
		if (scope.enclosingMethodScope()==null)
			varBinding=scope.getBinding(name, Binding.VARIABLE, this, false /*do not resolve hidden field*/);
		else
			varBinding=scope.getLocalBinding(name, Binding.VARIABLE, this, false);
		boolean alreadyDefined=false;
		if (varBinding != null && varBinding.isValidBinding()){
			VariableBinding existingVariable=(VariableBinding)varBinding;
			if (existingVariable.isFor(this))
			{
				if (variableType!=null)
					existingVariable.type=variableType;
				alreadyDefined=true;
			}
			else
			{

			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
				LocalVariableBinding localVariableBinding=(LocalVariableBinding)existingVariable;
				if (localVariableBinding.declaringScope instanceof CompilationUnitScope && scope.enclosingMethodScope()!=null)
					scope.problemReporter().localVariableHiding(this, existingVariable, false);
				else
					scope.problemReporter().redefineLocal(this);
			} else {
				scope.problemReporter().localVariableHiding(this, existingVariable, false);
			}
			}
		}

		if ((modifiers & ClassFileConstants.AccFinal)!= 0 && this.initialization == null) {
			modifiers |= ExtraCompilerModifiers.AccBlankFinal;
		}
		if (!(this.binding!=null && alreadyDefined))
		{
			this.binding = new LocalVariableBinding(this, variableType, modifiers, false);
			MethodScope methodScope = scope.enclosingMethodScope();
			if (methodScope!=null)
				methodScope.addLocalVariable(binding);
			else
				scope.compilationUnitScope().addLocalVariable(binding);
		}
		// allow to recursivelly target the binding....
		// the correct constant is harmed if correctly computed at the end of this method

		if (variableType == null) {
			if (initialization != null)
				initialization.resolveType(scope); // want to report all possible errors
			return;
		}

		// store the constant for final locals
		if (initialization != null) {
			if (initialization instanceof ArrayInitializer) {
				TypeBinding initializationType = initialization.resolveTypeExpecting(scope, variableType);
				if (initializationType != null) {
					((ArrayInitializer) initialization).binding = (ArrayBinding) initializationType;
				}
			} else {
			    this.initialization.setExpectedType(variableType);
				TypeBinding initializationType = this.initialization.resolveType(scope);
				if (initializationType != null) {
//					if (variableType != initializationType) // must call before computeConversion() and typeMismatchError()
//						scope.compilationUnitScope().recordTypeConversion(variableType, initializationType);
					if (initializationType.isFunctionType())
					{
						MethodBinding existingMethod = scope.findMethod(this.name, null,false);
						if (existingMethod!=null)
						{
							MethodBinding functionBinding = ((FunctionTypeBinding)initializationType).functionBinding;
							existingMethod.updateFrom(functionBinding);
						}
					}
					if (variableType==TypeBinding.UNKNOWN && initializationType!=TypeBinding.NULL)
						this.binding.type=initializationType;
					else {
						TypeBinding reconcileAnonymous = initializationType.reconcileAnonymous(this.binding.type);
						if (reconcileAnonymous!=null)
							this.binding.type=variableType=reconcileAnonymous;
						
						if (initialization.isConstantValueOfTypeAssignableToType(initializationType, variableType)
								|| variableType.isBaseType() /* && BaseTypeBinding.isWidening(variableType.id, initializationType.id)) */
								|| initializationType.isCompatibleWith(variableType)) {


//						this.initialization.computeConversion(scope, variableType, initializationType);
//						if (initializationType.needsUncheckedConversion(variableType)) {
//						    scope.problemReporter().unsafeTypeConversion(this.initialization, initializationType, variableType);
//						}
//						if (this.initialization instanceof CastExpression
//								&& (this.initialization.bits & ASTNode.UnnecessaryCast) == 0) {
//							CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression) this.initialization);
//						}
//					} else if (scope.isBoxingCompatibleWith(initializationType, variableType)
//										|| (initializationType.isBaseType()  // narrowing then boxing ?
//												&& scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5 // autoboxing
//												&& !variableType.isBaseType()
//												&& initialization.isConstantValueOfTypeAssignableToType(initializationType, scope.environment().computeBoxingType(variableType)))) {
//						this.initialization.computeConversion(scope, variableType, initializationType);
//						if (this.initialization instanceof CastExpression
//								&& (this.initialization.bits & ASTNode.UnnecessaryCast) == 0) {
//							CastExpression.checkNeedForAssignedCast(scope, variableType, (CastExpression) this.initialization);
//						}
						} else {
							//scope.problemReporter().typeMismatchError(initializationType, variableType, this.initialization);
						}
					}
				}
			}
			// check for assignment with no effect

			if (this.binding == Assignment.getDirectBinding(this.initialization)) {
				scope.problemReporter().assignmentHasNoEffect(this, this.name);
			}
		}
		// Resolve Javadoc comment if one is present
		if (this.javadoc != null) {
			/*
			if (classScope != null) {
				this.javadoc.resolve(classScope);
			}
			*/
			if (scope.enclosingMethodScope()!=null)
				this.javadoc.resolve(scope.enclosingMethodScope());
			else
				this.javadoc.resolve(scope.compilationUnitScope());
		}

		// only resolve annotation at the end, for constant to be positionned before (96991)
//		if (JavaScriptCore.IS_ECMASCRIPT4)
//		resolveAnnotations(scope, this.annotations, this.binding);
	}
	public StringBuffer printStatement(int indent, StringBuffer output) {
		if (this.javadoc != null) {
			this.javadoc.print(indent, output);
		}
		return super.printStatement(indent, output);
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (type!=null)
				type.traverse(visitor, scope);
			IAssignment assignment = getAssignment();
			if (assignment != null) {
				((Assignment) assignment).traverse(visitor, scope);
			}
			else if (initialization != null)
				initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
		if (this.nextLocal!=null)
			this.nextLocal.traverse(visitor, scope);
	}

	public String getTypeName()
	{
		if (type!=null)
			return type.toString();
		if (inferredType!=null)
			return new String(inferredType.getName());
		return null;
	}
	public int getASTType() {
		return IASTNode.LOCAL_DECLARATION;
	
	}
}
