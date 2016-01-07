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
package com.htmlhifive.tools.codeassist.core.proposal.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.ILocalDeclaration;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.core.ast.IScriptFileDeclaration;
import org.eclipse.wst.jsdt.core.ast.IStatement;

/**
 * コントローラノードを収集する.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ControllerObjectCollector implements NodeCollector {

	/**
	 * 正規表現にマッチしたノードリスト.
	 */
	private List<IStatement> controllerNodeList;

	/**
	 * 検索する正規表現パターン.
	 */
	private Pattern regExPatterns;

	/**
	 * コンストラクタ.
	 * 
	 * @param controllerPattern コントローラの正規表現パターン.
	 */
	ControllerObjectCollector(Pattern controllerPattern) {

		this.regExPatterns = controllerPattern;
		this.controllerNodeList = new ArrayList<IStatement>();
	}

	@Override
	public void collect(IScriptFileDeclaration node) {

		final List<IStatement> controllerLocalDeclarationList = new ArrayList<IStatement>();
		node.traverse(new ASTVisitor() {
			@Override
			public boolean visit(ILocalDeclaration localDeclaration) {

				addMathchesNode(controllerLocalDeclarationList, String.valueOf(localDeclaration.getName()),
						localDeclaration);
				return super.visit(localDeclaration);
			}

		});
		final List<IStatement> controllerObjectFieldList = new ArrayList<IStatement>();
		for (IStatement localDeclaration : controllerLocalDeclarationList) {
			localDeclaration.traverse(new ASTVisitor() {
				@Override
				public boolean visit(IObjectLiteralField field) {

					addMathchesNode(controllerObjectFieldList, field.getFieldName().toString(), field);
					return super.visit(field);
				}
			});
		}
		controllerNodeList.addAll(controllerLocalDeclarationList);
		controllerNodeList.addAll(controllerObjectFieldList);
	}

	/**
	 * ノードの名前が正規表現にマッチしたら指定したリストに追加する.
	 * 
	 * @param controllerLocalDeclarationList 追加されるリスト.
	 * @param checkName チェック名.
	 * @param statement ノード.
	 */
	private void addMathchesNode(List<IStatement> controllerLocalDeclarationList, String checkName, IStatement statement) {

		if (regExPatterns.matcher(checkName).matches()) {
			controllerLocalDeclarationList.add(statement);
		}
	}

	@Override
	public IStatement[] getNodes() {

		return (IStatement[]) controllerNodeList.toArray(new IStatement[controllerNodeList.size()]);
	}
}
