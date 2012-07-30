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
package org.eclipse.wst.jsdt.internal.codeassist.complete;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;


public class CompletionOnLocalName extends LocalDeclaration {
	private static final char[] FAKENAMESUFFIX = " ".toCharArray(); //$NON-NLS-1$
	public char[] realName;

	public CompletionOnLocalName(char[] name, int sourceStart, int sourceEnd){

		super(CharOperation.concat(name, FAKENAMESUFFIX), sourceStart, sourceEnd);
		this.realName = name;
	}

	public void resolve(BlockScope scope) {

		super.resolve(scope);
		throw new CompletionNodeFound(this, scope);
	}

	public StringBuffer printAsExpression(int indent, StringBuffer output) {
		printIndent(indent, output);
		output.append("<CompleteOnLocalName:"); //$NON-NLS-1$
		if (type != null)  type.print(0, output).append(' ');
		output.append(this.realName);
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

