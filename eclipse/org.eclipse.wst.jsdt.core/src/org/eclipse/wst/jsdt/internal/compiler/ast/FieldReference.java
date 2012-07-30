/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.compiler.ast;

import java.util.ArrayList;

import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeIds;
import org.eclipse.wst.jsdt.internal.compiler.util.Util;

public class FieldReference extends Reference implements InvocationSite, IFieldReference {

	public static final int READ = 0;
	public static final int WRITE = 1;
	public Expression receiver;
	public char[] token;
	public FieldBinding binding;															// exact binding resulting from lookup
	public TypeBinding typeBinding;															// exact binding resulting from lookup
//	protected FieldBinding codegenBinding;									// actual binding used for code generation (if no synthetic accessor)
//	public FunctionBinding[] syntheticAccessors; // [0]=read accessor [1]=write accessor

	public long nameSourcePosition; //(start<<32)+end
	public TypeBinding receiverType;
//	public TypeBinding genericCast;

public FieldReference(char[] source, long pos) {
	token = source;
	nameSourcePosition = pos;
	//by default the position are the one of the field (not true for super access)
	sourceStart = (int) (pos >>> 32);
	sourceEnd = (int) (pos & 0x00000000FFFFFFFFL);
	bits |= Binding.FIELD;

}

public FlowInfo analyseAssignment(BlockScope currentScope, 	FlowContext flowContext, 	FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
	// compound assignment extra work
//	if (isCompound) { // check the variable part is initialized if blank final
//		if (binding.isBlankFinal()
//			&& receiver.isThis()
//			&& currentScope.allowBlankFinalFieldAssignment(binding)
//			&& (!flowInfo.isDefinitelyAssigned(binding))) {
//			currentScope.problemReporter().uninitializedBlankFinalField(binding, this);
//			// we could improve error msg here telling "cannot use compound assignment on final blank field"
//		}
//		manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
//	}
	if (receiver instanceof SingleNameReference && ((SingleNameReference)receiver).binding instanceof LocalVariableBinding)
	{
		flowInfo.markAsDefinitelyNonNull((LocalVariableBinding)((SingleNameReference)receiver).binding);
		flowInfo.markAsDefinitelyAssigned((LocalVariableBinding)((SingleNameReference)receiver).binding);
	}
	flowInfo =
		receiver
			.analyseCode(currentScope, flowContext, flowInfo, binding==null || !binding.isStatic())
			.unconditionalInits();
	if (assignment.expression != null) {
		flowInfo =
			assignment
				.expression
				.analyseCode(currentScope, flowContext, flowInfo)
				.unconditionalInits();
	}
	manageSyntheticAccessIfNecessary(currentScope, flowInfo, false /*write-access*/);

	return flowInfo;
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return analyseCode(currentScope, flowContext, flowInfo, true);
}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
	boolean nonStatic = binding==null || !binding.isStatic();
	receiver.analyseCode(currentScope, flowContext, flowInfo, nonStatic);
	if (nonStatic) {
		receiver.checkNPE(currentScope, flowContext, flowInfo);
	}

	if (valueRequired || currentScope.compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4) {
		manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
	}
	return flowInfo;
}

/**
 * @see org.eclipse.wst.jsdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.wst.jsdt.internal.compiler.lookup.Scope, org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding, org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding)
 */
public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
	if (runtimeTimeType == null || compileTimeType == null)
		return;
	// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
	if (this.binding != null && this.binding.isValidBinding()) {
		FieldBinding originalBinding = this.binding.original();
	}
}

public FieldBinding fieldBinding() {
	return binding;
}

/**
 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
 */
public TypeBinding[] genericTypeArguments() {
	return null;
}
public boolean isSuperAccess() {
	return receiver.isSuper();
}

public boolean isTypeAccess() {
	return receiver != null && receiver.isTypeReference();
}

/*
 * No need to emulate access to protected fields since not implicitly accessed
 */
