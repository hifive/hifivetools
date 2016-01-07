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
package org.eclipse.wst.jsdt.internal.compiler;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.parser.JavadocParser;
import org.eclipse.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.wst.jsdt.internal.compiler.parser.ScannerHelper;

public class SourceJavadocParser extends JavadocParser {

	// Store categories identifiers parsed in javadoc
	int categoriesPtr = -1;
	char[][] categories = CharOperation.NO_CHAR_CHAR;

public SourceJavadocParser(Parser sourceParser) {
	super(sourceParser);
	this.kind = SOURCE_PARSER | TEXT_VERIF;
}

public boolean checkDeprecation(int commentPtr) {
	this.categoriesPtr = -1;
	boolean result = super.checkDeprecation(commentPtr);
	if (this.categoriesPtr > -1) {
		System.arraycopy(this.categories, 0, this.categories = new char[this.categoriesPtr+1][], 0, this.categoriesPtr+1);
	} else {
		this.categories = CharOperation.NO_CHAR_CHAR;
	}
	return result;
}

/* (non-Javadoc)
 * @see org.eclipse.wst.jsdt.internal.compiler.parser.AbstractCommentParser#parseIdentifierTag()
 */
protected boolean parseIdentifierTag(boolean report) {
	int end = this.lineEnd+1;
	if (super.parseIdentifierTag(report) && this.index <= end) {
		return true;
	}
	return false;
}

/* (non-Javadoc)
 * @see org.eclipse.wst.jsdt.internal.compiler.parser.JavadocParser#parseSimpleTag()
 */
protected void parseSimpleTag() {

	// Read first char
	// readChar() code is inlined to balance additional method call in checkDeprectation(int)
	char first = this.source[this.index++];
	if (first == '\\' && this.source[this.index] == 'u') {
		int c1, c2, c3, c4;
		int pos = this.index;
		this.index++;
		while (this.source[this.index] == 'u')
			this.index++;
		if (!(((c1 = ScannerHelper.getNumericValue(this.source[this.index++])) > 15 || c1 < 0)
				|| ((c2 = ScannerHelper.getNumericValue(this.source[this.index++])) > 15 || c2 < 0)
				|| ((c3 = ScannerHelper.getNumericValue(this.source[this.index++])) > 15 || c3 < 0)
				|| ((c4 = ScannerHelper.getNumericValue(this.source[this.index++])) > 15 || c4 < 0))) {
			first = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
		} else {
			this.index = pos;
		}
	}

	// switch on first tag char
	switch (first) {
		case 'd': // perhaps @deprecated tag?
	        if ((readChar() == 'e') &&
					(readChar() == 'p') && (readChar() == 'r') &&
					(readChar() == 'e') && (readChar() == 'c') &&
					(readChar() == 'a') && (readChar() == 't') &&
					(readChar() == 'e') && (readChar() == 'd')) {
				// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
				char c = readChar();
				if (ScannerHelper.isWhitespace(c) || c == '*') {
					this.tagValue = TAG_DEPRECATED_VALUE;
					this.deprecated = true;
				}
	        }
			break;
	}
}

}
