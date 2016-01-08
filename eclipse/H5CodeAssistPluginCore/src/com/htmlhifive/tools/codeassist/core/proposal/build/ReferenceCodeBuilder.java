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
package com.htmlhifive.tools.codeassist.core.proposal.build;


import org.apache.commons.lang.StringUtils;

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;
import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;

/**
 * 指定したオブジェクトにメソッドを追加するためのビルダー.
 * 
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ReferenceCodeBuilder extends AbstractContextCodeBuilder {

	/**
	 * コンストラクタ. <br>
	 * 引数が"null"又は空文字の場合はIllegalArgumentException()をthrowする.
	 * 
	 * @param referenceName メソッド等を追加されるオブジェクト名.
	 */
	public ReferenceCodeBuilder(String referenceName) {

		super(referenceName);
		if (StringUtils.isEmpty(referenceName)) {
			throw new IllegalArgumentException();
		}

	}

	/**
	 * コード追加例.<br>
	 * className.methodName=function(arg1,arg2){return new Return();};.
	 * 
	 * @param sb 追加されるストリングビルダー.
	 */
	@Override
	protected void buildMethod(StringBuilder sb) {

		FunctionBean[] methods = getFunctions();
		for (FunctionBean method : methods) {
			sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
			sb.append(getClassName());
			sb.append('.');
			sb.append(method.getName());
			sb.append("=");
			CodeBuilderUtils.addFunctionCode(sb, method);
			sb.append(";");
		}
		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
	}

	@Override
	protected void buildVarRef(StringBuilder sb) {

		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		VarReferenceBean[] beans = getFields();
		for (VarReferenceBean varReferenceBean : beans) {
			sb.append(getClassName());
			sb.append(".");
			sb.append(varReferenceBean.getKey());
			sb.append("= new ");
			sb.append(varReferenceBean.getClassName());
			sb.append("();");
		}
		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
	}

	@Override
	protected void buildConstructor(StringBuilder sb) {

		// コンストラクタの必要はなし.

	}

}
