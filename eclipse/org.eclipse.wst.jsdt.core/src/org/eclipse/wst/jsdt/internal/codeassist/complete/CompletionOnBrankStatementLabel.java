/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.codeassist.complete;

import org.eclipse.wst.jsdt.internal.compiler.ast.BranchStatement;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;

public class CompletionOnBrankStatementLabel extends BranchStatement {
	public static final int BREAK = 1;
	public static final int CONTINUE = 2;

	private int kind;
	public char[][] possibleLabels;

	public CompletionOnBrankStatementLabel(int kind, char[] l, int s, int e, char[][] possibleLabels) {
		super(l, s, e);
		this.kind = kind;
		this.possibleLabels = possibleLabels;
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {
		// Is never called
		return null;
	}

	public void resolve(BlockScope scope) {
		throw new CompletionNodeFound(this, scope);
	}
	public StringBuffer printStatement(int indent, StringBuffer output) {
		printIndent(indent, output);
		if(kind == CONTINUE) {
			output.append("continue "); //$NON-NLS-1$
		} else {
			output.append("break "); //$NON-NLS-1$
		}
		output.append("<CompleteOnLabel:"); //$NON-NLS-1$
		output.append(label);
		return output.append(">;"); //$NON-NLS-1$
	}

}
