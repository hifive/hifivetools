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
package jp.co.nssol.h5.tools.codeassist.core.exception;

/**
 * コード補完生成時の例外.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ProposalCreateException extends Exception {

	/**
	 * シリアルID.
	 */
	private static final long serialVersionUID = 1974363498307912975L;

	/**
	 * コンストラクタ.
	 * 
	 * @param message エラーメッセージ
	 */
	public ProposalCreateException(String message) {

		super(message);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param e 原因例外.
	 */
	public ProposalCreateException(Throwable e) {

		super(e);
	}

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param message エラーメッセージ.
	 * @param e 原因例外.
	 */
	public ProposalCreateException(String message, Throwable e) {

		super(message, e);
	}

}