public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo, boolean isReadAccess) {
	if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0)	return;

	// if field from parameterized type got found, use the original field at codegen time
//	this.codegenBinding = this.binding.original();
//
//	if (binding.isPrivate()) {
//		if ((currentScope.enclosingSourceType() != this.codegenBinding.declaringClass)
//				&& binding.constant() == Constant.NotAConstant) {
//			if (syntheticAccessors == null)
//				syntheticAccessors = new FunctionBinding[2];
//			syntheticAccessors[isReadAccess ? READ : WRITE] =
//				((SourceTypeBinding) this.codegenBinding.declaringClass).addSyntheticMethod(this.codegenBinding, isReadAccess);
//			currentScope.problemReporter().needToEmulateFieldAccess(this.codegenBinding, this, isReadAccess);
//			return;
//		}
//
//	} else if (receiver instanceof QualifiedSuperReference) { // qualified super
//
//		// qualified super need emulation always
//		SourceTypeBinding destinationType =
//			(SourceTypeBinding) (((QualifiedSuperReference) receiver)
//				.currentCompatibleType);
//		if (syntheticAccessors == null)
//			syntheticAccessors = new FunctionBinding[2];
//		syntheticAccessors[isReadAccess ? READ : WRITE] = destinationType.addSyntheticMethod(this.codegenBinding, isReadAccess);
//		currentScope.problemReporter().needToEmulateFieldAccess(this.codegenBinding, this, isReadAccess);
//		return;
//
//	} else if (binding.isProtected()) {
//
//		SourceTypeBinding enclosingSourceType;
//		if (((bits & DepthMASK) != 0)
//			&& binding.declaringClass.getPackage()
//				!= (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()) {
//
//			SourceTypeBinding currentCompatibleType =
//				(SourceTypeBinding) enclosingSourceType.enclosingTypeAt(
//					(bits & DepthMASK) >> DepthSHIFT);
//			if (syntheticAccessors == null)
//				syntheticAccessors = new FunctionBinding[2];
//			syntheticAccessors[isReadAccess ? READ : WRITE] = currentCompatibleType.addSyntheticMethod(this.codegenBinding, isReadAccess);
//			currentScope.problemReporter().needToEmulateFieldAccess(this.codegenBinding, this, isReadAccess);
//			return;
//		}
//	}
	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
	// and not from Object or implicit static field access.
//	if (this.binding.declaringClass != this.receiverType
//			&& !this.receiverType.isArrayType()
//			&& this.binding.declaringClass != null // array.length
//			&& this.binding.constant() == Constant.NotAConstant) {
//		CompilerOptions options = currentScope.compilerOptions();
//		if ((options.targetJDK >= ClassFileConstants.JDK1_2
//				&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || !(receiver.isImplicitThis() && this.codegenBinding.isStatic()))
//				&& this.binding.declaringClass.id != T_JavaLangObject) // no change for Object fields
//			|| !this.binding.declaringClass.canBeSeenBy(currentScope)) {
//
//			this.codegenBinding =
//				currentScope.enclosingSourceType().getUpdatedFieldBinding(
//					this.codegenBinding,
//					(ReferenceBinding) this.receiverType.erasure());
//		}
//	}
}

public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.UNKNOWN;
}

public Constant optimizedBooleanConstant() {
	switch (this.resolvedType.id) {
		case T_boolean :
		case T_JavaLangBoolean :
			return Constant.NotAConstant;
		default :
			return Constant.NotAConstant;
	}
}

/**
 * @see org.eclipse.wst.jsdt.internal.compiler.ast.Expression#postConversionType(Scope)
 */
