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
package jp.co.nssol.h5.tools.codeassist.core.proposal;

/**
 * コード補完生成実装クラスの抽象クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public abstract class AbstractProposalCreator implements ProposalCreator {
	/**
	 * 疑似ファイル名.
	 */
	protected static final String NAME_TEMPJSFILE = "_xxx.js";
	/**
	 * コード補完コンテキスト.
	 */
	private ProposalContext context;

	/**
	 * コンストラクタ.
	 * 
	 * @param context コード補完コンテキスト.
	 */
	public AbstractProposalCreator(ProposalContext context) {

		this.context = context;
	}

	/**
	 * コード補完コンテキストを取得します.
	 * 
	 * @return コード補完コンテキスト
	 */
	protected ProposalContext getContext() {

		return context;
	}

}
