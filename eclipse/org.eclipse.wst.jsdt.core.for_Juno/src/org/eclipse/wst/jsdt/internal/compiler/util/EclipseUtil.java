/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.compiler.util;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;

public class EclipseUtil {

	private EclipseUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>Determine if the given path is a match for the given match path.  If one path is
	 * file system absolute and another is relative or absolute to the workspace then the
	 * path that is not file system absolute will be converted to file system absolute.
	 * The matching pattern can contain *, **, or ? wild cards.</p>
	 * 
	 * @param pathChars  check to see if this path matches the <code>matchpathChars</code>
	 * @param matchPathChars check to see if the given <code>pathChars</code> match this pattern
	 * @return <code>true</code> if the given <code>pathChars</code> match the given given
	 * <code>matchPathChars<code>, <code>false</code> otherwise.
	 */
	public static boolean pathMatch(char[] pathChars, char[] matchPathChars) {
		IPath path = new Path(new String(pathChars));
		IPath matchPath = new Path(new String(matchPathChars));
	
		//determine if either path is file system absolute
		IPath fileSystemWorkspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		boolean isPathFileSystemAbsolute = fileSystemWorkspacePath.isPrefixOf(path);
		boolean isMatchPathFileSystemAbsolute = fileSystemWorkspacePath.isPrefixOf(matchPath);
		
		/* if the two paths are not both file system absolute or both workspace absolute
		 * then transform the none file system absolute path to file system absolute
		 */
		if((!isPathFileSystemAbsolute && isMatchPathFileSystemAbsolute) || (isPathFileSystemAbsolute && !isMatchPathFileSystemAbsolute)){
			if(!isPathFileSystemAbsolute) {
				boolean hadTrailingSeparator = path.hasTrailingSeparator();
				path = ResourcesPlugin.getWorkspace().getRoot().getFile(path).getLocation();
				if(hadTrailingSeparator) {
					path = path.addTrailingSeparator();
				}
			}
			
			if(!isMatchPathFileSystemAbsolute) {
				boolean hadTrailingSeparator = matchPath.hasTrailingSeparator();
				matchPath = ResourcesPlugin.getWorkspace().getRoot().getFile(matchPath).getLocation();
				if(hadTrailingSeparator) {
					matchPath = matchPath.addTrailingSeparator();
				}
			}
		}
		
		//be sure both are absolute now (fixes 'project1\file.js' to '\project1\file.js')
		path = path.makeAbsolute();
		matchPath = matchPath.makeAbsolute();
				
		return CharOperation.pathMatch(matchPath.toPortableString().toCharArray(), path.toPortableString().toCharArray(), true, IPath.SEPARATOR);
	}
}
