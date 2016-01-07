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
package com.htmlhifive.tools.jslint;

/**
 * 
 * JSLintPluginで使用するの定数クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class JSLintPluginConstant {

	/**
	 * マーカーID.
	 */
	public static final String JS_TYPE_MARKER = "com.htmlhifive.tools.jslint.H5JSLintPlugin.javascriptMarker";

	/**
	 * オプション設定ファイルの拡張子.
	 */
	public static final String EXTENTION_OPTION = "xml";

	/**
	 * JavaScriptファイルの拡張子.
	 */
	public static final String EXTENTION_JS = "js";

	/**
	 * jsLintを使用するときのファイル名.
	 */
	public static final String JS_LINT_NAME = "jslint.js";

	/**
	 * jsHintを使用するときのファイル名.
	 */
	public static final String JS_HINT_NAME = "jshint.js";

	/**
	 * jsLint使用時のメソッド名.
	 */
	public static final String JS_LINT_METHOD = "JSLINT";

	/**
	 * jsHint使用時の起動メソッド名.
	 */
	public static final String JS_HINT_METHOD = "JSHINT";

	/**
	 * オプション区切り文字.
	 */
	public static final String OPTION_SEPARATOR = ",";

	/**
	 * コンストラクタ.
	 */
	private JSLintPluginConstant() {

		// nocreate
	}

}
