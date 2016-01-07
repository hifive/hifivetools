/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.wst.jsdt.core.ast;

/**
 *  
 *  Representation of javascript file.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */

public interface IScriptFileDeclaration extends IASTNode{

	IProgramElement []getStatements();
	
	/**
	 * get the filename for the script if it can be determined
	 * @return the scripts file name,  this could be null
	 */
	char[] getFileName();
	
	/**
	 * get the inference ID for the script if it is located in a container that specified an Inference ID
	 * @return the inference ID for the script, could be null 
	 */
	String getInferenceID();
	
	public void addImport(char [] importName, int startPosition, int endPosition, int nameStartPosition);
	
}