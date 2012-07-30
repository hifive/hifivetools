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
package org.eclipse.wst.jsdt.internal.compiler.batch;

import java.io.File;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRestriction;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.wst.jsdt.internal.compiler.util.SuffixConstants;

public abstract class ClasspathLocation implements FileSystem.Classpath,
		SuffixConstants {

	public static final int SOURCE = 1;
	public static final int BINARY = 2;

	public AccessRuleSet accessRuleSet;

	public String destinationPath;
		// destination path for compilation units that are reached through this
		// classpath location; the coding is consistent with the one of
		// Main.destinationPath:
		// == null: unspecified, use whatever value is set by the enclosing
		//          context, id est Main;
		// == Main.NONE: absorbent element, do not output class files;
		// else: use as the path of the directory into which class files must
		//       be written.
		// potentially carried by any entry that contains to be compiled files

	protected ClasspathLocation(AccessRuleSet accessRuleSet,
			String destinationPath) {
		this.accessRuleSet = accessRuleSet;
		this.destinationPath = destinationPath;
	}

	/**
	 * Return the first access rule which is violated when accessing a given
	 * type, or null if no 'non accessible' access rule applies.
	 *
	 * @param qualifiedBinaryFileName
	 *            tested type specification, formed as:
	 *            "org/eclipse/jdt/core/JavaScriptCore.class"; on systems that
	 *            use \ as File.separator, the
	 *            "org\eclipse\jdt\core\JavaScriptCore.class" is accepted as well
	 * @return the first access rule which is violated when accessing a given
	 *         type, or null if none applies
	 */
	protected AccessRestriction fetchAccessRestriction(String qualifiedBinaryFileName) {
		if (this.accessRuleSet == null)
			return null;
		char [] qualifiedTypeName = qualifiedBinaryFileName.
			substring(0, qualifiedBinaryFileName.length() - SUFFIX_java.length)
			.toCharArray();
		if (File.separatorChar == '\\') {
			CharOperation.replace(qualifiedTypeName, File.separatorChar, '/');
		}
		return this.accessRuleSet.getViolatedRestriction(qualifiedTypeName);
	}
}
