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

/*
 * Selection node build by the parser in any case it was intending to
 * reduce an import reference containing the assist identifier.
 * e.g.
 *
 *  import java.[start]io[end].*;
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <SelectOnImport:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 */

import org.eclipse.wst.jsdt.internal.compiler.ast.ImportReference;

public class SelectionOnImportReference extends ImportReference {

public SelectionOnImportReference(char[][] tokens , long[] positions) {
	super(tokens, positions, false);
}
public StringBuffer print(int indent, StringBuffer output, boolean withOnDemand) {

	printIndent(indent, output).append("<SelectOnImport:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		if (i > 0) output.append('.');
		output.append(tokens[i]);
	}
	return output.append('>');
}
}
