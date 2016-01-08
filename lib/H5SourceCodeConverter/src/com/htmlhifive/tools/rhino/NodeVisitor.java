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

import org.mozilla.javascript.ast.AstNode;

/**
 * ツリー構造を深さ優先で辿るクラス.
 */
public class NodeVisitor {

	/** ルート. */
	private AstNode root;

	/** 現在位置. */
	private AstNode cursor;

	/**
	 * コンストラクタ.
	 *
	 * @param root
	 */
	public NodeVisitor(AstNode root) {
		this.root = root;
		this.cursor = (AstNode) root.getFirstChild();
	}

	/**
	 * 次のノードを返す.
	 *
	 * @return
	 */
	public AstNode next() {
		if (null == cursor) {
			return cursor;
		}
		AstNode result = cursor;
		AstNode next = (AstNode) cursor.getFirstChild();
		if (null == next) {
			next = (AstNode) cursor.getNext();
			AstNode parent = cursor.getParent();
			while (null == next) {
				next = (AstNode) parent.getNext();
				parent = (AstNode) parent.getParent();
				if (parent == root) {
					break;
				}
			}
		}
		cursor = next;
		return result;
	}
}
