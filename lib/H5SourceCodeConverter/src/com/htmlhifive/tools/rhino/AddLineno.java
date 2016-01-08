/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
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
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Word;

/**
 * Wordノードに対し、行番号をを設定する(整形前位置).
 */
public class AddLineno {

	/**
	 *
	 * @param node
	 */
	public static void setup(AstNode node) {

		AstNode prev = null;
		AstNode child = (AstNode) node.getFirstChild();
		while (null != child) {
			if (child instanceof Word) {
				int lineno = node.getLineno();
				Node last = Util.getLast(prev);
				if (null != last) {
					lineno = last.getLineno();
					if (last instanceof Word) {
						Word lastW = (Word) last;
						String value = lastW.getValue();
						Node parent = lastW.getParent();
						if (parent.getLastChild() == lastW) {
							Boolean trim = (Boolean) parent.getProp(Constants.TRIM);
							if (null != trim) {
								value = value.trim();
							}
						}
						int count = Util.count(value, '\n');
						lineno += count;
					}
				}
				child.setLineno(lineno);
			}
			setup(child);
			prev = child;
			child = (AstNode) child.getNext();
		}
	}
}
