/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
 * Descriptor for a child property of an AST node.
 * A child property is one whose value is an
 * {@link ASTNode}.
 *
 * @see org.eclipse.wst.jsdt.core.dom.ASTNode#getStructuralProperty(StructuralPropertyDescriptor)
 * @see org.eclipse.wst.jsdt.core.dom.ASTNode#setStructuralProperty(StructuralPropertyDescriptor, Object)
 *
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public final class ChildPropertyDescriptor extends StructuralPropertyDescriptor {

	/**
	 * Child type. For example, for a node type like
	 * JavaScriptUnit, the "package" property is PackageDeclaration.class
	 */
	private final Class childClass;

	/**
	 * Indicates whether the child is mandatory. A child property is allowed
	 * to be <code>null</code> only if it is not mandatory.
	 */
	private final boolean mandatory;

	/**
	 * Indicates whether a cycle is possible.
	 * Field is private, but marked package-visible for fast
	 * access from ASTNode.
	 */
	final boolean cycleRisk;

	/**
	 * Creates a new child property descriptor with the given property id.
	 * Note that this constructor is declared package-private so that
	 * property descriptors can only be created by the AST
	 * implementation.
	 *
	 * @param nodeClass concrete AST node type that owns this property
	 * @param propertyId the property id
	 * @param childType the child type of this property
	 * @param mandatory <code>true</code> if the property is mandatory,
	 * and <code>false</code> if it is may be <code>null</code>
	 * @param cycleRisk <code>true</code> if this property is at
	 * risk of cycles, and <code>false</code> if there is no worry about cycles
	 */
	ChildPropertyDescriptor(Class nodeClass, String propertyId, Class childType, boolean mandatory, boolean cycleRisk) {
		super(nodeClass, propertyId);
		if (childType == null || !ASTNode.class.isAssignableFrom(childType)) {
			throw new IllegalArgumentException();
		}
		this.childClass = childType;
		this.mandatory = mandatory;
		this.cycleRisk = cycleRisk;
	}

	/**
	 * Returns the child type of this property.
	 * <p>
	 * For example, for a node type like JavaScriptUnit,
	 * the "package" property returns <code>PackageDeclaration.class</code>.
	 * </p>
	 *
	 * @return the child type of the property
	 */
	public final Class getChildType() {
		return this.childClass;
	}

	/**
	 * Returns whether this property is mandatory. A property value
	 * is not allowed to be <code>null</code> if it is mandatory.
	 *
	 * @return <code>true</code> if the property is mandatory,
	 * and <code>false</code> if it is may be <code>null</code>
	 */
	public final boolean isMandatory() {
		return this.mandatory;
	}

	/**
	 * Returns whether this property is vulnerable to cycles.
	 * <p>
	 * A property is vulnerable to cycles if a node of the owning
	 * type (that is, the type that owns this property) could legally
	 * appear in the AST subtree below this property. For example,
	 * the body property of a
	 * {@link FunctionDeclaration} node
	 * admits a body which might include statement that embeds
	 * another {@link FunctionDeclaration} node.
	 * On the other hand, the name property of a
	 * FunctionDeclaration node admits only names, and thereby excludes
	 * another FunctionDeclaration node.
	 * </p>
	 *
	 * @return <code>true</code> if cycles are possible,
	 * and <code>false</code> if cycles are impossible
	 */
	public final boolean cycleRisk() {
		return this.cycleRisk;
	}
}
