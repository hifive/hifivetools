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
package jp.co.nssol.h5.tools.codeassist.core.config.bean;

import jp.co.nssol.h5.tools.codeassist.core.config.xml.Variable;

/**
 * 
 * 型と名前を保持するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class VariableBean {

	/**
	 * 変数名.
	 */
	private String name;

	/**
	 * 変数型.
	 */
	private String type;

	/**
	 * 説明.
	 */
	private String description;

	/**
	 * コンストラクタ.
	 * 
	 * @param variable 変数.
	 */
	public VariableBean(Variable variable) {

		this.name = variable.getName();
		this.type = variable.getType();
		this.description = variable.getDescription();
	}

	/**
	 * 変数名を取得する.
	 * 
	 * @return 変数名
	 */
	public String getName() {

		return name;
	}

	/**
	 * 変数型を取得する.
	 * 
	 * @return 変数型
	 */
	public String getType() {

		return type;
	}

	/**
	 * 説明を取得する.
	 * 
	 * @return 説明
	 */
	public String getDescription() {

		return description;
	}

	// @Override
	// public JsObjectValueType getValuleType() {
	//
	// return JsObjectValueType.VARIABLE;
	// }

}
