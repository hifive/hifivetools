/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	int

		ERROR_SYMBOL = 120,
		MAX_NAME_LENGTH = 36,
		NUM_STATES = 558,

		NT_OFFSET = 120,
		SCOPE_UBOUND = 56,
		SCOPE_SIZE = 57,
		LA_STATE_OFFSET = 7569,
		MAX_LA = 1,
		NUM_RULES = 398,
		NUM_TERMINALS = 120,
		NUM_NON_TERMINALS = 180,
		NUM_SYMBOLS = 300,
		START_STATE = 437,
		EOFT_SYMBOL = 70,
		EOLT_SYMBOL = 70,
		ACCEPT_ACTION = 7568,
		ERROR_ACTION = 7569;

}
