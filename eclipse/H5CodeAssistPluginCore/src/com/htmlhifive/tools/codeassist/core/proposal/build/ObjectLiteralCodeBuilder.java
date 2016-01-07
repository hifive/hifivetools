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


import org.apache.commons.lang.CharUtils;

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;
import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;

/**
 * オブジェクトリテラルに追加するダミーコードビルダー.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ObjectLiteralCodeBuilder extends AbstractContextCodeBuilder {

	/**
	 * コンストラクタ.
	 */
	public ObjectLiteralCodeBuilder() {

		super(null);
	}

	/**
	 * コード追加例.<br>
	 * <code>
	 * ,methodName:function(arg1,arg2){return new Return();}
	 * </code>
	 * 
	 * @param sb 追加されるビルダー.
	 */
	@Override
	protected void buildMethod(StringBuilder sb) {

		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		FunctionBean[] methods = getFunctions();
		for (FunctionBean method : methods) {
			sb.append(",");
			sb.append(method.getName());
			sb.append(":");
			CodeBuilderUtils.addFunctionCode(sb, method);
			sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		}

	}

	@Override
	protected void buildVarRef(StringBuilder sb) {

		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		VarReferenceBean[] beans = getFields();
		for (VarReferenceBean varReferenceBean : beans) {
			sb.append(",");
			CodeBuilderUtils.addJsProperty(sb, varReferenceBean);
			sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		}
	}

	@Override
	protected void buildEnd(StringBuilder sb, StringBuilder part, int tempInsertPosition) {

		// 一文字削除するフラグ
		boolean delflg = true;
		int deletePosition = 0;
		int insertPosition = tempInsertPosition;
		for (int currentPosition = insertPosition - 1; currentPosition > 0; currentPosition--) {
			if (sb.charAt(currentPosition) == ',') {
				deletePosition = currentPosition;
				break;
			}
			if (CharUtils.isAsciiPrintable(sb.charAt(currentPosition))) {
				delflg = false;
				break;
			}
		}
		// while (sb.charAt(currentPosition) != ',') {
		// currentPosition--;
		// System.out.println(sb.charAt(currentPosition));
		// }
		if (delflg) {
			insertPosition--;
			sb.deleteCharAt(deletePosition);
		}
		super.buildEnd(sb, part, insertPosition);
	}

	@Override
	protected void buildConstructor(StringBuilder sb) {

		// コンストラクタは必要なし.

	}

}
