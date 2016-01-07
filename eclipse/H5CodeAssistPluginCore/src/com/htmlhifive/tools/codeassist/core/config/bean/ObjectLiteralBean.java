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

import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.tools.codeassist.core.config.xml.Function;
import com.htmlhifive.tools.codeassist.core.config.xml.VarReference;


/**
 * 設定ファイルのオブジェクトリテラルのビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public abstract class ObjectLiteralBean implements RegExPatternRootChild {
	/**
	 * 定義したファンクション.
	 */
	private List<FunctionBean> functions = new ArrayList<FunctionBean>();

	/**
	 * 定義した変数.
	 */
	private List<VarReferenceBean> varRefs = new ArrayList<VarReferenceBean>();

	/**
	 * コンストラクタ.
	 * 
	 * @param functionOrVarRefList オブジェクトの要素.
	 */
	public ObjectLiteralBean(List<Object> functionOrVarRefList) {

		for (Object object : functionOrVarRefList) {
			if (object instanceof Function) {
				functions.add(new FunctionBean((Function) object));
			}
			if (object instanceof VarReference) {
				varRefs.add(new VarReferenceBean((VarReference) object));
			}

		}
	}

	/**
	 * 定義したファンクションを取得する.
	 * 
	 * @return 定義したファンクション
	 */
	public FunctionBean[] getFunctions() {

		return (FunctionBean[]) functions.toArray(new FunctionBean[functions.size()]);
	}

	/**
	 * 定義した変数を取得する.
	 * 
	 * @return 定義した変数
	 */
	public VarReferenceBean[] getVarRefs() {

		return (VarReferenceBean[]) varRefs.toArray(new VarReferenceBean[varRefs.size()]);
	}
}