public TypeBinding postConversionType(Scope scope) {
	TypeBinding convertedType = this.resolvedType;
//	if (this.genericCast != null)
//		convertedType = this.genericCast;
	int runtimeType = (this.implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4;
	switch (runtimeType) {
		case T_boolean :
			convertedType = TypeBinding.BOOLEAN;
			break;
		case T_short :
			convertedType = TypeBinding.SHORT;
			break;
		case T_char :
			convertedType = TypeBinding.CHAR;
			break;
		case T_int :
			convertedType = TypeBinding.INT;
			break;
		case T_float :
			convertedType = TypeBinding.FLOAT;
			break;
		case T_long :
			convertedType = TypeBinding.LONG;
			break;
		case T_double :
			convertedType = TypeBinding.DOUBLE;
			break;
		default :
	}
	if ((this.implicitConversion & BOXING) != 0) {
		convertedType = scope.environment().computeBoxingType(convertedType);
	}
	return convertedType;
}

public StringBuffer printExpression(int indent, StringBuffer output) {
	return receiver.printExpression(0, output).append('.').append(token);
}


public TypeBinding resolveType(BlockScope scope) {
	return resolveType(scope, false, null);
}

public TypeBinding resolveType(BlockScope scope, boolean define, TypeBinding useType) {
	// Answer the signature type of the field.
	// constants are propaged when the field is final
	// and initialized with a (compile time) constant

	//always ignore receiver cast, since may affect constant pool reference
//	boolean receiverCast = false;
//	if (this.receiver instanceof CastExpression) {
//		this.receiver.bits |= DisableUnnecessaryCastCheck; // will check later on
//		receiverCast = true;
//	}


	/*
	 * Handle if this is a reference to the prototype of a type
	 *
	 * By default, the prototype is of type Object, but if there is an InferredType
	 * for the receiver, it should yeild the receiver type.
	 */
if( this.isPrototype() ){
		// check if receiver type is defined
	if ((this.receiverType = receiver.resolveType(scope)) == null) {
		constant = Constant.NotAConstant;
        return null;
    }


		//construc the name of the type based on the receiver
		char [] possibleTypeName = Util.getTypeName( receiver );
		TypeBinding typeBinding = scope.getJavaLangObject();
		if( possibleTypeName != null ){
			Binding possibleTypeBinding = scope.getBinding( possibleTypeName, Binding.TYPE  & RestrictiveFlagMASK, this, true /*resolve*/);

			if( possibleTypeBinding.isValidBinding() ){
				//get the super class
//				TypeBinding superTypeBinding = ((ReferenceBinding)possibleTypeBinding).superclass();
//				if( superTypeBinding != null )
//					typeBinding = superTypeBinding;
				typeBinding = (TypeBinding)possibleTypeBinding;
			}
			char[] fieldname=new char[]{'p','r','o','t','o','t','y','p','e'};
			this.binding=scope.getJavaLangObject().getField(fieldname, true);
			constant = Constant.NotAConstant;
			return this.resolvedType = typeBinding;
		}

	}

	char [] possibleTypeName = Util.getTypeName( this );
	Binding possibleTypeBinding =null;
	if (possibleTypeName!=null)
	   possibleTypeBinding = scope.getBinding( possibleTypeName, Binding.TYPE  & RestrictiveFlagMASK, this, true /*resolve*/);
	if(possibleTypeBinding != null && possibleTypeBinding.isValidBinding() && (TypeBinding)possibleTypeBinding != scope.getJavaLangObject()) {
		this.typeBinding=(TypeBinding)possibleTypeBinding;
		constant = Constant.NotAConstant;
		this.bits|=Binding.TYPE;
		return this.typeBinding;
	}
	boolean receiverDefined=true;
   // if this could be a qualified type name, first check if receiver is defined, and if not look up as type name
	if (possibleTypeName!=null && receiver instanceof SingleNameReference)
	{
		Binding receiverBinding = ((SingleNameReference)receiver).findBinding(scope);
		if (receiverBinding==null || !receiverBinding.isValidBinding())
			receiverDefined=false;
		this.receiverType=null;
	}
	if (receiverDefined)
	  this.receiverType = receiver.resolveType(scope);
	if (this.receiverType == null || this.receiverType==scope.getJavaLangObject()) {
		if (possibleTypeBinding!=null && possibleTypeBinding.isValidBinding())
		{
			this.typeBinding=(TypeBinding)possibleTypeBinding;
			this.bits|=Binding.TYPE;
			return this.typeBinding;
		}
		else
		{
			this.binding=new ProblemFieldBinding(null,this.token,ProblemReasons.NotFound);
			constant = Constant.NotAConstant;
			this.resolvedType=TypeBinding.ANY;
		}
		return null;
	}
//	if (receiverCast) {
//		 // due to change of declaring class with receiver type, only identity cast should be notified
//		if (((CastExpression)this.receiver).expression.resolvedType == this.receiverType) {
//				scope.problemReporter().unnecessaryCast((CastExpression)this.receiver);
//		}
//	}
	// the case receiverType.isArrayType and token = 'length' is handled by the scope API

	/*
	 * Need to look in the fields and method for a match... In JS there is no distinction between member functions
	 * or field. We are trying to mimic that property below (Java does have a distinction)
	 */
	if (this.receiverType.id==TypeIds.T_any)
	{
		constant = Constant.NotAConstant;
		this.binding=new  ProblemFieldBinding( null, token, ProblemReasons.NotFound) ;
	    return this.resolvedType=TypeBinding.ANY;
	}
	
	Binding memberBinding = scope.getFieldOrMethod(this.receiverType, token, this);
	boolean receiverIsType = (receiver instanceof NameReference || receiver instanceof FieldReference || receiver instanceof ThisReference)
		&& ( receiver.bits & Binding.TYPE) != 0;
	if (!memberBinding.isValidBinding() && (this.receiverType!=null && this.receiverType.isFunctionType()))
	{
		   Binding alternateBinding = receiver.alternateBinding();
		   if (alternateBinding instanceof TypeBinding)
		   {
			   this.receiverType=(TypeBinding)alternateBinding;
				memberBinding = scope.getFieldOrMethod(this.receiverType, token, this);
				receiverIsType=true;
		   }
	}
			
	
	//FieldBinding fieldBinding = this.codegenBinding = this.binding = scope.getField(this.receiverType, token, this);

	constant = Constant.NotAConstant;
	if( memberBinding instanceof FieldBinding ){
		FieldBinding fieldBinding =/* this.codegenBinding =*/ this.binding = (FieldBinding)memberBinding;
		if (!fieldBinding.isValidBinding()) {
			this.binding=fieldBinding;
			this.resolvedType=TypeBinding.ANY;
			if (!define)
			{
				constant = Constant.NotAConstant;
				scope.problemReporter().invalidField(this, this.receiverType);
				return null;
			}
			else	// should add binding here
			{

			}
	//		return this.resolvedType=TypeBinding.UNKNOWN;
		}
		if (JavaScriptCore.IS_ECMASCRIPT4)
		{
			TypeBinding receiverErasure = this.receiverType;
			if (receiverErasure instanceof ReferenceBinding) {
				if (receiverErasure.findSuperTypeWithSameErasure(fieldBinding.declaringClass) == null) {
					this.receiverType = fieldBinding.declaringClass; // handle indirect inheritance thru variable secondary bound
				}
			}
		}
		if (isFieldUseDeprecated(fieldBinding, scope, (this.bits & IsStrictlyAssigned) !=0)) {
			scope.problemReporter().deprecatedField(fieldBinding, this);
		}
		boolean isImplicitThisRcv = receiver.isImplicitThis();
		constant = Constant.NotAConstant;
		if (fieldBinding.isStatic()) {
			// static field accessed through receiver? legal but unoptimal (optional warning)
			if (!(isImplicitThisRcv
					||  receiverIsType
						)) {
				scope.problemReporter().nonStaticAccessToStaticField(this, fieldBinding);
			}
			if (!isImplicitThisRcv
					&& fieldBinding.declaringClass != receiverType
					&& fieldBinding.declaringClass.canBeSeenBy(scope)) {
				scope.problemReporter().indirectAccessToStaticField(this, fieldBinding);
			}
		} else {
			if(receiverIsType)
				scope.problemReporter().staticFieldAccessToNonStaticVariable(this, fieldBinding);
		}
		// perform capture conversion if read access
		return this.resolvedType = fieldBinding.type;
	}
	else if( memberBinding instanceof MethodBinding ){
		MethodBinding methodBinding=(MethodBinding) memberBinding;

		if (!methodBinding.isStatic()) {
			if (receiverIsType && methodBinding.isValidBinding() && !methodBinding.isConstructor()) {
				if(this.receiverType == null || !this.receiverType.isAnonymousType())
					scope.problemReporter().mustUseAStaticMethod(this, methodBinding);
			}
		}
		else 
		{
			if (!receiverIsType && methodBinding.isValidBinding())
				scope.problemReporter().nonStaticAccessToStaticMethod(this,
						methodBinding);

		}
		
		this.resolvedType= scope.getJavaLangFunction();
		this.binding = new FieldBinding(((MethodBinding) memberBinding).selector, this.receiverType, ((MethodBinding) memberBinding).modifiers, methodBinding.declaringClass);
		//this.binding=new ProblemFieldBinding(null,this.token,ProblemReasons.NotFound);
		if( memberBinding.isValidBinding() )
			return this.resolvedType;
		return null;
	}

	return null;
}

public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}

