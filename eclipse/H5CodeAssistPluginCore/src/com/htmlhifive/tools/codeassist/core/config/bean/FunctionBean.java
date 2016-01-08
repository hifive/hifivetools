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
package com.htmlhifive.tools.codeassist.core.config.bean;

import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.tools.codeassist.core.config.xml.Function;
import com.htmlhifive.tools.codeassist.core.config.xml.Variable;


/**
 * メソッド情報を保持するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class FunctionBean {
	/**
	 * メソッド名.
	 */
	private final String name;
	/**
	 * 戻り型.
	 */
	private final String returnType;

	/**
	 * 戻り値説明.
	 */
	private final String returnDescription;
	/**
	 * 引数.
	 */
	private final List<VariableBean> argments = new ArrayList<VariableBean>();

	/**
	 * ヘルプドキュメント.
	 */
	private String description;

	/**
	 * コンストラクタ.
	 * 
	 * @param func ファンクションオブジェクト.
	 */
	public FunctionBean(Function func) {

		this.name = func.getName();
		this.returnType = func.getReturnType();
		List<Variable> listArgHelp = func.getArgument();
		for (Variable variable : listArgHelp) {
			argments.add(new VariableBean(variable));
		}
		description = func.getDescription();
		returnDescription = func.getReturnDescription();

	}

	/**
	 * メソッド名を取得する.
	 * 
	 * @return メソッド名
	 */
	public String getName() {

		return name;
	}

	/**
	 * 戻り型を取得する.
	 * 
	 * @return 戻り型
	 */
	public String getReturnType() {

		return returnType;
	}

	/**
	 * 戻り値説明を取得する.
	 * 
	 * @return 戻り値説明
	 */
	public String getReturnDescription() {

		return returnDescription;
	}

	/**
	 * 引数を取得する.
	 * 
	 * @return 引数
	 */
	public VariableBean[] getArgments() {

		return (VariableBean[]) argments.toArray(new VariableBean[argments.size()]);
	}

	/**
	 * ヘルプドキュメントを取得する.
	 * 
	 * @return ヘルプドキュメント
	 */
	public String getDescription() {

		return description;
	}

}
