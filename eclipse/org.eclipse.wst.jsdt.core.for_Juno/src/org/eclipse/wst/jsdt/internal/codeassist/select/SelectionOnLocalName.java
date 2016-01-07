/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.codeassist.select;

import org.eclipse.wst.jsdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;

public class SelectionOnLocalName extends LocalDeclaration{

	public SelectionOnLocalName(char[] name, int sourceStart, int sourceEnd) {

		super(name, sourceStart, sourceEnd);
	}

	public void resolve(BlockScope scope) {

		super.resolve(scope);
		throw new SelectionNodeFound(binding);
	}

	public StringBuffer printAsExpression(int indent, StringBuffer output) {
		printIndent(indent, output);
		output.append("<SelectionOnLocalName:"); //$NON-NLS-1$
		printModifiers(this.modifiers, output);
		output.append(this.name);
		if (initialization != null) {
			output.append(" = "); //$NON-NLS-1$
			initialization.printExpression(0, output);
		}
		return output.append('>');
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {
		this.printAsExpression(indent, output);
		return output.append(';');
	}
}
