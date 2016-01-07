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
package org.eclipse.wst.jsdt.internal.compiler.flow;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class LabelFlowContext extends SwitchFlowContext {

	public char[] labelName;

public LabelFlowContext(FlowContext parent, ASTNode associatedNode, char[] labelName, BlockScope scope) {
	super(parent, associatedNode);
	this.labelName = labelName;
	checkLabelValidity(scope);
}

void checkLabelValidity(BlockScope scope) {
	// check if label was already defined above
	FlowContext current = parent;
	while (current != null) {
		char[] currentLabelName;
		if (((currentLabelName = current.labelName()) != null)
			&& CharOperation.equals(currentLabelName, labelName)) {
			scope.problemReporter().alreadyDefinedLabel(labelName, associatedNode);
		}
		current = current.parent;
	}
}

public String individualToString() {
	return "Label flow context [label:" + String.valueOf(labelName) + "]"; //$NON-NLS-2$ //$NON-NLS-1$
}

public char[] labelName() {
	return labelName;
}
}
