/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.jsdt.core.dom;

/**
 * Abstract base class of AST nodes that represent expressions.
 * There are several kinds of expressions.
 * <p>
 * <pre>
 * Expression:
 *    Name
 *    IntegerLiteral (includes decimal, hex, and octal forms; and long)
 *    FloatingPointLiteral (includes both float and double)
 *    CharacterLiteral
 *    NullLiteral
 *    BooleanLiteral
 *    StringLiteral
 *    TypeLiteral
 *    ThisExpression
 *    SuperFieldAccess
 *    FieldAccess
 *    Assignment
 *    ParenthesizedExpression
 *    ClassInstanceCreation
 *    ArrayCreation
 *    ArrayInitializer
 *    FunctionInvocation
 *    SuperMethodInvocation
 *    ArrayAccess
 *    InfixExpression
 *    InstanceofExpression
 *    ConditionalExpression
 *    PostfixExpression
 *    PrefixExpression
 *    CastExpression
 *    VariableDeclarationExpression
 * </pre>
 * </p>
  * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
*/
public abstract class Expression extends ASTNode {

	/**
	 * Creates a new AST node for an expression owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	Expression(AST ast) {
		super(ast);
	}

	/**
	 * Resolves and returns the compile-time constant expression value as
	 * specified in JLS2 15.28, if this expression has one. Constant expression
	 * values are unavailable unless bindings are requested when the AST is
	 * being built. If the type of the value is a primitive type, the result
	 * is the boxed equivalent (i.e., int returned as an <code>Integer</code>);
	 * if the type of the value is <code>String</code>, the result is the string
	 * itself. If the expression does not have a compile-time constant expression
	 * value, the result is <code>null</code>.
	 * <p>
	 * Resolving constant expressions takes into account the value of simple
	 * and qualified names that refer to constant variables (JLS2 4.12.4).
	 * </p>
	 * <p>
	 * Note 1: enum constants are not considered constant expressions.
	 * The result is always <code>null</code> for these.
	 * </p>
	 * <p>
	 * Note 2: Compile-time constant expressions cannot denote <code>null</code>.
	 * So technically {@link NullLiteral} nodes are not constant expressions.
	 * The result is <code>null</code> for these nonetheless.
	 * </p>
	 *
	 * @return the constant expression value, or <code>null</code> if this
	 * expression has no constant expression value or if bindings were not
	 * requested when the AST was created
	 *  
	 */
	public final Object resolveConstantExpressionValue() {
		return this.ast.getBindingResolver().resolveConstantExpressionValue(this);
	}

	/**
	 * Resolves and returns the binding for the type of this expression.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the binding for the type of this expression, or
	 *    <code>null</code> if the type cannot be resolved
	 */
	public final ITypeBinding resolveTypeBinding() {
		return this.ast.getBindingResolver().resolveExpressionType(this);
	}

	/**
	 * Returns whether this expression node is the site of a boxing
	 * conversion (JLS3 5.1.7). This information is available only
	 * when bindings are requested when the AST is being built.
	 *
	 * @return <code>true</code> if this expression is the site of a
	 * boxing conversion, or <code>false</code> if either no boxing conversion
	 * is involved or if bindings were not requested when the AST was created
	 *  
	 */
	public final boolean resolveBoxing() {
		return this.ast.getBindingResolver().resolveBoxing(this);
	}

	/**
	 * Returns whether this expression node is the site of an unboxing
	 * conversion (JLS3 5.1.8). This information is available only
	 * when bindings are requested when the AST is being built.
	 *
	 * @return <code>true</code> if this expression is the site of an
	 * unboxing conversion, or <code>false</code> if either no unboxing
	 * conversion is involved or if bindings were not requested when the
	 * AST was created
	 *  
	 */
	public final boolean resolveUnboxing() {
		return this.ast.getBindingResolver().resolveUnboxing(this);
	}
}

