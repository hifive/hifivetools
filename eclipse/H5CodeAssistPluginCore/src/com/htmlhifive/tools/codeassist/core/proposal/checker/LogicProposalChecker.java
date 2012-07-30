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

import java.util.regex.Pattern;


import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;

import com.htmlhifive.tools.codeassist.core.config.bean.LogicBean;
import com.htmlhifive.tools.codeassist.core.config.bean.ObjectLiteralBean;

/**
 * ロジック補完用のチェッカクラス.
 * 
 * @see DummyCodeInfo
 * @see DelegateDummyCodeInfo
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public class LogicProposalChecker extends AbstractSuffixProposalChecker {

	/**
	 * コンストラクタ.
	 * 
	 * @param unit ユニット.
	 * @param project プロジェクト.
	 * @param bean ロジックビーン.
	 * @throws JavaScriptModelException 生成例外.
	 */
	public LogicProposalChecker(IJavaScriptUnit unit, IJavaScriptProject project, LogicBean bean)
			throws JavaScriptModelException {

		super(unit, project, bean);
	}

	@Override
	boolean suffixCheckCodeAssist(CompilationUnitDeclaration unitDeclaration, ObjectLiteralBean suffixBean,
			Pattern suffixPattern) {

		boolean result = super.suffixCheckCodeAssist(unitDeclaration, getBean(), getBean().getRegExPattern());
		// コントローラの補完チェック.
		if (result && getBean().getRegExControllerPattern().matcher(getCodeAssistStr()).matches()) {
			return false;
		}
		return result;
	}

	@Override
	protected LogicBean getBean() {

		return (LogicBean) super.getBean();
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
