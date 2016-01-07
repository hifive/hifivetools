/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.libraries;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJsGlobalScopeContainer;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JsGlobalScopeContainerInitializer;
import org.eclipse.wst.jsdt.core.compiler.libraries.LibraryLocation;
import org.eclipse.wst.jsdt.core.compiler.libraries.SystemLibraryLocation;
import org.eclipse.wst.jsdt.core.infer.DefaultInferrenceProvider;

/**
 * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public class BasicBrowserLibraryJsGlobalScopeContainerInitializer extends JsGlobalScopeContainerInitializer implements IJsGlobalScopeContainer {
	private static final String CONTAINER_ID = org.eclipse.wst.jsdt.launching.JavaRuntime.BASE_BROWSER_LIB; //$NON-NLS-1$
	private static final String BROWSER_SUPER_TYPE_NAME = "Window"; //$NON-NLS-1$
		
	/**
	 * Union of all filenames composing web browser libraries
	 */
	private static final char[][] LIBRARY_FILE_NAME = {
		{ 'b', 'a', 's', 'e', 'B', 'r', 'o', 'w', 's', 'e', 'r', 'L', 'i', 'b', 'r', 'a', 'r', 'y', '.', 'j', 's' },
		{'b','r','o','w','s','e','r','W','i','n','d','o','w','.','j','s'},
		{'x','h','r','.','j','s'},
		"dom5.js".toCharArray()
	  };

	/**
	 * Filenames composing our HTML4.01 web browser library
	 */
	static final char[][] LIBRARY_FILE_NAME4 = {
		{ 'b', 'a', 's', 'e', 'B', 'r', 'o', 'w', 's', 'e', 'r', 'L', 'i', 'b', 'r', 'a', 'r', 'y', '.', 'j', 's' },
		{'b','r','o','w','s','e','r','W','i','n','d','o','w','.','j','s'},
		{'x','h','r','.','j','s'}
	  };

	
	/**
	 * Filenames composing our HTML5-compatible library
	 * TODO: create new and unique files with the intended library content
	 */
	static final char[][] LIBRARY_FILE_NAME5 = {
		"dom5.js".toCharArray(),
		{'b','r','o','w','s','e','r','W','i','n','d','o','w','.','j','s'},
		{'x','h','r','.','j','s'}
	};

	static class BasicLibLocation extends SystemLibraryLocation {
		BasicLibLocation() {
			super();
		}
		
		public char[][] getLibraryFileNames() {
			return  BasicBrowserLibraryJsGlobalScopeContainerInitializer.LIBRARY_FILE_NAME ;
		}
		
		static LibraryLocation fInstance;
		
		public static LibraryLocation getInstance(){
			if(fInstance== null){
				fInstance = new BasicLibLocation();
			}
			return fInstance;
		}
	}
	
	public LibraryLocation getLibraryLocation() {
		return BasicLibLocation.getInstance();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.JsGlobalScopeContainerInitializer#canUpdateJsGlobalScopeContainer(org.eclipse.core.runtime.IPath, org.eclipse.wst.jsdt.core.IJavaScriptProject)
	 */
	public boolean canUpdateJsGlobalScopeContainer(IPath containerPath, IJavaScriptProject project) {
		return !containerPath.isEmpty() && CONTAINER_ID.equals(containerPath.segment(0));
	}

	protected IJsGlobalScopeContainer getContainer(IPath containerPath, IJavaScriptProject project) {
		return new BasicBrowserLibraryContainer(containerPath);
	}
	
	public String getDescription(IPath containerPath, IJavaScriptProject project) {
		if (containerPath == null || containerPath.isEmpty())
			return null;

		return containerPath.lastSegment(); 
	}
	
	public void initialize(IPath containerPath, IJavaScriptProject project) throws CoreException {
		JavaScriptCore.setJsGlobalScopeContainer(containerPath, new IJavaScriptProject[] { project }, new IJsGlobalScopeContainer[] { getContainer(containerPath, project) }, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.jsdt.core.JsGlobalScopeContainerInitializer#containerSuperTypes()
	 */
	public String[] containerSuperTypes() {
		return new String[] {BROWSER_SUPER_TYPE_NAME};
	}

	public String getInferenceID() {
		return DefaultInferrenceProvider.ID;
	}
}
