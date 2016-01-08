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
package com.htmlhifive.tools.codeassist.core.exception;

/**
 * コード補完の必要性チェック時の例外.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ProposalCheckException extends Exception {

	/**
	 * シリアルID.
	 */
	private static final long serialVersionUID = -8719751719219180651L;

	/**
	 * コンストラクタ.
	 * 
	 * @param message エラーメッセージ
	 */
	public ProposalCheckException(String message) {

		super(message);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param e 原因例外.
	 */
	public ProposalCheckException(Throwable e) {

		super(e);
	}

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param message エラーメッセージ.
	 * @param e 原因例外.
	 */
	public ProposalCheckException(String message, Throwable e) {

		super(message, e);
	}

}
