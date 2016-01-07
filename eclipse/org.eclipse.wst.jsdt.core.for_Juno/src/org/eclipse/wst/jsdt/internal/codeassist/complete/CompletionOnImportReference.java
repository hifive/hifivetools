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

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an import reference containing the cursor location.
 * e.g.
 *
 *  import java.io[cursor];
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <CompleteOnImport:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */

import org.eclipse.wst.jsdt.internal.compiler.ast.ImportReference;

public class CompletionOnImportReference extends ImportReference {

public CompletionOnImportReference(char[][] tokens , long[] positions) {
	super(tokens, positions, false);
}
public StringBuffer print(int indent, StringBuffer output, boolean withOnDemand) {

	printIndent(indent, output).append("<CompleteOnImport:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		if (i > 0) output.append('.');
		output.append(tokens[i]);
	}
	return output.append('>');
}
}
