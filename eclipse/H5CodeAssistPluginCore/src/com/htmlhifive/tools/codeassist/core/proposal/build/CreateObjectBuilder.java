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

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;
import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;

/**
 * JavaScriptでオブジェクトを生成するコードを生成する.<br>
 * ex.<br>
 * <code>
 * obj={<br>
 * method : function(){},<br>
 * 　　key : value,<br>
 * };<br>
 * 
 * </code>
 * 
 * 
 * @author NS Solutions Corporation
 */
public class CreateObjectBuilder extends AbstractContextCodeBuilder {

	/**
	 * コンストラクタ.
	 * 
	 * @param objName ダミーとして生成するオブジェクト名.
	 */
	public CreateObjectBuilder(String objName) {

		super(objName);
	}

	@Override
	protected void buildStart(StringBuilder sb) {

		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		sb.append(getClassName());
		sb.append("={");
		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
	}

	@Override
	protected void buildMethod(StringBuilder sb) {

		FunctionBean[] beans = getFunctions();
		for (FunctionBean functionBean : beans) {
			sb.append(functionBean.getName());
			sb.append(":");
			CodeBuilderUtils.addFunctionCode(sb, functionBean);
			sb.append(",");
			sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		}
	}

	@Override
	protected void buildVarRef(StringBuilder sb) {

		VarReferenceBean[] beans = getFields();
		for (VarReferenceBean varReferenceBean : beans) {
			CodeBuilderUtils.addJsProperty(sb, varReferenceBean);
			sb.append(",");
			sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		}
	}

	@Override
	protected void buildConstructor(StringBuilder sb) {

		// no impl
	}

	@Override
	protected void buildEnd(StringBuilder sb, StringBuilder part, int insertPosition) {

		part.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		int last = part.lastIndexOf(",");
		if (last != -1) {
			part.deleteCharAt(last);
		}
		part.append("};");
		part.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		sb.insert(insertPosition, part.toString());

	}

}
