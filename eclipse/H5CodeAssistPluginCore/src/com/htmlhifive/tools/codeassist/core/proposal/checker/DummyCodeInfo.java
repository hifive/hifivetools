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
package com.htmlhifive.tools.codeassist.core.proposal.checker;

import com.htmlhifive.tools.codeassist.core.proposal.build.CodeBuilderType;

/**
 * チェッカで取得したダミーコードを生成するために必要な情報のビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class DummyCodeInfo {

	/**
	 * ダミーコードの挿入位置.
	 */
	private final int insertPosition;

	/**
	 * ビルダータイプ.
	 */
	private final CodeBuilderType builderType;

	/**
	 * コンストラクタ.
	 * 
	 * @param insertPosition ダミーコードの挿入位置.
	 * @param builderType ダミーコード生成時のコードビルダタイプ
	 */
	public DummyCodeInfo(int insertPosition, CodeBuilderType builderType) {

		this.insertPosition = insertPosition;
		this.builderType = builderType;
	}

	/**
	 * ダミーコードの挿入位置を取得する.
	 * 
	 * @return ダミーコードの挿入位置
	 */
	public int getInsertPosition() {

		return insertPosition;
	}

	/**
	 * ビルダータイプを取得する.
	 * 
	 * @return ビルダータイプ
	 */
	public CodeBuilderType getBuilderType() {

		return builderType;
	}

}
