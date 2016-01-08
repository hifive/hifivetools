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
package com.htmlhifive.tools.codeassist.core.proposal.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IAssignment;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.core.ast.IScriptFileDeclaration;
import org.eclipse.wst.jsdt.core.ast.ISingleNameReference;
import org.eclipse.wst.jsdt.core.ast.IStatement;

/**
 * ロジックノードを収集する.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class LogicObjectCollector implements NodeCollector {

	/**
	 * ロジックノードリスト.
	 */
	private List<IStatement> logicNodeList;

	/**
	 * ロジックの正規表現パターン.
	 */
	private Pattern regExPattern;

	/**
	 * コントローラの正規表現パターン.
	 */
	private Pattern regExControlerPattern;

	/**
	 * コンストラクタ.
	 * 
	 * @param regExPattern ロジックの正規表現パターン.
	 * @param regExControllerPattern コントローラの正規表現パターン.
	 */
	LogicObjectCollector(Pattern regExPattern, Pattern regExControllerPattern) {

		this.regExControlerPattern = regExControllerPattern;
		this.regExPattern = regExPattern;
		this.logicNodeList = new ArrayList<IStatement>();
	}

	@Override
	public void collect(IScriptFileDeclaration node) {

		NodeCollector collector = new ControllerObjectCollector(regExPattern);
		collector.collect(node);
		IStatement[] logicNodes = collector.getNodes();
		for (IStatement iStatement : logicNodes) {
			logicNodeList.add(iStatement);
		}
		collector = new ControllerObjectCollector(regExControlerPattern);
		collector.collect(node);
		IStatement[] controllerNodes = collector.getNodes();
		for (IStatement iStatement : controllerNodes) {
			iStatement.traverse(new ASTVisitor() {
				@Override
				public boolean visit(IObjectLiteralField field) {

					if (regExPattern.matcher(field.getFieldName().toString()).matches()) {
						logicNodeList.add(field);
					}
					return super.visit(field);
				}
			});
		}
		// xxxLogic.prototypeのノードを取得する
		node.traverse(new ASTVisitor() {
			@Override
			public boolean visit(IAssignment assignment) {

				// xxxLogic.prototypeの部分を取得
				IExpression expression = assignment.getLeftHandSide();
				if (expression instanceof IFieldReference) {
					// xxxLogicの取得
					IExpression receverName = ((IFieldReference) expression).getReceiver();
					if (receverName instanceof ISingleNameReference) {
						if (regExPattern.matcher(new String(((ISingleNameReference) receverName).getToken())).matches()) {
							logicNodeList.add(assignment);
							return super.visit(assignment);
						}
					}

				}
				return super.visit(assignment);
			}
		});
	}

	@Override
	public IStatement[] getNodes() {

		return (IStatement[]) logicNodeList.toArray(new IStatement[logicNodeList.size()]);
	}

}
