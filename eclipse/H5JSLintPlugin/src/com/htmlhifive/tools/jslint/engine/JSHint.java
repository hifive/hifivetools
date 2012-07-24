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

import com.htmlhifive.tools.jslint.JSLintPluginConstant;
import com.htmlhifive.tools.jslint.engine.option.CheckOption;

/**
 * JSHintを利用したチェッカクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSHint extends AbstractJSChecker {

	/**
	 * コンストラクタ.
	 * 
	 * @param jsHint jshint.jsファイル.
	 * @param options オプション.
	 * @throws CoreException 解析例外
	 */
	public JSHint(Reader jsHint, CheckOption[] options) throws CoreException {

		super(jsHint, options);
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

		return JSLintPluginConstant.JS_HINT_METHOD;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see com.htmlhifive.tool.jslint.engine.AbstractJSChecker#
	 * optionsAsJavaScriptObject()
	 */
	// @Override
	// Scriptable optionsAsJavaScriptObject() {
	//
	// return (Scriptable) getFactory().call(new ContextAction() {
	// public Object run(Context cx) {
	//
	// Scriptable opts = cx.newObject(getScope());
	// for (JSHintOptions option : JSHintOptions.values()) {
	// Object value = null;
	// if (option.getClazz() == Boolean.class) {
	// value = Boolean.valueOf(getProperty().getProperty(option.getKey()));
	// } else if (option.getClazz() == Integer.class) {
	// value =
	// CheckJavaScriptUtils.nullOrEmplty(getProperty().getProperty(option.getKey()))
	// ? null
	// : Integer.valueOf(getProperty().getProperty(option.getKey()));
	// }
	// opts.put(option.getKey(), opts, value);
	// }
	// return opts;
	// }
	//
	// });
	// }
}
