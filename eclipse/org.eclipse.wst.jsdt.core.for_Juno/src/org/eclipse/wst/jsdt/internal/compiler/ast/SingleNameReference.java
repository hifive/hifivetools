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
import org.eclipse.wst.jsdt.core.ast.ISingleNameReference;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.FunctionTypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.VariableBinding;

public class SingleNameReference extends NameReference implements ISingleNameReference, OperatorIds {

	public static final int READ = 0;
	public static final int WRITE = 1;
	public char[] token;
//	public FunctionBinding[] syntheticAccessors; // [0]=read accessor [1]=write accessor
// 	public TypeBinding genericCast;

	public SingleNameReference(char[] source, long pos) {
		this(source, (int) (pos >>> 32), (int) pos);
	}
	
	public SingleNameReference(char[] source, int sourceStart, int sourceEnd) {
		super();
		token = source;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}
	
	public char[] getToken() {
		return this.token;
	}
	public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {

		boolean isReachable = (flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0;
		// compound assignment extra work
		if (isCompound) { // check the variable part is initialized if blank final
			switch (bits & RestrictiveFlagMASK) {
				case Binding.FIELD : // reading a field
					manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
					break;
				case Binding.LOCAL : // reading a local variable
					// check if assigning a final blank field
					LocalVariableBinding localBinding;
					if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding) binding)) {
						if (localBinding.declaringScope instanceof MethodScope) {
							currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
						}
						// we could improve error msg here telling "cannot use compound assignment on final local variable"
					}
					if (isReachable) {
						localBinding.useFlag = LocalVariableBinding.USED;
					} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
						localBinding.useFlag = LocalVariableBinding.FAKE_USED;
					}
			}
		}
		if (assignment.expression != null) {
			flowInfo = assignment.expression.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
		switch (bits & RestrictiveFlagMASK) {
			case Binding.FIELD : // assigning to a field
				manageSyntheticAccessIfNecessary(currentScope, flowInfo, false /*write-access*/);

				break;
			case Binding.LOCAL : // assigning to a local variable
				LocalVariableBinding localBinding = (LocalVariableBinding) binding;
				if (!flowInfo.isDefinitelyAssigned(localBinding)){// for local variable debug attributes
					bits |= FirstAssignmentToLocal;
				} else {
					bits &= ~FirstAssignmentToLocal;
				}
				if ((localBinding.tagBits & TagBits.IsArgument) != 0) {
					currentScope.problemReporter().parameterAssignment(localBinding, this);
				}
				flowInfo.markAsDefinitelyAssigned(localBinding);
		}
		manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		return flowInfo;
	}
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		return analyseCode(currentScope, flowContext, flowInfo, true);
	}
	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {

		switch (bits & RestrictiveFlagMASK) {
			case Binding.FIELD : // reading a field
				if (valueRequired) {
					manageSyntheticAccessIfNecessary(currentScope, flowInfo, true /*read-access*/);
				}

				break;
			case Binding.LOCAL : // reading a local variable
			case Binding.LOCAL | Binding.TYPE :
			case Binding.VARIABLE:
				if(binding instanceof LocalVariableBinding) {
					LocalVariableBinding localBinding= (LocalVariableBinding) binding;
	
					// ignore the arguments variable inside a function
					if(!(CharOperation.equals(localBinding.name, new char[]{'a','r','g','u','m','e','n','t','s'}) && (localBinding.declaringScope instanceof MethodScope))) {
						if(!flowInfo.isDefinitelyAssigned(localBinding)) {
							if (localBinding.declaringScope instanceof MethodScope) {
									currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);		
							} else if(localBinding.isSameCompilationUnit(currentScope)) {
								currentScope.problemReporter().uninitializedGlobalVariable(localBinding, this);
							}
						}
					}
					
					if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
						localBinding.useFlag = LocalVariableBinding.USED;
					} else if (localBinding.useFlag == LocalVariableBinding.UNUSED) {
						localBinding.useFlag = LocalVariableBinding.FAKE_USED;
					}	
				}
				
		}
		if (valueRequired) {
			manageEnclosingInstanceAccessIfNecessary(currentScope, flowInfo);
		}
		return flowInfo;
	}

	public TypeBinding checkFieldAccess(BlockScope scope) {

		FieldBinding fieldBinding = (FieldBinding) binding;

		bits &= ~RestrictiveFlagMASK; // clear bits
		bits |= Binding.FIELD;
		MethodScope methodScope = scope.methodScope();
		boolean isStatic = fieldBinding.isStatic();
		if (!isStatic) {
			// must check for the static status....
			if (methodScope!=null && methodScope.isStatic) {
					// reference is ok if coming from compilation unit superclass
				if (fieldBinding.declaringClass==null || !fieldBinding.declaringClass.equals(scope.compilationUnitScope().superBinding))
				{
					scope.problemReporter().staticFieldAccessToNonStaticVariable(this, fieldBinding);
					this.constant = Constant.NotAConstant;
					return fieldBinding.type;
				}
			}
		}

		if (isFieldUseDeprecated(fieldBinding, scope, (this.bits & IsStrictlyAssigned) !=0))
			scope.problemReporter().deprecatedField(fieldBinding, this);

//		if ((this.bits & IsStrictlyAssigned) == 0
//				&& methodScope.enclosingSourceType() == fieldBinding.original().declaringClass
//				&& methodScope.lastVisibleFieldID >= 0
//				&& fieldBinding.id >= methodScope.lastVisibleFieldID
//				&& (!fieldBinding.isStatic() || methodScope.isStatic)) {
//			scope.problemReporter().forwardReference(this, 0, methodScope.enclosingSourceType());
//			this.bits |= ASTNode.IgnoreNoEffectAssignCheck;
//		}
		return fieldBinding.type;

	}

	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.Expression#computeConversion(org.eclipse.wst.jsdt.internal.compiler.lookup.Scope, org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding, org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding)
	 */
	public void computeConversion(Scope scope, TypeBinding runtimeTimeType, TypeBinding compileTimeType) {
//		if (runtimeTimeType == null || compileTimeType == null)
//			return;
//		if ((bits & Binding.FIELD) != 0 && this.binding != null && this.binding.isValidBinding()) {
//			// set the generic cast after the fact, once the type expectation is fully known (no need for strict cast)
//			FieldBinding field = (FieldBinding) this.binding;
//			FieldBinding originalBinding = field.original();
//			TypeBinding originalType = originalBinding.type;
//		    // extra cast needed if method return type is type variable
//			if (originalBinding != field
//					&& originalType != field.type
//					&& runtimeTimeType.id != T_JavaLangObject
//					&& (originalType.tagBits & TagBits.HasTypeVariable) != 0) {
//		    	TypeBinding targetType = (!compileTimeType.isBaseType() && runtimeTimeType.isBaseType())
//		    		? compileTimeType  // unboxing: checkcast before conversion
//		    		: runtimeTimeType;
//		        this.genericCast = originalType.genericCast(scope.boxing(targetType));
//			}
//		}
//		super.computeConversion(scope, runtimeTimeType, compileTimeType);
	}

	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.lookup.InvocationSite#genericTypeArguments()
	 */
	public TypeBinding[] genericTypeArguments() {
		return null;
	}

	/**
	 * Returns the local variable referenced by this node. Can be a direct reference (SingleNameReference)
	 * or thru a cast expression etc...
	 */
	public LocalVariableBinding localVariableBinding() {
		switch (bits & RestrictiveFlagMASK) {
			case Binding.FIELD : // reading a field
				break;
			case Binding.LOCAL : // reading a local variable
				return (LocalVariableBinding) this.binding;
		}
		return null;
	}

	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo) {

		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) == 0)	{
		//If inlinable field, forget the access emulation, the code gen will directly target it
		if (((bits & DepthMASK) == 0) || (constant != Constant.NotAConstant)) return;

		if ((bits & RestrictiveFlagMASK) == Binding.LOCAL) {
			currentScope.emulateOuterAccess((LocalVariableBinding) binding);
		}
		}
	}
	public void manageSyntheticAccessIfNecessary(BlockScope currentScope, FlowInfo flowInfo, boolean isReadAccess) {

//		if ((flowInfo.tagBits & FlowInfo.UNREACHABLE) != 0)	return;
//
//		//If inlinable field, forget the access emulation, the code gen will directly target it
//		if (constant != Constant.NotAConstant)
//			return;
//
//		if ((bits & Binding.FIELD) != 0) {
//			FieldBinding fieldBinding = (FieldBinding) binding;
//			FieldBinding codegenField = fieldBinding.original();
//			this.codegenBinding = codegenField;
//			if (((bits & DepthMASK) != 0)
//				&& (codegenField.isPrivate() // private access
//					|| (codegenField.isProtected() // implicit protected access
//							&& codegenField.declaringClass.getPackage() != currentScope.enclosingSourceType().getPackage()))) {
//				if (syntheticAccessors == null)
//					syntheticAccessors = new FunctionBinding[2];
//				syntheticAccessors[isReadAccess ? READ : WRITE] =
//				    ((SourceTypeBinding)currentScope.enclosingSourceType().
//						enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT)).addSyntheticMethod(codegenField, isReadAccess);
//				currentScope.problemReporter().needToEmulateFieldAccess(codegenField, this, isReadAccess);
//				return;
//			}
//			// if the binding declaring class is not visible, need special action
//			// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
//			// NOTE: from target 1.2 on, field's declaring class is touched if any different from receiver type
//			// and not from Object or implicit static field access.
//			if (fieldBinding.declaringClass != this.actualReceiverType
//					&& !this.actualReceiverType.isArrayType()
//					&& fieldBinding.declaringClass != null // array.length
//					&& fieldBinding.constant() == Constant.NotAConstant) {
//				CompilerOptions options = currentScope.compilerOptions();
//				if ((options.targetJDK >= ClassFileConstants.JDK1_2
//						&& (options.complianceLevel >= ClassFileConstants.JDK1_4 || !fieldBinding.isStatic())
//						&& fieldBinding.declaringClass.id != T_JavaLangObject) // no change for Object fields
//					|| !fieldBinding.declaringClass.canBeSeenBy(currentScope)) {
//
//					this.codegenBinding =
//					    currentScope.enclosingSourceType().getUpdatedFieldBinding(
//						       codegenField,
//						        (ReferenceBinding)this.actualReceiverType.erasure());
//				}
//			}
//		}
	}

