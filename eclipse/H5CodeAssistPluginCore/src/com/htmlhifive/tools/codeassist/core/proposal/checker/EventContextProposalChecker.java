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
package com.htmlhifive.tools.codeassist.core.proposal.checker;


import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IArgument;
import org.eclipse.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;

import com.htmlhifive.tools.codeassist.core.config.bean.EventContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.RootChildrenElem;
import com.htmlhifive.tools.codeassist.core.proposal.build.CodeBuilderType;
import com.htmlhifive.tools.codeassist.core.proposal.collector.MemberAccessVisitor;
import com.htmlhifive.tools.codeassist.core.proposal.collector.NodeCollector;
import com.htmlhifive.tools.codeassist.core.proposal.collector.NodeCollectorFactory;

/**
 * Eventcontext補完用のチェッカクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public class EventContextProposalChecker extends AbstractObjectProposalChecker {

	/**
	 * イベントコンテキストビーン.
	 */
	private EventContextBean bean;

	/**
	 * コードアシスト時の文字列.
	 */
	private String codeAssistStr;

	/**
	 * コード補完ノード.
	 */
	private CompletionOnMemberAccess memberAccess;

	/**
	 * コンストラクタ.
	 * 
	 * @param unit ユニット.
	 * @param project プロジェクト.
	 * @param bean イベントコンテキストビーン.
	 * @throws JavaScriptModelException 生成例外.
	 */
	public EventContextProposalChecker(IJavaScriptUnit unit, IJavaScriptProject project, EventContextBean bean)
			throws JavaScriptModelException {

		super(unit, project);
		this.bean = bean;
	}

	@Override
	public String getCodeAssistStr() {

		return codeAssistStr;
	}

	@Override
	protected boolean doCheckCodeAssist(CompilationUnitDeclaration unitDeclaration) {

		NodeCollector collector = NodeCollectorFactory.createNodeCollector(getBean());
		collector.collect(unitDeclaration);
		IASTNode[] eventHandlerNodes = collector.getNodes();
		String eventContextName = null;
		for (IASTNode eventHandler : eventHandlerNodes) {
			MemberAccessVisitor visitor = new MemberAccessVisitor();
			eventHandler.traverse(visitor);
			if (visitor.getMemberAccess() != null) {
				memberAccess = visitor.getMemberAccess();
				IFunctionDeclaration funcDeclaration = (IFunctionDeclaration) eventHandler;
				IArgument arg = funcDeclaration.getArguments()[0];
				// jsdocで定義されている場合は補完を出さない
				if (arg.getInferredType() != null) {
					return false;
				}
				eventContextName = String.valueOf(arg.getName());
			}
		}
		if (memberAccess == null || eventContextName == null) {
			return false;
		}
		codeAssistStr = memberAccess.getReceiver().toString();
		String firstSegMemberAccess = StringUtils.substringBefore(codeAssistStr, ".");
		if (StringUtils.equals(firstSegMemberAccess, eventContextName)) {
			this.addDummyCodeInfoList(new DelegateDummyCodeInfo(memberAccess.sourceEnd + 1, eventContextName,
					CodeBuilderType.CREATE_OBJ));
			return true;
		}
		return false;
	}

	@Override
	protected RootChildrenElem getBean() {

		return bean;
	}

	@Override
	public boolean existDefaultCodeAssist() {

		return false;
	}

	@Override
	CompletionOnMemberAccess getMemberAccess() {

		return memberAccess;
	}

}
