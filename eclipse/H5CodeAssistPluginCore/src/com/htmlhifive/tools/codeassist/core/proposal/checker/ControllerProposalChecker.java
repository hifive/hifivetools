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


import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;

import com.htmlhifive.tools.codeassist.core.config.bean.ControllerBean;

/**
 * Controllerのコード補完の必要があるかどうかチェックするチェッカークラス.<br>
 * コード補完の必要があった場合はダミーコードを生成するために必要な情報を保持する.
 * 
 * @see DummyCodeInfo
 * @see DelegateDummyCodeInfo
 * @author NS Solutions Corporation
 * 
 */
public class ControllerProposalChecker extends AbstractSuffixProposalChecker {

	/**
	 * コンストラクタ.
	 * 
	 * @param unit ユニット.
	 * @param project プロジェクト.
	 * @param bean コントローラビーン.
	 * @throws JavaScriptModelException 生成例外.
	 */
	public ControllerProposalChecker(IJavaScriptUnit unit, IJavaScriptProject project, ControllerBean bean)
			throws JavaScriptModelException {

		super(unit, project, bean);
	}

	@Override
	protected ControllerBean getBean() {

		return (ControllerBean) super.getBean();
	}

	@Override
	public boolean existDefaultCodeAssist() {

		if (!StringUtils.contains(getCodeAssistStr(), '.')) {
			return true;
		}
		String str = StringUtils.substringAfterLast(getCodeAssistStr(), ".");
		return getBean().getRegExPattern().matcher(str).matches();
	}

}
