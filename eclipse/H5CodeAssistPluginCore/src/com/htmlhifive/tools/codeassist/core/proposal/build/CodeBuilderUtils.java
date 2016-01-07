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


import org.apache.commons.lang.StringUtils;

import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VariableBean;

/**
 * コードビルダーのユーティルクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
final class CodeBuilderUtils {
	/**
	 * コンストラクタ.
	 */
	private CodeBuilderUtils() {

	}

	/**
	 * FunctionBeanオブジェクトから匿名関数コードを追加する.<br>
	 * ex.<br>
	 * <code>
	 * function(arg1,arg2){return new Return();}
	 * </code>
	 * 
	 * @param sb コードを追加されるオブジェクト.
	 * @param method コードの元となるメソッドオブジェクト.
	 */
	static void addFunctionCode(StringBuilder sb, FunctionBean method) {

		sb.append("function(");
		VariableBean[] args = method.getArgments();
		boolean existArgs = false;
		for (VariableBean arg : args) {
			existArgs = true;
			sb.append(arg.getName());
			sb.append(",");
		}
		// カンマを削除
		if (existArgs) {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append("){");
		if (StringUtils.isNotEmpty(method.getReturnType()) && !(method.getReturnType().equals("void"))) {
			sb.append("return new ");
			sb.append(method.getReturnType());
			sb.append("();");
		}
		sb.append("}");
	}

	/**
	 * プロパティのコードを追加する.<br>
	 * <code>
	 * key : new ClassName()
	 * </code>
	 * 
	 * @param sb コードを追加されるオブジェクト.
	 * @param varReferenceBean コードの元となるオブジェクト.
	 */
	public static void addJsProperty(StringBuilder sb, VarReferenceBean varReferenceBean) {

		sb.append(varReferenceBean.getKey());
		sb.append(": new ");
		sb.append(varReferenceBean.getClassName());
		sb.append("()");

	}
}
