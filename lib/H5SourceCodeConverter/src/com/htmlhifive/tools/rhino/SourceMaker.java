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

package com.htmlhifive.tools.rhino;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.EmptyExpression;
import org.mozilla.javascript.ast.Word;

/**
 * 与えられたノードの文字列表現を返す.
 */
public class SourceMaker {

	/**
	 *
	 * @param node
	 * @return
	 */
	public static String toSource(Node node) {

		if (node.hasChildren()) {
			StringBuilder sb = new StringBuilder();
			Node child = node.getFirstChild();
			while (null != child) {
				sb.append(toSource(child));
				child = child.getNext();
			}
			String result = sb.toString();
			if (null != node.getProp(Constants.TRIM)) {
				result = result.trim();
			}
			return result;
		} else {
			if (node instanceof EmptyExpression) {
				return "";
			} else if (node instanceof Word) {
				return node.toString();
			}
			throw new IllegalArgumentException(Token.typeToName(node.getType()));
		}
	}
}
