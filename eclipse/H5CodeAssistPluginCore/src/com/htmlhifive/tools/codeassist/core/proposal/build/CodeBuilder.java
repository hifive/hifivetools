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
package com.htmlhifive.tools.codeassist.core.proposal.build;

import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;

/**
 * 
 * メソッド定義とフィールド定義をしたコードを生成するインターフェース.<br>
 * 
 * @see AbstractContextCodeBuilder
 * @author NS Solutions Corporation
 * 
 */
public interface CodeBuilder {

	/**
	 * コードを追加する.
	 * 
	 * @param sb 追加するStringBuilder
	 * @param insertPosition 追加位置.
	 * @return 生成したコード.
	 */
	String build(StringBuilder sb, int insertPosition);

	/**
	 * コードを追加する.
	 * 
	 * @param sb 追加するStringBuilder
	 * @return 生成したコード.
	 */
	String build(StringBuilder sb);

	/**
	 * コードを生成する.
	 * 
	 * @return 生成したコード.
	 */
	String build();

	/**
	 * インターフェースにメソッドを追加する.<br>
	 * 
	 * @param functionBean ファンクション定義オブジェクト
	 */
	void addFunction(FunctionBean functionBean);

	/**
	 * フィールドを追加する.
	 * 
	 * @param varReference フィールド
	 */
	void addField(VarReferenceBean varReference);

}
