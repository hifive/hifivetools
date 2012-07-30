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
package org.eclipse.wst.jsdt.internal.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * Exception thrown when there is an internal error in the image builder.
 * May wrapper another exception.
 */
public class ImageBuilderInternalException extends RuntimeException {

private static final long serialVersionUID = 28252254530437336L; // backward compatible
protected CoreException coreException;

public ImageBuilderInternalException(CoreException e) {
	this.coreException = e;
}

public String getLocalizedMessage() {
	IStatus status = this.coreException.getStatus();
	if (status.isMultiStatus()) {
		IStatus[] children = status.getChildren();
		if (children != null && children.length > 0)
		    return children[0].getMessage();
	}
    return this.coreException.getLocalizedMessage();
}

public CoreException getThrowable() {
	return coreException;
}

public void printStackTrace() {
	if (coreException != null) {
		System.err.println(this);
		System.err.println("Stack trace of embedded core exception:"); //$NON-NLS-1$
		coreException.printStackTrace();
	} else {
		super.printStackTrace();
	}
}
}
