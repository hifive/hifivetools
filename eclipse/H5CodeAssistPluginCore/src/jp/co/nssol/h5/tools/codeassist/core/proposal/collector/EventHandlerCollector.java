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
package jp.co.nssol.h5.tools.codeassist.core.proposal.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.wst.jsdt.core.ast.IFunctionExpression;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.core.ast.IScriptFileDeclaration;
import org.eclipse.wst.jsdt.core.ast.IStatement;
import org.eclipse.wst.jsdt.core.ast.IStringLiteral;

/**
 * イベントハンドラを収集するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class EventHandlerCollector implements NodeCollector {

	/**
	 * イベントハンドラノード.
	 */
	private List<IFunctionDeclaration> eventHandlerList;

	/**
	 * コントローラの正規表現パターン.
	 */
	private Pattern controllerPattern;

	/**
	 * コンストラクタ.
	 * 
	 * @param controllerPattern コントローラの正規表現パターン.
	 */
	EventHandlerCollector(Pattern controllerPattern) {

		this.controllerPattern = controllerPattern;
		this.eventHandlerList = new ArrayList<IFunctionDeclaration>();
	}

	@Override
	public void collect(IScriptFileDeclaration node) {

		NodeCollector collector = new ControllerObjectCollector(controllerPattern);
		collector.collect(node);
		IStatement[] controllerNodes = (IStatement[]) collector.getNodes();
		for (IStatement controllerNode : controllerNodes) {
			controllerNode.traverse(new ASTVisitor() {
				@Override
				public boolean visit(IObjectLiteralField field) {

					if (supportField(field)) {
						IFunctionDeclaration functionDec = ((IFunctionExpression) field.getInitializer())
								.getMethodDeclaration();
						eventHandlerList.add(functionDec);
					}
					return super.visit(field);
				}

			});
		}
	}

	@Override
	public IStatement[] getNodes() {

		return (IFunctionDeclaration[]) eventHandlerList.toArray(new IFunctionDeclaration[eventHandlerList.size()]);
	}

	/**
	 * イベントハンドラとしてみなすフィールド名かどうかを判定する.
	 * 
	 * @param field フィールドノード
	 * @return イベントハンドラフィールドかどうか
	 */
	protected boolean supportField(IObjectLiteralField field) {

		// 判定内容
		// 1.ストリングリテラルかどうか
		// 2.ストリング表記でスペースを含んでいるかどうか('{rootElement} click' : function()...)
		// 3.フィールドのイニシャライザが関数かどうか
		// 4.関数宣言がnull出ないかどうか
		// 5.関数の引数があるかどうか
		IExpression fieldName = field.getFieldName();
		if (!(fieldName instanceof IStringLiteral)) {
			return false;
		}
		if (!StringUtils.contains(String.valueOf(((IStringLiteral) fieldName).source()), " ")) {
			return false;
		}
		if (!(field.getInitializer() instanceof IFunctionExpression)) {
			return false;
		}
		IFunctionDeclaration functionDec = ((IFunctionExpression) field.getInitializer()).getMethodDeclaration();
		return functionDec != null && functionDec.getArguments() != null && functionDec.getArguments().length > 0;
	}

}
