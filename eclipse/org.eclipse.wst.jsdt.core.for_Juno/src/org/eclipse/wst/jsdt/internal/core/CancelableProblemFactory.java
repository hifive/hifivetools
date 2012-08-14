/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.wst.jsdt.core.compiler.CategorizedProblem;
import org.eclipse.wst.jsdt.internal.compiler.problem.DefaultProblemFactory;


public class CancelableProblemFactory extends DefaultProblemFactory {
	public IProgressMonitor monitor;

	public CancelableProblemFactory(IProgressMonitor monitor) {
		super();
		this.monitor = monitor;
	}

	public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber) {
		//if canceled throw exception
		if (this.monitor != null && this.monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		return super.createProblem(originatingFileName, problemId, problemArguments, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
	}
}
