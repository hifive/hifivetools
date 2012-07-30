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
package org.eclipse.wst.jsdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;

public class AssistOptions {
	/**
	 * Option IDs
	 */
	public static final String OPTION_PerformVisibilityCheck =
		"org.eclipse.wst.jsdt.core.codeComplete.visibilityCheck"; 	//$NON-NLS-1$
	public static final String OPTION_PerformDeprecationCheck =
		"org.eclipse.wst.jsdt.core.codeComplete.deprecationCheck"; 	//$NON-NLS-1$
	public static final String OPTION_ForceImplicitQualification =
		"org.eclipse.wst.jsdt.core.codeComplete.forceImplicitQualification"; 	//$NON-NLS-1$
	public static final String OPTION_FieldPrefixes =
		"org.eclipse.wst.jsdt.core.codeComplete.fieldPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_StaticFieldPrefixes =
		"org.eclipse.wst.jsdt.core.codeComplete.staticFieldPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_LocalPrefixes =
		"org.eclipse.wst.jsdt.core.codeComplete.localPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_ArgumentPrefixes =
		"org.eclipse.wst.jsdt.core.codeComplete.argumentPrefixes"; 	//$NON-NLS-1$
	public static final String OPTION_FieldSuffixes =
		"org.eclipse.wst.jsdt.core.codeComplete.fieldSuffixes"; 	//$NON-NLS-1$
	public static final String OPTION_StaticFieldSuffixes =
		"org.eclipse.wst.jsdt.core.codeComplete.staticFieldSuffixes"; 	//$NON-NLS-1$
	public static final String OPTION_LocalSuffixes =
		"org.eclipse.wst.jsdt.core.codeComplete.localSuffixes"; 	//$NON-NLS-1$
	public static final String OPTION_ArgumentSuffixes =
		"org.eclipse.wst.jsdt.core.codeComplete.argumentSuffixes"; 	//$NON-NLS-1$
	public static final String OPTION_PerformForbiddenReferenceCheck =
		"org.eclipse.wst.jsdt.core.codeComplete.forbiddenReferenceCheck"; 	//$NON-NLS-1$
	public static final String OPTION_PerformDiscouragedReferenceCheck =
		"org.eclipse.wst.jsdt.core.codeComplete.discouragedReferenceCheck"; 	//$NON-NLS-1$
	public static final String OPTION_CamelCaseMatch =
		"org.eclipse.wst.jsdt.core.codeComplete.camelCaseMatch"; 	//$NON-NLS-1$
	public static final String OPTION_SuggestStaticImports =
		"org.eclipse.wst.jsdt.core.codeComplete.suggestStaticImports"; 	//$NON-NLS-1$

	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	public static final String DISABLED = "disabled"; //$NON-NLS-1$

	public boolean checkVisibility = false;
	public boolean checkDeprecation = false;
	public boolean checkForbiddenReference = false;
	public boolean checkDiscouragedReference = false;
	public boolean forceImplicitQualification = false;
	public boolean camelCaseMatch = true;
	public boolean suggestStaticImport = true;
	public char[][] fieldPrefixes = null;
	public char[][] staticFieldPrefixes = null;
	public char[][] localPrefixes = null;
	public char[][] argumentPrefixes = null;
	public char[][] fieldSuffixes = null;
	public char[][] staticFieldSuffixes = null;
	public char[][] localSuffixes = null;
	public char[][] argumentSuffixes = null;

	/**
	 * Initializing the assist options with default settings
	 */
	public AssistOptions() {
		// Initializing the assist options with default settings
	}

	/**
	 * Initializing the assist options with external settings
	 */
	public AssistOptions(Map settings) {
		if (settings == null)
			return;

		set(settings);
	}
	public void set(Map optionsMap) {

		Object optionValue;
		if ((optionValue = optionsMap.get(OPTION_PerformVisibilityCheck)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.checkVisibility = true;
			} else if (DISABLED.equals(optionValue)) {
				this.checkVisibility = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ForceImplicitQualification)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.forceImplicitQualification = true;
			} else if (DISABLED.equals(optionValue)) {
				this.forceImplicitQualification = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_FieldPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.fieldPrefixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.fieldPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_StaticFieldPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.staticFieldPrefixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.staticFieldPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_LocalPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.localPrefixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.localPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ArgumentPrefixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.argumentPrefixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.argumentPrefixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_FieldSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.fieldSuffixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.fieldSuffixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_StaticFieldSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.staticFieldSuffixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.staticFieldSuffixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_LocalSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.localSuffixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.localSuffixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ArgumentSuffixes)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					this.argumentSuffixes = this.splitAndTrimOn(',', stringValue.toCharArray());
				} else {
					this.argumentSuffixes = null;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_PerformForbiddenReferenceCheck)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.checkForbiddenReference = true;
			} else if (DISABLED.equals(optionValue)) {
				this.checkForbiddenReference = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_PerformDiscouragedReferenceCheck)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.checkDiscouragedReference = true;
			} else if (DISABLED.equals(optionValue)) {
				this.checkDiscouragedReference = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_CamelCaseMatch)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.camelCaseMatch = true;
			} else if (DISABLED.equals(optionValue)) {
				this.camelCaseMatch = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_PerformDeprecationCheck)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.checkDeprecation = true;
			} else if (DISABLED.equals(optionValue)) {
				this.checkDeprecation = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_SuggestStaticImports)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.suggestStaticImport = true;
			} else if (DISABLED.equals(optionValue)) {
				this.suggestStaticImport = false;
			}
		}
	}

	private char[][] splitAndTrimOn(char divider, char[] arrayToSplit) {
		char[][] result = CharOperation.splitAndTrimOn(',', arrayToSplit);

		int length = result.length;

		int resultCount = 0;
		for (int i = 0; i < length; i++) {
			if(result[i].length != 0) {
				result[resultCount++] = result[i];
			}
		}
		if(resultCount != length) {
			System.arraycopy(result, 0, result = new char[resultCount][], 0, resultCount);
		}
		return result;
	}
}
