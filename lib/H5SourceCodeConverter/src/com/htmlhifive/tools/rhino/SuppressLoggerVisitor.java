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

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ログ出力を行っているコードを削除する.
 */
public class SuppressLoggerVisitor implements NodeVisitor {

	private static Logger logger = LoggerFactory.getLogger(SuppressLoggerVisitor.class);

	/** 削除対象ロガー名. */
	public static final String FW_LOGGER_NAME = "fwLogger";

	/** 生成したロガーの名前. */
	private Set<String> loggerNames = new HashSet<String>();

	/** 出力ストリーム. */
	// private PrintStream out;

	/** コンストラクタ. */
	public SuppressLoggerVisitor() {

		// this(System.out);
	}

	/** コンストラクタ. */
	// public SuppressLoggerVisitor(PrintStream systemOut) {
	//
	// // out = systemOut;
	// }

	@Override
	public boolean visit(AstNode node) {

		if (node instanceof FunctionCall) {
			FunctionCall an = (FunctionCall) node;
			AstNode target = an.getTarget();

			String id = getId(target);

			if (id.endsWith("createLogger")) {
				String loggerName = removeLoggerInitialization(node);
				if (null != loggerName) {
					loggerNames.add(loggerName);
				}
			}
			if (useLogger(id)) {
				removeNode(node);
			}
		}
		return true;
	}

	public boolean useLogger(String id) {

		if (null == id) {
			return false;
		}
		for (String loggerName : loggerNames) {
			if (id.startsWith(loggerName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ロガーの初期化コードを削除する.
	 *
	 * @param target
	 * @return
	 */
	public String removeLoggerInitialization(AstNode target) {

		AstNode parent = target.getParent();
		if (parent instanceof VariableInitializer) {
			VariableInitializer varInit = (VariableInitializer) parent;
			AstNode grandParent = varInit.getParent();
			if (!(grandParent instanceof VariableDeclaration)) {
				throw new IllegalStateException("VariableInitializer is not define in VariableDeclaration.");
			}
			String id = getId(varInit.getTarget());
			if (id.endsWith(FW_LOGGER_NAME)) {
				VariableDeclaration varDecl = (VariableDeclaration) grandParent;
				int size = varDecl.getVariables().size();
				if (size > 1) {
					varDecl.removeChild(varInit); // TODO ","とかインデントとかもセットで消さないといかん筈.
					logger.trace(SourceMaker.toSource(varInit));
					// out.println(SourceMaker.toSource(varInit));
				} else {
					removeNode(varDecl);
				}
			}
			return id;
		} else if (parent instanceof Assignment) {
			Assignment assignment = (Assignment) parent;
			AstNode left = assignment.getLeft();
			String id = getId(left);
			if (id.endsWith(FW_LOGGER_NAME)) {
				removeNode(parent);
			}
			return id;

		} else {
			logger.trace("[" + parent.getLineno() + "] return statement is not assigned !");
			// out.println("[" + parent.getLineno() + "] return statement is not assigned !");
			return null;
		}
	}

	/**
	 * 対象ノードを削除する.
	 *
	 * @param node
	 */
	public void removeNode(AstNode node) {

		AstNode parent = node.getParent();
		AstNode target = node;
		if (parent instanceof ExpressionStatement) {
			target = parent;
			parent = parent.getParent();
		}
		parent.removeChild(target);
		logger.debug("[lno:" + target.getLineno() + "] " + SourceMaker.toSource(target).trim());
		// out.println("[lno:" + target.getLineno() + "] " + SourceMaker.toSource(target).trim());
	}

	/**
	 * ノードのId情報を取得する.
	 *
	 * @param node
	 * @return
	 */
	public String getId(AstNode node) {

		int type = node.getType();
		switch (type) {
			case Token.GETPROP:
				PropertyGet pg = (PropertyGet) node;
				AstNode left = pg.getLeft();
				AstNode right = pg.getRight();
				return (getId(left) + "." + getId(right));
			case Token.NAME:
				return ((Name) node).getIdentifier();
			case Token.FUNCTION:
				return ((FunctionNode) node).getName();
			case Token.LP:
				return node.getClass().toString();
			default:
				return "[" + Util.typeToName(node) + "]";
		}
	}
}
