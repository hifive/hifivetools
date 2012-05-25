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
 package jp.co.nssol.h5.tool.jslint.exception;

/**
 * 
 * JSLintプラグイン用の例外クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSLintPluginException extends Exception {
	/**
	 * シリアルID.
	 */
	private static final long serialVersionUID = -7832376461265017886L;

	/**
	 * コンストラクタ.
	 */
	public JSLintPluginException() {

		super();
	}

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param arg0 エラーメッセージ
	 */
	public JSLintPluginException(String arg0) {

		super(arg0);
	}

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param arg0 例外クラス.
	 */
	public JSLintPluginException(Throwable arg0) {

		super(arg0);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param arg0 エラーメッセージ
	 * @param arg1 例外クラス.
	 */
	public JSLintPluginException(String arg0, Throwable arg1) {

		super(arg0, arg1);
	}

}
