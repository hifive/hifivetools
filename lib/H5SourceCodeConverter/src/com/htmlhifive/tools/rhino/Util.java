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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.Word;

/**
 * ユーティリティ.
 */
public class Util {

	/** ファイル出力時の文字コード. */
	public static final String ENCODE = "UTF-8";

	/** インデント文字列 */
	public static final String INDENT = "  ";

	/**
	 * インデント文字列を生成する.
	 *
	 * @param indent
	 * @return
	 */
	public static String makeIndent(int indent) {

		if (0 == indent) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append(INDENT);
		}
		return sb.toString();
	}

	/**
	 * 兄弟として、1つ前のノードを返す.
	 *
	 * @param node
	 * @return
	 */
	public static AstNode getPrev(AstNode node) {

		AstNode parent = node.getParent();
		if (null == parent) {
			return null;
		}
		Node prev = null;
		Node child = parent.getFirstChild();
		while (null != child) {
			if (child == node) {
				return (AstNode) prev;
			}
			prev = child;
			child = child.getNext();
		}
		return null;
	}

	/**
	 * 子ノード中、木構造上最初のノードを返す.
	 *
	 * @param node
	 * @return
	 */
	public static AstNode getFirst(AstNode node) {

		if (null == node) {
			return null;
		}
		AstNode first = (AstNode) node.getFirstChild();
		if (null == first) {
			return node;
		} else {
			return getFirst(first);
		}
	}

	/**
	 * 子ノード中、木構造上最後のノードを返す.
	 *
	 * @param node
	 * @return
	 */
	public static AstNode getLast(AstNode node) {

		if (null == node) {
			return null;
		}
		AstNode last = (AstNode) node.getLastChild();
		if (null == last) {
			return node;
		} else {
			return getLast(last);
		}
	}

	/**
	 * 1つ前に追加する.
	 *
	 * @param newNode
	 * @param node
	 */
	public static void addBefore(AstNode newNode, AstNode node) {

		AstNode parent = node.getParent();
		if (null == parent) {
			return;
		}

		Node prev = null;
		Node child = parent.getFirstChild();
		while (null != child) {
			if (child == node) {
				if (null != prev) {
					parent.addChildAfter(newNode, prev);
				} else {
					parent.addChildrenToFront(newNode);
				}
				newNode.setParent(parent);
				return;
			}
			prev = child;
			child = child.getNext();
		}
	}

	/**
	 * ツリー中のノード情報を表示する.
	 *
	 * @param node
	 * @return
	 */
	public static String printTree(AstNode node) {

		StringBuilder sb = new StringBuilder(printNode(node));

		Node child = node.getFirstChild();
		while (null != child) {
			sb.append(printTree((AstNode) child));
			child = child.getNext();
		}
		return sb.toString();
	}

	public static String printNode(AstNode node) {

		StringBuilder sb = new StringBuilder();
		// int depth = getPropValue(node, Constants.DEPTH);
		int depth = node.depth();
		String indent = makeIndent(depth);
		sb.append(indent);
		String name = typeToName(node);

		sb.append(name);
		sb.append("(lno:");
		sb.append(node.getLineno());
		sb.append(", abp:");
		sb.append(node.getAbsolutePosition());
		sb.append(", pos:");
		sb.append(node.getPosition());
		sb.append(", len:");
		sb.append(node.getLength());
		sb.append(")");

		int tt = node.getType();
		if (tt == Token.NAME) {
			String identifier = ((Name) node).getIdentifier();
			sb.append(identifier);
		} else if (node instanceof Word || node instanceof Comment) {
			String value = null;
			if (node instanceof Word) {
				value = ((Word) node).getValue();
			} else {
				value = ((Comment) node).getValue();
			}
			if (null != value) {
				value = value.replaceAll("\n", "\\\\n");
				value = value.replaceAll("\r", "\\\\r");
				value = value.substring(0, Math.min(30, value.length()));
				sb.append("\"" + value + "\"");
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	/**
	 *
	 * @param node
	 * @return
	 */
	public static String typeToName(Node node) {

		if (node instanceof Word) {
			return "WORD";
		}
		int type = node.getType();
		String name = Token.typeToName(type);
		return name;
	}

	/**
	 * ファイルにjson情報を格納する.
	 *
	 * @param g
	 * @param file
	 * @throws IOException
	 */
	public static void dumpToFile(AstNode node, File file) throws IOException {

		if (file.getParentFile().mkdirs()) {
			System.out.println("mkdirs" + file.getParentFile().getAbsolutePath());
		}
		String src = SourceMaker.toSource(node);
		FileUtils.write(file, src, ENCODE, false);
	}

	/**
	 * ノードのプロパティ値を返す.
	 *
	 * @param node
	 * @param prop
	 * @return
	 */
	public static int getPropValue(Node node, int prop) {

		if (null == node) {
			return 0;
		}
		Integer propObj = (Integer) node.getProp(prop);
		if (null != propObj) {
			return propObj.intValue();
		}
		return 0;
	}

	/**
	 * 文字列中、指定された文字の数を返す.
	 *
	 * @param word
	 * @param ch
	 * @return
	 */
	public static int count(String word, char ch) {

		int count = 0;

		for (int i = 0; i < word.length(); i++) {
			if (word.charAt(i) == ch) {
				count++;
			}
		}
		return count;
	}

	/**
	 * ノードを追加する.文字列追加
	 *
	 * @param an
	 * @param pos
	 * @param str
	 * @return
	 */
	public static int addChild(AstNode an, int pos, String str) {

		if (null == str) {
			return pos;
		}
		Word child = new Word(pos, str);
		return addChild(an, child, 0, 0);
	}

	/**
	 * ノートを追加する。子ノード追加
	 *
	 * @param an
	 * @param child
	 * @param depth
	 * @return
	 */
	public static int addChild(AstNode an, AstNode child, int depth) {

		return addChild(an, child, depth, 0);
	}

	/**
	 * ノードを追加する.子ノード追加
	 *
	 * @param an
	 * @param child
	 * @param depth
	 * @param indent
	 * @return
	 */
	public static int addChild(AstNode an, AstNode child, int depth, int indent) {

		return addChild(an, child, depth, indent, false);
	}

	/**
	 * ノードを追加する.子ノード追加
	 *
	 * @param an
	 * @param child
	 * @param depth
	 * @param indent
	 * @param trim
	 * @return
	 */
	public static int addChild(AstNode an, AstNode child, int depth, int indent, boolean trim) {

		// an.addChild(child);
		an.addChildToBack(child);
		int positionBackup = child.getPosition();
		child.setParent(an);
		child.setPosition(positionBackup);

		child.putProp(Constants.DEPTH, depth);
		child.putProp(Constants.INDENT, indent);
		if (trim) {
			child.putProp(Constants.TRIM, trim);
		}
		return child.getPosition() + child.getLength();
	}

	/**
	 * ノードを追加する.
	 *
	 * @param an
	 * @param pos
	 * @param children
	 * @param depth
	 * @return
	 */
	public static <T> int addChildren(AstNode an, int pos, List<T> children, int depth) {

		return addChildren(an, pos, children, depth, false);
	}

	/**
	 * 複数のノードを追加する.
	 *
	 * @param an
	 * @param pos
	 * @param children
	 * @param depth
	 * @param ln
	 * @return
	 */
	public static <T> int addChildren(AstNode an, int pos, List<T> children, int depth, boolean ln) {

		if (null == children || 0 == children.size()) {
			return pos;
		}
		int i = 0;
		for (T childT : children) {
			AstNode child = (AstNode) childT;
			if (i++ != 0) {
				if (ln) {
					pos = addChild(an, pos, ",");
				} else {
					pos = addChild(an, pos, ", ");
				}
			}
			if (ln) {
				pos = addChild(an, pos, "\n");
				int newDepth = depth + 1;
				pos = addChild(an, child, newDepth, newDepth);
			} else {
				pos = addChild(an, child, depth);
			}
		}
		if (ln) {
			pos = addChild(an, pos, "\n");
			pos = addChild(an, pos, Util.makeIndent(depth));
		}
		return pos;
	}

	/**
	 * 複数のver句を追加する.
	 *
	 * @param an
	 * @param pos
	 * @param children
	 * @param depth
	 * @param ln
	 * @return
	 */
	public static <T> int addChildrenForVariables(AstNode an, int pos, List<T> children, int depth, boolean ln) {

		if (null == children || 0 == children.size()) {
			return pos;
		} else if (1 == children.size()) {
			return addChild(an, (AstNode) children.get(0), depth);
		} else {
			int i = 0;
			int newDepth = depth + 1;
			for (T childT : children) {
				AstNode child = (AstNode) childT;
				if (i != 0) {
					if (ln) {
						pos = addChild(an, pos, ",");
					} else {
						pos = addChild(an, pos, ", ");
					}
				}
				if (ln) {
					if (i == 0) {
						pos = addChild(an, child, newDepth, 0);
					} else {
						pos = addChild(an, pos, "\n");
						pos = addChild(an, child, newDepth, newDepth);
					}
				} else {
					pos = addChild(an, child, depth);
				}
				i++;
			}
			return pos;
		}
	}

	public static boolean isVSDoc(String str) {

		return StringUtils.startsWith(str, "///");
	}
}
