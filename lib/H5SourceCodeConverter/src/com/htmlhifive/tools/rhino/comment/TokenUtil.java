/*
 * Copyright (C) 2012 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.htmlhifive.tools.rhino.comment;

import java.util.regex.Pattern;


import org.apache.commons.lang.ArrayUtils;

import com.htmlhifive.tools.rhino.comment.js.JSTag;

public final class TokenUtil {

	private static final String JSDOC_START = "/**";
	private static final String JSDOC_END = "*/";
	private static final String[] SYMBOLS = { "+", "-", "*", "/", "(", ")", "{", "}", };
	private static Pattern HTMLTAG_PATTERN = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");

	public static TokenType resolveType(String str) {

		if (str == null) {
			return null;
		} else if (str.startsWith("{") && str.endsWith("}")) {
			return TokenType.TYPE;
		} else if (str.startsWith("@")) {
			return TokenType.ANNOTATION;
		} else if (str.equals(JSDOC_START)) {
			return TokenType.START;
		} else if (str.equals(JSDOC_END)) {
			return TokenType.END;
		} else if (str.length() == 1 && ArrayUtils.contains(SYMBOLS, str)) {
			return TokenType.SYMBOL;
		}
		return TokenType.STRING_LITERAL;
	}

	public static JSTag resolveTagType(Token token) {

		if (token == null || token.getType() != TokenType.ANNOTATION) {
			return null;
		}
		String str = token.getValue();
		if (str.equals("@augments")) {
			return JSTag.AUGMENTS;
		} else if (str.equals("@author")) {
			return JSTag.AUTHOR;
		} else if (str.equals("@argument")) {
			return JSTag.ARGUMENT;
		} else if (str.equals("@borrows")) {
			return JSTag.BORROWS;
		} else if (str.equals("@class")) {
			return JSTag.CLASS;
		} else if (str.equals("@constant") || str.equals("@const")) {
			return JSTag.CONSTANT;
		} else if (str.equals("@constructor")) {
			return JSTag.CONSTRUCTOR;
		} else if (str.equals("@constructs")) {
			return JSTag.CONSTRUCTS;
		} else if (str.equals("@default")) {
			return JSTag.DEFAULT;
		} else if (str.equals("@deprecated")) {
			return JSTag.DEPRECATED;
		} else if (str.equals("@description")) {
			return JSTag.DESCRIPTION;
		} else if (str.equals("@event")) {
			return JSTag.EVENT;
		} else if (str.equals("@example")) {
			return JSTag.EXAMPLE;
		} else if (str.equals("@exports")) {
			return JSTag.EXPORTS;
		} else if (str.equals("@extends")) {
			return JSTag.EXTENDS;
		} else if (str.equals("@field")) {
			return JSTag.FIELD;
		} else if (str.equals("@fieldOf")) {
			return JSTag.FIELDOF;
		} else if (str.equals("@fileOverview")) {
			return JSTag.FILEOVERVIEW;
		} else if (str.equals("@function")) {
			return JSTag.FUNCTION;
		} else if (str.equals("@ignore")) {
			return JSTag.IGNORE;
		} else if (str.equals("@inner")) {
			return JSTag.INNER;
		} else if (str.equals("@lends")) {
			return JSTag.LENDS;
		} else if (str.equals("@link")) {
			return JSTag.LINK;
		} else if (str.equals("@memberOf")) {
			return JSTag.MEMBEROF;
		} else if (str.equals("@methodOf")) {
			return JSTag.METHODOF;
		} else if (str.equals("@name")) {
			return JSTag.NAME;
		} else if (str.equals("@namespace")) {
			return JSTag.NAMESPACE;
		} else if (str.equals("@param")) {
			return JSTag.PARAM;
		} else if (str.equals("@private")) {
			return JSTag.PRIVATE;
		} else if (str.equals("@property")) {
			return JSTag.PROPERTY;
		} else if (str.equals("@public")) {
			return JSTag.PUBLIC;
		} else if (str.equals("@requires")) {
			return JSTag.REQUIRES;
		} else if (str.equals("@returns") || str.equals("@return")) {
			return JSTag.RETURNS;
		} else if (str.equals("@see")) {
			return JSTag.SEE;
		} else if (str.equals("@since")) {
			return JSTag.SINCE;
		} else if (str.equals("@static")) {
			return JSTag.STATIC;
		} else if (str.equals("@throws")) {
			return JSTag.THROWS;
		} else if (str.equals("@type")) {
			return JSTag.TYPE;
		} else if (str.equals("@version")) {
			return JSTag.VERSION;
		} else if (str.equals("@todo") || str.equals("@member") || str.equals("@params")) {
			return null;
		}
		throw new IllegalArgumentException(str);
	}

	private TokenUtil() {

	}

	public static boolean isSymbolType(TokenType type) {

		if (type == TokenType.START || type == TokenType.END || type == TokenType.SYMBOL) {
			return true;
		}
		return false;
	}

	public static String escapeHtml(String str) {

		StringBuilder sb = new StringBuilder();
		char[] charArray = str.toCharArray();
		for (char c : charArray) {
			switch (c) {
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&apos;");
					break;

				default:
					sb.append(c);
					break;
			}
		}

		return sb.toString();
	}

	public static String removeHtmlTag(String nextToken) {

		return HTMLTAG_PATTERN.matcher(nextToken).replaceAll("");
	}
}