public void setDepth(int depth) {
	bits &= ~DepthMASK; // flush previous depth if any
	if (depth > 0) {
		bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
	}
}

public void setFieldIndex(int index) {
	// ignored
}

public void traverse(ASTVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		receiver.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
public boolean isPrototype()
{
	return (CharOperation.equals(TypeConstants.PROTOTYPE,this.token));
}


public TypeBinding resolveForAllocation(BlockScope scope, ASTNode location)
{
	char [][]qualifiedName=asQualifiedName();
	TypeBinding typeBinding=null;
	if (qualifiedName!=null)
	{
		typeBinding=scope.getType(CharOperation.concatWith(qualifiedName, '.'));
	}
	if (typeBinding==null || !typeBinding.isValidBinding())
	{
		this.receiverType = receiver.resolveType(scope);
		if (this.receiverType == null) {
			this.binding=new ProblemFieldBinding(null,this.token,ProblemReasons.NotFound);
			constant = Constant.NotAConstant;
			this.resolvedType=TypeBinding.ANY;
			return null;
		}
		Binding memberBinding = scope.getFieldOrMethod(this.receiverType, token, this);
		if( memberBinding instanceof MethodBinding && memberBinding.isValidBinding()){
			this.resolvedType= ((MethodBinding)memberBinding).allocationType;
			this.binding=new ProblemFieldBinding(null,this.token,ProblemReasons.NotFound);
			if( memberBinding.isValidBinding() )
				return this.resolvedType;
		}
		
	}
	if (typeBinding==null)
	{
		if (qualifiedName==null)
			qualifiedName=new char[][]{token};
		typeBinding=new  ProblemReferenceBinding(qualifiedName,null,ProblemReasons.NotFound);
	}
	return typeBinding;
}
public int getASTType() {
	return IASTNode.FIELD_REFERENCE;

}

public char [][] asQualifiedName()
{
	ArrayList list=new ArrayList();
	list.add(token);
	FieldReference fieldReference=this;
	while (fieldReference!=null)
	{
		if ( fieldReference.receiver instanceof SingleNameReference)
		{
			list.add(0,((SingleNameReference)fieldReference.receiver).token);
			fieldReference=null;
		}
		else if (fieldReference.receiver instanceof FieldReference)
		{
			fieldReference=(FieldReference)fieldReference.receiver;
			list.add(0,fieldReference.token);
		}
		else
			return null;
	}
	return (char [][])list.toArray(new char[list.size()][]);
}

public IExpression getReceiver() {
	return receiver;
}

public char[] getToken() {
	return token;
}

public boolean isTypeReference() {
	return (this.bits & Binding.TYPE) ==Binding.TYPE;
}

}
