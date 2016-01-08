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


import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;

import com.htmlhifive.tools.codeassist.core.config.bean.ControllerBean;
import com.htmlhifive.tools.codeassist.core.config.bean.EventContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.InitializationContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.LogicBean;
import com.htmlhifive.tools.codeassist.core.config.bean.RootChildrenElem;

/**
 * インターナルクラスのラッパーファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class CheckerFactory {
	/**
	 * コンストラクタ.
	 */
	private CheckerFactory() {

	}

	/**
	 * コード補完チェッカクラスを生成する.
	 * 
	 * @param unit コード補完対象ユニット.
	 * @param project プロジェクト.
	 * @param bean オプションファイルで補完するべきオブジェクト.
	 * @return コード補完チェッカクラス
	 * @throws JavaScriptModelException 生成例外.
	 */
	public static ProposalChecker createChecker(IJavaScriptUnit unit, IJavaScriptProject project, RootChildrenElem bean)
			throws JavaScriptModelException {

		if (bean instanceof ControllerBean) {
			return new ControllerProposalChecker(unit, project, (ControllerBean) bean);
		} else if (bean instanceof LogicBean) {
			return new LogicProposalChecker(unit, project, (LogicBean) bean);
		} else if (bean instanceof EventContextBean) {
			return new EventContextProposalChecker(unit, project, (EventContextBean) bean);
		} else if (bean instanceof InitializationContextBean) {
			return new InitializationContextProposalChecker(unit, project, (InitializationContextBean) bean);
		}
		return null;

	}

}
