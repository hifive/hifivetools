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

import org.eclipse.wst.jsdt.core.IJavaScriptElement;

/**
 * A binding represents a named entity in the JavaScript language. The world of
 * bindings provides an integrated picture of the structure of the program as
 * seen from the compiler's point of view. This interface declare protocol
 * common to the various different kinds of named entities in the JavaScript language:
 * packages, types, fields, methods, constructors, and local variables.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPackageBinding
 * @see ITypeBinding
 * @see IVariableBinding
 * @see IFunctionBinding
  * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
*/
public interface IBinding {

	/**
	 * Kind constant (value 1) indicating a package binding.
	 * Bindings of this kind can be safely cast to <code>IPackageBinding</code>.
	 *
	 * @see #getKind()
	 * @see IPackageBinding
	 */
	public static final int PACKAGE = 1;

	/**
	 * Kind constant (value 2) indicating a type binding.
	 * Bindings of this kind can be safely cast to <code>ITypeBinding</code>.
	 *
	 * @see #getKind()
	 * @see ITypeBinding
	 */
	public static final int TYPE = 2;

	/**
	 * Kind constant (value 3) indicating a field or local variable binding.
	 * Bindings of this kind can be safely cast to <code>IVariableBinding</code>.
	 *
	 * @see #getKind()
	 * @see IVariableBinding
	 */
	public static final int VARIABLE = 3;

	/**
	 * Kind constant (value 4) indicating a method or constructor binding.
	 * Bindings of this kind can be safely cast to <code>IFunctionBinding</code>.
	 *
	 * @see #getKind()
	 * @see IFunctionBinding
	 */
	public static final int METHOD = 4;



	/**
	 * Returns the kind of bindings this is. That is one of the kind constants:
	 * <code>PACKAGE</code>,
	 * 	<code>TYPE</code>,
	 * 	<code>VARIABLE</code>,
	 * 	<code>METHOD</code>,
	 * or <code>MEMBER_VALUE_PAIR</code>.
	 * <p>
	 * Note that additional kinds might be added in the
	 * future, so clients should not assume this list is exhaustive and
	 * should program defensively, e.g. by having a reasonable default
	 * in a switch statement.
	 * </p>
	 * @return one of the kind constants
	 */
	public int getKind();

	/**
	 * Returns the name of this binding.
	 * Details of the name are specified with each specific kind of binding.
	 *
	 * @return the name of this binding
	 */
	public String getName();

	/**
	 * Returns the modifiers for this binding.
	 * <p>
	 * Note that deprecated is not included among the modifiers.
	 * Use <code>isDeprecated</code> to find out whether a binding is deprecated.
	 * </p>
	 *
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 */
	public int getModifiers();

	/**
	 * Return whether this binding is for something that is deprecated.
	 * A deprecated class, interface, field, method, or constructor is one that
	 * is marked with the 'deprecated' tag in its jsdoc comment.
	 *
	 * @return <code>true</code> if this binding is deprecated, and
	 *    <code>false</code> otherwise
	 */
	public boolean isDeprecated();

	/**
	 * Return whether this binding is created because the bindings recovery is enabled. This binding is considered
	 * to be incomplete. Its internal state might be incomplete.
	 *
	 * @return <code>true</code> if this binding is a recovered binding, and
	 *    <code>false</code> otherwise
	 *  
	 */
	public boolean isRecovered();

	/**
	 * Returns the JavaScript element that corresponds to this binding.
	 * Returns <code>null</code> if this binding has no corresponding
	 * JavaScript element.
	 * <p>
	 * For array types, this method returns the JavaScript element that corresponds
	 * to the array's element type. For raw and parameterized types, this method
	 * returns the JavaScript element of the erasure. For annotations, this methods
	 * returns the JavaScript element of the annotation type.
	 * </p>
	 * <p>
	 * Here are the cases where a <code>null</code> should be expected:
	 * <ul>
	 * <li>primitive types, including void</li>
	 * <li>null type</li>
	 * <li>capture types</li>
	 * <li>array types of any of the above</li>
	 * <li>the "length" field of an array type</li>
	 * <li>the default constructor of a source class</li>
	 * <li>the constructor of an anonymous class</li>
	 * <li>member value pairs</li>
	 * </ul>
	 * For all other kind of type, method, variable, annotation and package bindings,
	 * this method returns non-<code>null</code>.
	 * </p>
	 *
	 * @return the JavaScript element that corresponds to this binding,
	 * 		or <code>null</code> if none
	 *  
	 */
	public IJavaScriptElement getJavaElement();

