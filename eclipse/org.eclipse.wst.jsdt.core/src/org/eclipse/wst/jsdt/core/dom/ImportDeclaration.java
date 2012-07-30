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

import java.util.ArrayList;
import java.util.List;

/**
 * Import declaration AST node type.
 *
 * For JLS2:
 * <pre>
 * ImportDeclaration:
 *    <b>import</b> Name [ <b>.</b> <b>*</b> ] <b>;</b>
 * </pre>
 * For JLS3, static was added:
 * <pre>
 * ImportDeclaration:
 *    <b>import</b> [ <b>static</b> ] Name [ <b>.</b> <b>*</b> ] <b>;</b>
 * </pre>
 * 
 * <p><b>Note: This Class only applies to ECMAScript 4 which is not yet supported</b></p>
 *
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public class ImportDeclaration extends ASTNode {

	/**
	 * The "name" structural property of this node type.
	 *  
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY =
		new ChildPropertyDescriptor(ImportDeclaration.class, "name", Name.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "onDemand" structural property of this node type.
	 *  
	 */
	public static final SimplePropertyDescriptor ON_DEMAND_PROPERTY =
		new SimplePropertyDescriptor(ImportDeclaration.class, "onDemand", boolean.class, MANDATORY); //$NON-NLS-1$

	/**
	 * The "static" structural property of this node type (added in JLS3 API).
	 *  
	 */
	public static final SimplePropertyDescriptor STATIC_PROPERTY =
		new SimplePropertyDescriptor(ImportDeclaration.class, "static", boolean.class, MANDATORY); //$NON-NLS-1$


	public static final SimplePropertyDescriptor ISFILE_PROPERTY =
		new SimplePropertyDescriptor(ImportDeclaration.class, "isFile", boolean.class, MANDATORY); //$NON-NLS-1$

	
	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 *  
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 *  
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;

	static {
		List properyList = new ArrayList(3);
		createPropertyList(ImportDeclaration.class, properyList);
		addProperty(NAME_PROPERTY, properyList);
		addProperty(ON_DEMAND_PROPERTY, properyList);
		addProperty(ISFILE_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(properyList);

		properyList = new ArrayList(4);
		createPropertyList(ImportDeclaration.class, properyList);
		addProperty(STATIC_PROPERTY, properyList);
		addProperty(NAME_PROPERTY, properyList);
		addProperty(ON_DEMAND_PROPERTY, properyList);
		addProperty(ISFILE_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(properyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants

	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 *  
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}

	/**
	 * The import name; lazily initialized; defaults to a unspecified,
	 * legal JavaScript identifier.
	 */
	private Name importName = null;

	/**
	 * On demand versus single type import; defaults to single type import.
	 */
	private boolean onDemand = false;

	/**
	 * Static versus regular; defaults to regular import.
	 * Added in JLS3; not used in JLS2.
	 *  
	 */
	private boolean isStatic = false;

	private boolean isFile = false;
	/**
	 * Creates a new AST node for an import declaration owned by the
	 * given AST. The import declaration initially is a regular (non-static)
	 * single type import for an unspecified, but legal, JavaScript type name.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	ImportDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == ON_DEMAND_PROPERTY) {
			if (get) {
				return isOnDemand();
			} else {
				setOnDemand(value);
				return false;
			}
		}
		if (property == STATIC_PROPERTY) {
			if (get) {
				return isStatic();
			} else {
				setStatic(value);
				return false;
			}
		}
		if (property == ISFILE_PROPERTY) {
			if (get) {
				return isFileImport();
			} else {
				setIsFileImport(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((Name) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return IMPORT_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		ImportDeclaration result = new ImportDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setOnDemand(isOnDemand());
		result.setIsFileImport(isFileImport());
		if (this.ast.apiLevel >= AST.JLS3) {
			result.setStatic(isStatic());
		}
		result.setName((Name) getName().clone(target));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the name imported by this declaration.
	 * <p>
	 * For a regular on-demand import, this is the name of a package.
	 * For a static on-demand import, this is the qualified name of
	 * a type. For a regular single-type import, this is the qualified name
	 * of a type. For a static single-type import, this is the qualified name
	 * of a static member of a type.
	 * </p>
	 *
	 * @return the imported name node
	 */
	public Name getName()  {
		if (this.importName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.importName == null) {
					preLazyInit();
					this.importName =this.ast.newQualifiedName(
							new SimpleName(this.ast), new SimpleName(this.ast));
					postLazyInit(this.importName, NAME_PROPERTY);
				}
			}
		}
		return importName;
	}

	/**
	 * Sets the name of this import declaration to the given name.
	 * <p>
	 * For a regular on-demand import, this is the name of a package.
	 * For a static on-demand import, this is the qualified name of
	 * a type. For a regular single-type import, this is the qualified name
	 * of a type. For a static single-type import, this is the qualified name
	 * of a static member of a type.
	 * </p>
	 *
	 * @param name the new import name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.importName;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.importName = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}

	/**
	 * Returns whether this import declaration is an on-demand or a
	 * single-type import.
	 *
	 * @return <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */
	public boolean isOnDemand() {
		return onDemand;
	}

	/**
	 * Sets whether this import declaration is an on-demand or a
	 * single-type import.
	 *
	 * @param onDemand <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */
	public void setOnDemand(boolean onDemand) {
		preValueChange(ON_DEMAND_PROPERTY);
		this.onDemand = onDemand;
		postValueChange(ON_DEMAND_PROPERTY);
	}

	public void setIsFileImport(boolean isFileImport) {
		preValueChange(ISFILE_PROPERTY);
		this.isFile = isFileImport;
		postValueChange(ISFILE_PROPERTY);
	}

	/**
	 * Returns whether this import declaration is a static import (added in JLS3 API).
	 *
	 * @return <code>true</code> if this is a static import,
	 *    and <code>false</code> if this is a regular import
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 *  
	 */
	public boolean isStatic() {
		unsupportedIn2();
		return isStatic;
	}

	/**
	 * Sets whether this import declaration is a static import (added in JLS3 API).
	 *
	 * @param isStatic <code>true</code> if this is a static import,
	 *    and <code>false</code> if this is a regular import
	 * @exception UnsupportedOperationException if this operation is used in
	 * a JLS2 AST
	 *  
	 */
	public void setStatic(boolean isStatic) {
		unsupportedIn2();
		preValueChange(STATIC_PROPERTY);
		this.isStatic = isStatic;
		postValueChange(STATIC_PROPERTY);
	}

	/**
	 * Resolves and returns the binding for the package, type, field, or
	 * method named in this import declaration.
	 * <p>
	 * The name specified in a non-static single-type import can resolve
	 * to a type (only). The name specified in a non-static on-demand
	 * import can itself resolve to either a package or a type.
	 * For static imports (introduced in JLS3), the name specified in a
	 * static on-demand import can itself resolve to a type (only).
	 * The name specified in a static single import can resolve to a
	 * type, field, or method; in cases where the name could be resolved
	 * to more than one element with that name (for example, two
	 * methods both named "max", or a method and a field), this method
	 * returns one of the plausible bindings.
	 * </p>
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return a package, type, field, or method binding, or <code>null</code>
	 * if the binding cannot be resolved
	 */
	public IBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveImport(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 3 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (importName == null ? 0 : getName().treeSize());
	}
	
	public boolean isFileImport()
	{
		return isFile;
	}
}

