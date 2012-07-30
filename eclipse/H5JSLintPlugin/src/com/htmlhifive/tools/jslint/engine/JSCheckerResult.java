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

import java.util.ArrayList;
import java.util.List;

/**
 * JSLintの実行結果を保持するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSCheckerResult {

	/**
	 * JSLint実行のエラーを保持するリスト.
	 */
	private List<JSCheckerErrorBean> errorList;

	/**
	 * コンストラクタ.
	 */
	public JSCheckerResult() {

		errorList = new ArrayList<JSCheckerErrorBean>();
	}

	/**
	 * JSLint実行のエラーを保持するクラス.を取得する.
	 * 
	 * @return JSLint実行のエラーを保持するクラス.
	 */
	public JSCheckerErrorBean[] getErrors() {

		return (JSCheckerErrorBean[]) errorList.toArray(new JSCheckerErrorBean[errorList.size()]);
	}

	/**
	 * エラーを追加する.
	 * 
	 * @param error JSLintError;
	 */
	public void addErroList(JSCheckerErrorBean error) {

		this.errorList.add(error);
	}

}