	/**
	 * Returns the key for this binding.
	 * <p>
	 * Within a connected cluster of bindings (for example, all bindings
	 * reachable from a given AST), each binding will have a distinct keys.
	 * The keys are generated in a manner that is predictable and as
	 * stable as possible. This last property makes these keys useful for
	 * comparing bindings between disconnected clusters of bindings (for example,
	 * the bindings between the "before" and "after" ASTs of the same
	 * javaScript unit).
	 * </p>
	 * <p>
	 * The exact details of how the keys are generated is unspecified.
	 * However, it is a function of the following information:
	 * <ul>
	 * <li>packages - the name of the package (for an unnamed package,
	 *   some internal id)</li>
	 * <li>classes or interfaces - the VM name of the type and the key
	 *   of its package</li>
	 * <li>array types - the key of the component type and number of
	 *   dimensions</li>
	 * <li>primitive types - the name of the primitive type</li>
	 * <li>fields - the name of the field and the key of its declaring
	 *   type</li>
	 * <li>methods - the name of the method, the key of its declaring
	 *   type, and the keys of the parameter types</li>
	 * <li>constructors - the key of its declaring class, and the
	 *   keys of the parameter types</li>
	 * <li>local variables - the name of the local variable, the index of the
	 *   declaring block relative to its parent, the key of its method</li>
	 * <li>local types - the name of the type, the index of the declaring
	 *   block relative to its parent, the key of its method</li>
	 * <li>anonymous types - the occurence count of the anonymous
	 *   type relative to its declaring type, the key of its declaring type</li>
	 * <li>enum types - treated like classes</li>
	 * <li>annotation types - treated like interfaces</li>
	 * <li>type variables - the name of the type variable and
	 * the key of the generic type or generic method that declares that
	 * type variable</li>
	 * <li>generic type instances - the key of the generic type and the keys
	 * of the type arguments used to instantiate it, and whether the
	 * instance is explicit (a parameterized type reference) or
	 * implicit (a raw type reference)</li>
	 * <li>generic method instances - the key of the generic method and the keys
	 * of the type arguments used to instantiate it, and whether the
	 * instance is explicit (a parameterized method reference) or
	 * implicit (a raw method reference)</li>
	 * <li>members of generic type instances - the key of the generic type
	 * instance and the key of the corresponding member in the generic
	 * type</li>
	 * </ul>
	 * </p>
	 * <p>Note that the key for annotation bindings and member value pair bindings is
	 * not yet implemented. This returns <code>null</code> for these 2 kinds of bindings.<br>
	 * Recovered bindings have a unique key.
	 * </p>
	 *
	 * @return the key for this binding
	 */
	public String getKey();

	/**
	 * There is no special definition of equality for bindings; equality is
	 * simply object identity.  Within the context of a single cluster of
	 * bindings, each binding is represented by a distinct object. However,
	 * between different clusters of bindings, the binding objects may or may
	 * not be different; in these cases, the client should compare bindings
	 * using {@link #isEqualTo(IBinding)}, which checks their keys.
	 *
	 * @param obj {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	public boolean equals(Object obj);

	/**
	 * Returns whether this binding has the same key as that of the given
	 * binding. Within the context of a single cluster of bindings, each
	 * binding is represented by a distinct object. However, between
	 * different clusters of bindings, the binding objects may or may
	 * not be different objects; in these cases, the binding keys
	 * are used where available.
	 *
	 * @param binding the other binding, or <code>null</code>
	 * @return <code>true</code> if the given binding is the identical
	 * object as this binding, or if the keys of both bindings are the
	 * same string; <code>false</code> if the given binding is
	 * <code>null</code>, or if the bindings do not have the same key,
	 * or if one or both of the bindings have no key
	 * @see #getKey()
	 *  
	 */
	public boolean isEqualTo(IBinding binding);

	/**
	 * Returns a string representation of this binding suitable for debugging
	 * purposes only.
	 *
	 * @return a debug string
	 */
	public String toString();
}
