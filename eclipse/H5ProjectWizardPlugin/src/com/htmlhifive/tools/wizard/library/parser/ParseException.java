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
 */
package com.htmlhifive.tools.wizard.library.parser;

/**
 * xmlの解析に失敗例外.
 * 
 * @author fkubo
 */
public class ParseException extends Exception {

	/**
	 * シリアルID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ.
	 * 
	 * @param message エラーメッセージ.
	 */
	public ParseException(String message) {

		super(message);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param e 原因例外.
	 */
	public ParseException(Throwable e) {

		super(e);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param message エラーメッセージ.
	 * @param e 原因例外.
	 */
	public ParseException(String message, Throwable e) {

		super(message, e);
	}

}
