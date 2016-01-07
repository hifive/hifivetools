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
package com.htmlhifive.tools.codeassist.core.proposal.checker;


import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;

import com.htmlhifive.tools.codeassist.core.config.bean.InitializationContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.RootChildrenElem;

/**
 * Initializationcontext補完用のチェッカクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class InitializationContextProposalChecker extends EventContextProposalChecker {

	/**
	 * イベントコンテキストビーン.
	 */
	private InitializationContextBean bean;

	/**
	 * コンストラクタ.
	 * 
	 * @param unit ユニット.
	 * @param project プロジェクト.
	 * @param bean イベントコンテキストビーン.
	 * @throws JavaScriptModelException 生成例外.
	 */
	public InitializationContextProposalChecker(IJavaScriptUnit unit, IJavaScriptProject project,
			InitializationContextBean bean) throws JavaScriptModelException {

		super(unit, project, null);
		this.bean = bean;
	}

	@Override
	protected RootChildrenElem getBean() {

		return bean;
	}

}
