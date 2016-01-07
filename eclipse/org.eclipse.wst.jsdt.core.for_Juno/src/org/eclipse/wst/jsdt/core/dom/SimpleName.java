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

package org.eclipse.wst.jsdt.core.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.jsdt.core.compiler.InvalidInputException;
import org.eclipse.wst.jsdt.internal.compiler.parser.Scanner;
import org.eclipse.wst.jsdt.internal.compiler.parser.TerminalTokens;

/**
 * AST node for a simple name. A simple name is an identifier other than
 * a keyword, boolean literal ("true", "false") or null literal ("null").
 * <pre>
 * SimpleName:
 *     Identifier
 * </pre>
 * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public class SimpleName extends Name {

	/**
	 * The "identifier" structural property of this node type.
	 *
	 *  
	 */
	public static final SimplePropertyDescriptor IDENTIFIER_PROPERTY =
		new SimplePropertyDescriptor(SimpleName.class, "identifier", String.class, MANDATORY); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 *  
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(SimpleName.class, propertyList);
		addProperty(IDENTIFIER_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the AST.JLS* constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 *  
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * An unspecified (but externally observable) legal JavaScript identifier.
	 */
	private static final String MISSING_IDENTIFIER = "MISSING";//$NON-NLS-1$

	/**
	 * The identifier; defaults to a unspecified, legal JavaScript identifier.
	 */
	private String identifier = MISSING_IDENTIFIER;

	/**
	 * Creates a new AST node for a simple name owned by the given AST.
	 * The new node has an unspecified, legal JavaScript identifier.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	SimpleName(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 *  
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == IDENTIFIER_PROPERTY) {
			if (get) {
				return getIdentifier();
			} else {
				setIdentifier((String) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return SIMPLE_NAME;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		SimpleName result = new SimpleName(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setIdentifier(getIdentifier());
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
		visitor.visit(this);
		visitor.endVisit(this);
	}

	/**
	 * Returns this node's identifier.
	 *
	 * @return the identifier of this node
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * Sets the identifier of this node to the given value.
	 * The identifier should be legal according to the rules
	 * of the JavaScript language. Note that keywords are not legal
	 * identifiers.
	 * <p>
	 * Note that the list of keywords may depend on the version of the
	 * language (determined when the AST object was created).
	 * </p>
	 *
	 * @param identifier the identifier of this node
	 * @exception IllegalArgumentException if the identifier is invalid
	 */
	public void setIdentifier(String identifier) {
		// update internalSetIdentifier if this is changed
		if (identifier == null) {
			throw new IllegalArgumentException();
		}
		Scanner scanner = this.ast.scanner;
		char[] source = identifier.toCharArray();
		scanner.setSource(source);
		final int length = source.length;
		scanner.resetTo(0, length - 1);
		try {
			int tokenType = scanner.scanIdentifier();
			if (tokenType != TerminalTokens.TokenNameIdentifier) {
				throw new IllegalArgumentException();
			}
			if (scanner.currentPosition != length) {
				// this is the case when there is only one identifier see 87849
				throw new IllegalArgumentException();
			}
		} catch(InvalidInputException e) {
			throw new IllegalArgumentException();
		}
		preValueChange(IDENTIFIER_PROPERTY);
		this.identifier = identifier;
		postValueChange(IDENTIFIER_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * This method is a copy of setIdentifier(String) that doesn't do any validation.
	 */
	void internalSetIdentifier(String ident) {
		preValueChange(IDENTIFIER_PROPERTY);
		this.identifier = ident;
		postValueChange(IDENTIFIER_PROPERTY);
	}

	/**
	 * Returns whether this simple name represents a name that is being defined,
	 * as opposed to one being referenced. The following positions are considered
	 * ones where a name is defined:
	 * <ul>
	 * <li>The type name in a <code>TypeDeclaration</code> node.</li>
	 * <li>The method name in a <code>FunctionDeclaration</code> node
	 * providing <code>isConstructor</code> is <code>false</code>.</li>
	 * <li>The variable name in any type of <code>VariableDeclaration</code>
	 * node.</li>
	 * <li>The enum type name in a <code>EnumDeclaration</code> node.</li>
	 * <li>The enum constant name in an <code>EnumConstantDeclaration</code>
	 * node.</li>
	 * <li>The variable name in an <code>EnhancedForStatement</code>
	 * node.</li>
	 * <li>The type variable name in a <code>TypeParameter</code>
	 * node.</li>
	 * <li>The type name in an <code>AnnotationTypeDeclaration</code> node.</li>
	 * <li>The member name in an <code>AnnotationTypeMemberDeclaration</code> node.</li>
	 * </ul>
	 * <p>
	 * Note that this is a convenience method that simply checks whether
	 * this node appears in the declaration position relative to its parent.
	 * It always returns <code>false</code> if this node is unparented.
	 * </p>
	 *
	 * @return <code>true</code> if this node declares a name, and
	 *    <code>false</code> otherwise
	 */
	public boolean isDeclaration() {
		StructuralPropertyDescriptor d = getLocationInParent();
		if (d == null) {
			// unparented node
			return false;
		}
		ASTNode parent = getParent();
		if (parent instanceof TypeDeclaration) {
			return (d == TypeDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof FunctionDeclaration) {
			FunctionDeclaration p = (FunctionDeclaration) parent;
			// could be the name of the method or constructor
			return !p.isConstructor() && (d == FunctionDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof SingleVariableDeclaration) {
			return (d == SingleVariableDeclaration.NAME_PROPERTY);
		}
		if (parent instanceof VariableDeclarationFragment) {
			return (d == VariableDeclarationFragment.NAME_PROPERTY);
		}
		
		return false;
	}

	/* (omit javadoc for this method)
	 * Method declared on Name.
	 */
	void appendName(StringBuffer buffer) {
		buffer.append(getIdentifier());
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NAME_NODE_SIZE + 2 * 4;
		if (identifier != MISSING_IDENTIFIER) {
			// everything but our missing id costs
			size += stringSize(identifier);
		}
		return size;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}

