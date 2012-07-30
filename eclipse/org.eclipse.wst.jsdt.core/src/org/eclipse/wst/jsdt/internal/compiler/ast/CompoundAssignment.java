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
package org.eclipse.wst.jsdt.internal.compiler.ast;

import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.ICompoundAssignment;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class CompoundAssignment extends Assignment implements OperatorIds, ICompoundAssignment {
	public int operator;
	public int preAssignImplicitConversion;

	//  var op exp is equivalent to var = (varType) var op exp
	// assignmentImplicitConversion stores the cast needed for the assignment

	public CompoundAssignment(Expression lhs, Expression expression,int operator, int sourceEnd) {
		//lhs is always a reference by construction ,
		//but is build as an expression ==> the checkcast cannot fail

		super(lhs, expression, sourceEnd);
		lhs.bits &= ~IsStrictlyAssigned; // tag lhs as NON assigned - it is also a read access
		lhs.bits |= IsCompoundAssigned; // tag lhs as assigned by compound
		this.operator = operator ;
	}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext,
		FlowInfo flowInfo) {
	// record setting a variable: various scenarii are possible, setting an array reference,
	// a field reference, a blank final field reference, a field of an enclosing instance or
	// just a local variable.
	if (this.resolvedType.id != T_JavaLangString) {
		lhs.checkNPE(currentScope, flowContext, flowInfo);
	}
	return  ((Reference) lhs).analyseAssignment(currentScope, flowContext, flowInfo, this, true).unconditionalInits();
}

	public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.NON_NULL;
	// we may have complained on checkNPE, but we avoid duplicate error
}

	public String operatorToString() {
		switch (operator) {
			case PLUS :
				return "+="; //$NON-NLS-1$
			case MINUS :
				return "-="; //$NON-NLS-1$
			case MULTIPLY :
				return "*="; //$NON-NLS-1$
			case DIVIDE :
				return "/="; //$NON-NLS-1$
			case AND :
				return "&="; //$NON-NLS-1$
			case OR :
				return "|="; //$NON-NLS-1$
			case XOR :
				return "^="; //$NON-NLS-1$
			case REMAINDER :
				return "%="; //$NON-NLS-1$
			case LEFT_SHIFT :
				return "<<="; //$NON-NLS-1$
			case RIGHT_SHIFT :
				return ">>="; //$NON-NLS-1$
			case UNSIGNED_RIGHT_SHIFT :
				return ">>>="; //$NON-NLS-1$
		}
		return "unknown operator"; //$NON-NLS-1$
	}

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		lhs.printExpression(indent, output).append(' ').append(operatorToString()).append(' ');
		return expression.printExpression(0, output) ;
	}

	public TypeBinding resolveType(BlockScope scope) {
		constant = Constant.NotAConstant;
		if (!(this.lhs instanceof Reference) || this.lhs.isThis()) {
			scope.problemReporter().expressionShouldBeAVariable(this.lhs);
			return null;
		}
		TypeBinding originalLhsType = lhs.resolveType(scope);
		TypeBinding originalExpressionType = expression.resolveType(scope);
		this.resolvedType=TypeBinding.ANY;
		if (originalLhsType == null || originalExpressionType == null)
		{
			return null;
		}

		// autoboxing support
		LookupEnvironment env = scope.environment();
		TypeBinding lhsType = originalLhsType, expressionType = originalExpressionType;
		boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		boolean unboxedLhs = false;
		if (use15specifics) {
			if (!lhsType.isBaseType() && expressionType.id != T_JavaLangString && expressionType.id != T_null) {
				TypeBinding unboxedType = env.computeBoxingType(lhsType);
				if (unboxedType != lhsType) {
					lhsType = unboxedType;
					unboxedLhs = true;
				}
			}
			if (!expressionType.isBaseType() && lhsType.id != T_JavaLangString  && lhsType.id != T_null) {
				expressionType = env.computeBoxingType(expressionType);
			}
		}

		if (restrainUsageToNumericTypes() && !lhsType.isNumericType() && !lhsType.isAnyType()) {
			scope.problemReporter().operatorOnlyValidOnNumericType(this, lhsType, expressionType);
			return null;
		}
		int lhsID = lhsType.id;
		int expressionID = expressionType.id;
		if (lhsID > 15 || expressionID > 15) {
			if (lhsID != T_JavaLangString) { // String += Thread is valid whereas Thread += String  is not
				scope.problemReporter().invalidOperator(this, lhsType, expressionType);
				return null;
			}
			expressionID = T_JavaLangObject; // use the Object has tag table
		}

		// the code is an int
		// (cast)  left   Op (cast)  rigth --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8     <<4        <<0

		// the conversion is stored INTO the reference (info needed for the code gen)
		int result = OperatorExpression.OperatorSignatures[operator][ (lhsID << 4) + expressionID];
		if (result == T_undefined) {
			scope.problemReporter().invalidOperator(this, lhsType, expressionType);
			return null;
		}
//		if (operator == PLUS){
//			if(lhsID == T_JavaLangObject) {
//				// <Object> += <String> is illegal (39248)
//				scope.problemReporter().invalidOperator(this, lhsType, expressionType);
//				return null;
//			} else {
//				// <int | boolean> += <String> is illegal
//				if ((lhsType.isNumericType() || lhsID == T_boolean) && !expressionType.isNumericType()){
//					scope.problemReporter().invalidOperator(this, lhsType, expressionType);
//					return null;
//				}
//			}
//		}
		this.preAssignImplicitConversion =  (unboxedLhs ? BOXING : 0) | (lhsID << 4) | (result & 0x0000F);
		return this.resolvedType = originalLhsType;
	}

	public boolean restrainUsageToNumericTypes(){
		return false ;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			lhs.traverse(visitor, scope);
			expression.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
	public int getASTType() {
		return IASTNode.COMPOUND_ASSIGNMENT;
	
	}
}
