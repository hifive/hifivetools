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
package com.htmlhifive.tools.codeassist.core.proposal.checker;

import com.htmlhifive.tools.codeassist.core.proposal.build.CodeBuilderType;

/**
 * デリゲート用のダミーコードに必要な情報ビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class DelegateDummyCodeInfo extends DummyCodeInfo {

	/**
	 * 外部定義情報が追加されるオブジェクト名.
	 */
	private final String addedObjName;

	/**
	 * コンストラクタ.
	 * 
	 * @param insertPosition 挿入位置.
	 * @param addedObjName 外部定義情報が追加されるオブジェクト名
	 * @param builderType ダミーコード生成時のコードビルダタイプ
	 */
	public DelegateDummyCodeInfo(int insertPosition, String addedObjName, CodeBuilderType builderType) {

		super(insertPosition, builderType);
		this.addedObjName = addedObjName;

	}

	/**
	 * 外部定義情報が追加されるオブジェクト名を取得する.
	 * 
	 * @return 外部定義情報が追加されるオブジェクト名
	 */
	public String getAddedObjName() {

		return addedObjName;
	}
}
