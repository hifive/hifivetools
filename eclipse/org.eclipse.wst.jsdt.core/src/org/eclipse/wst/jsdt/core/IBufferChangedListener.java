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
package org.eclipse.wst.jsdt.core;

/**
 * A listener, which gets notified when the contents of a specific buffer
 * have changed, or when the buffer is closed.
 * When a buffer is closed, the listener is notified <em>after</em> the buffer has been closed.
 * A listener is not notified when a buffer is saved.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *  
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
*/
public interface IBufferChangedListener {

	/**
	 * Notifies that the given event has occurred.
	 *
	 * @param event the change event
	 */
	public void bufferChanged(BufferChangedEvent event);
}
