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

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;
import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;

/**
 * JavaScriptのクラスの定義コードを生成する.<br>
 * JavaScriptにはクラスの概念が存在しないが、prototypeプロパティを使用し<br>
 * クラスのような挙動を示すオブジェクトを生成する.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JsContextCodeBuilder extends AbstractContextCodeBuilder {

	/**
	 * コンストラクタ.<br>
	 * 
	 * 
	 * @param className ダミーで生成するクラス名.
	 */
	public JsContextCodeBuilder(String className) {

		super(className);
	}

	/**
	 * jsのメソッドを定義する.<br>
	 * ex.<br>
	 * <code>
	 * Sample.prototype = { <br>
	 * methodName : function() {<br>
	 * <br>
	 * 			return new ReturnType();<br>
	 * 			}<br>
	 * </code>
	 * 
	 * @param sb コード
	 */
	@Override
	protected void buildMethod(StringBuilder sb) {

		sb.append(getClassName());
		sb.append(".prototype = {");
		for (FunctionBean method : getFunctions()) {
			sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
			sb.append(method.getName());
			sb.append(" : ");
			CodeBuilderUtils.addFunctionCode(sb, method);
			sb.append(",");
		}
		// カンマを削除
		sb.deleteCharAt(sb.length() - 1);
		sb.append("};");
	}

	@Override
	protected void buildVarRef(StringBuilder sb) {

		// no operation
	}

	/**
	 * jsのコンストラクタを定義する.<br>
	 * ex.<br>
	 * var Sample = function () {};
	 * 
	 * @param sb コード
	 */
	@Override
	protected void buildConstructor(StringBuilder sb) {

		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		sb.append("var ");
		sb.append(getClassName());
		sb.append(" =function(){};");
		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);

	}

}
