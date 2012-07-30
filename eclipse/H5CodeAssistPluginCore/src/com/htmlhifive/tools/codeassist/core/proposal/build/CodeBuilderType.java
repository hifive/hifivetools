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

/**
 * コード補完呼び出し時のタイプ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public enum CodeBuilderType {
	/**
	 * this.で補完を呼び出したとき.
	 */
	OBJ_LITERAL,
	/**
	 * 委譲しているオブジェクトから呼び出した場合.
	 */
	REFERENCE_OBJ,

	/**
	 * オブジェクトを生成するビルダー.
	 */
	CREATE_OBJ,

	/**
	 * その他.
	 */
	OTHER,
}