public int nullStatus(FlowInfo flowInfo) {
	if (this.constant != null && this.constant != Constant.NotAConstant) {
		return FlowInfo.NON_NULL; // constant expression cannot be null
	}
	switch (bits & RestrictiveFlagMASK) {
		case Binding.FIELD : // reading a field
			return FlowInfo.UNKNOWN;
		case Binding.LOCAL : // reading a local variable
			LocalVariableBinding local = (LocalVariableBinding) this.binding;
			if (local != null) {
				if (flowInfo.isDefinitelyNull(local))
					return FlowInfo.NULL;
				if (flowInfo.isDefinitelyNonNull(local))
					return FlowInfo.NON_NULL;
				return FlowInfo.UNKNOWN;
			}
	}
	return FlowInfo.NON_NULL; // never get there
}

	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.ast.Expression#postConversionType(Scope)
	 */
	public TypeBinding postConversionType(Scope scope) {
		TypeBinding convertedType = this.resolvedType;
//		if (this.genericCast != null)
//			convertedType = this.genericCast;
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

	public StringBuffer printExpression(int indent, StringBuffer output){

		return output.append(token);
	}
	public TypeBinding reportError(BlockScope scope) {

		//=====error cases=======
		constant = Constant.NotAConstant;
		if (binding instanceof ProblemFieldBinding) {
			scope.problemReporter().invalidField(this, (FieldBinding) binding);
		} else if (binding instanceof ProblemReferenceBinding) {
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
		} else {
			scope.problemReporter().unresolvableReference(this, binding);
		}
		return null;
	}

	public TypeBinding resolveType(BlockScope scope) {
		return resolveType(scope,false,null);
	}

	public TypeBinding resolveType(BlockScope scope, boolean define, TypeBinding useType) {

		// for code gen, harm the restrictiveFlag
		constant = Constant.NotAConstant;

		this.binding=findBinding(scope);
		if (define && this.binding instanceof ProblemBinding)
		{
			LocalDeclaration localDeclaration = new LocalDeclaration(this.token,this.sourceEnd,this.sourceEnd);
			LocalVariableBinding localBinding=new LocalVariableBinding(localDeclaration,TypeBinding.UNKNOWN,0,false);
		    scope.compilationUnitScope().addLocalVariable(localBinding);
			this.binding=localBinding;
		}
//		this.codegenBinding = this.binding;
		if (this.binding.isValidBinding()) {
			switch (bits & RestrictiveFlagMASK) {
				case Binding.FIELD:
				case Binding.LOCAL : // =========only variable============
				case Binding.VARIABLE : // =========only variable============
				case Binding.LOCAL | Binding.TYPE : //====both variable and type============
				case Binding.VARIABLE | Binding.TYPE : //====both variable and type============
					if (binding instanceof VariableBinding) {
						VariableBinding variable = (VariableBinding) binding;
						if (binding instanceof LocalVariableBinding) {
							bits &= ~RestrictiveFlagMASK;  // clear bits
							bits |= Binding.LOCAL;
//							if (!variable.isFinal() && (bits & DepthMASK) != 0) {
//								scope.problemReporter().cannotReferToNonFinalOuterLocal((LocalVariableBinding)variable, this);
//							}
							TypeBinding fieldType = variable.type;
//							if (fieldType.isAnonymousType() && !fieldType.isObjectLiteralType()) {
//								LocalDeclaration declaration = ((LocalVariableBinding)binding).declaration;
//								if(declaration != null && !(declaration.getInitialization() instanceof AllocationExpression) &&
//										! (declaration.getInitialization() instanceof Literal)) {
//									bits |= Binding.TYPE;
//								}
//							}
								
							if (useType!=null && !(useType.id==T_null ||useType.id==T_any || useType.id==T_undefined))
							{
								if (define)
								{
									fieldType=variable.type=useType;
									if (useType.isFunctionType())	// add method binding if function
									{
										MethodBinding methodBinding = ((FunctionTypeBinding)useType).functionBinding.createNamedMethodBinding(this.token);
										MethodScope methodScope = scope.enclosingMethodScope();
										if (methodScope!=null)
											methodScope.addLocalMethod(methodBinding);
										else
											scope.compilationUnitScope().addLocalMethod(methodBinding);
									}
								}
								else
								{
									if (fieldType==TypeBinding.UNKNOWN)
										fieldType=variable.type=useType;
									else if (!fieldType.isCompatibleWith(useType))
										fieldType=variable.type=TypeBinding.ANY;
								}
							}
						
							constant = Constant.NotAConstant;
							

							return this.resolvedType = fieldType;
						}
						// perform capture conversion if read access
						TypeBinding fieldType = checkFieldAccess(scope);
						if (fieldType.isAnonymousType())
							bits |= Binding.TYPE;
						
						return this.resolvedType = fieldType;
					}

					if (binding instanceof MethodBinding)
					{
						return ((MethodBinding)binding).functionTypeBinding;
					}
					else
					{
					// thus it was a type
						bits &= ~RestrictiveFlagMASK;  // clear bits
						bits |= Binding.TYPE;
					}

				case Binding.TYPE : //========only type==============
					constant = Constant.NotAConstant;
					//deprecated test
					TypeBinding type = (TypeBinding)binding;
					if (isTypeUseDeprecated(type, scope))
						scope.problemReporter().deprecatedType(type, this);
					return this.resolvedType = type;
			}
		}

		// error scenarii
		return this.resolvedType = this.reportError(scope);
	}

	public Binding findBinding(BlockScope scope) {
		if (this.actualReceiverType != null) {
			Binding binding = scope.getField(this.actualReceiverType, token, this);
			if(!(binding instanceof ProblemFieldBinding))
				return binding;
			
		} else {
			this.actualReceiverType = scope.enclosingSourceType();
		}
		return  scope.getBinding(token, (Binding.TYPE|Binding.METHOD | bits)  & RestrictiveFlagMASK, this, true /*resolve*/);
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	public String unboundReferenceErrorName(){

		return new String(token);
	}
	
	public TypeBinding resolveForAllocation(BlockScope scope, ASTNode location)
	{
		char[] memberName = this.token;
		TypeBinding typeBinding=null;
		this.binding=	
				scope.getBinding(memberName, (Binding.TYPE|Binding.METHOD | bits)  & RestrictiveFlagMASK, this, true /*resolve*/);
		if (binding instanceof TypeBinding)
			typeBinding=(TypeBinding)binding;
		else if (binding instanceof MethodBinding)
			typeBinding=((MethodBinding)binding).returnType;
		else if (binding!=null && !binding.isValidBinding())
		{
			typeBinding=new ProblemReferenceBinding(memberName,null,binding.problemId());
		}
		return typeBinding;
	}
	public int getASTType() {
		return IASTNode.SINGLE_NAME_REFERENCE;
	
	}
}
