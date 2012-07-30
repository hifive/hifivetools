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
package com.htmlhifive.tools.codeassist.core.config.bean;

import com.htmlhifive.tools.codeassist.core.config.xml.VarReference;

/**
 * jsオブジェクトのプロパティとキー値のビーンクラス.<br>
 * 
 * 
 * @author NS Solutions Corporation
 * 
 */
public class VarReferenceBean {

	/**
	 * プロパティのキー値.
	 */
	private String key;

	/**
	 * プロパティのクラス名.
	 */
	private String className;

	/**
	 * コンストラクタ.
	 * 
	 * @param varReference キーバリュー.
	 */
	public VarReferenceBean(VarReference varReference) {

		this.key = varReference.getKey();
		this.className = varReference.getClassName();
	}

	/**
	 * プロパティのキー値を取得する.
	 * 
	 * @return プロパティのキー値
	 */
	public String getKey() {

		return key;
	}

	/**
	 * プロパティのクラス名を取得する.
	 * 
	 * @return プロパティのクラス名.
	 */
	public String getClassName() {

		return className;
	}

}
