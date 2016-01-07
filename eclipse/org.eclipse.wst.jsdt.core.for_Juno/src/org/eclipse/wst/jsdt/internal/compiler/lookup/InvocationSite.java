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
package org.eclipse.wst.jsdt.internal.compiler.lookup;

public interface InvocationSite {

	boolean isSuperAccess();
	boolean isTypeAccess();
	void setDepth(int depth);
	void setFieldIndex(int depth);

	// in case the receiver type does not match the actual receiver type
	// e.g. pkg.Type.C (receiver type of C is type of source context,
	//		but actual receiver type is pkg.Type)
	// e.g2. in presence of implicit access to enclosing type
	void setActualReceiverType(ReferenceBinding receiverType);
	int sourceStart();
	int sourceEnd();
}
