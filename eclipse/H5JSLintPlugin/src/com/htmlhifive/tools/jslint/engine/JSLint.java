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
package com.htmlhifive.tools.jslint.engine;

import java.io.Reader;

import org.eclipse.core.runtime.CoreException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

import com.htmlhifive.tools.jslint.JSLintPluginConstant;
import com.htmlhifive.tools.jslint.engine.option.CheckOption;

/**
 * JSLintを利用したチェッカクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSLint extends AbstractJSChecker {

	/**
	 * コンストラクタ.
	 * 
	 * @param jslint jslint.jsファイル.
	 * @param options オプション.
	 * @throws CoreException 解析例外
	 */
	public JSLint(Reader jslint, CheckOption[] options) throws CoreException {

		super(jslint, options);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.engine.AbstractJSChecker#getCheckerMethodName
	 * ()
	 */
	@Override
	protected String getCheckerMethodName() {

		return JSLintPluginConstant.JS_LINT_METHOD;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see com.htmlhifive.tool.jslint.engine.AbstractJSChecker#getErrors
	 */
	@Override
	protected NativeArray getErrors() {
		Scriptable jslint = (Scriptable) result;
		return (NativeArray) jslint.get("warnings", jslint);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.engine.AbstractJSChecker#convertToErrorBean
	 */
	@Override
	protected JSCheckerErrorBean convertToErrorBean(Scriptable err) {
		JSCheckerErrorBean e = new JSCheckerErrorBean();
		e.setCharacter(err.get("column", err) == Scriptable.NOT_FOUND ? null
				: Double.valueOf(err.get("column", err).toString()));
		e.setEvidence(err.get("code", err) == Scriptable.NOT_FOUND ? null : err.get("code", err).toString());
		e.setLine(err.get("line", err) == Scriptable.NOT_FOUND ? null
				: Double.valueOf(err.get("line", err).toString()) + 1);
		e.setReason(err.get("message", err) == Scriptable.NOT_FOUND ? null : err.get("message", err).toString());
		return e;
	}
}
