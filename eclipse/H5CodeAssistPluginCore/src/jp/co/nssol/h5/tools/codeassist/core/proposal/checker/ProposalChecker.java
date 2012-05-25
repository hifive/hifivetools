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
package jp.co.nssol.h5.tools.codeassist.core.proposal.checker;

import jp.co.nssol.h5.tools.codeassist.core.exception.ProposalCheckException;

/**
 * プラグインでコード補完を追加する必要があるかをチェックする.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface ProposalChecker {

	/**
	 * コード補完をする必要があるかどうかをチェックする.
	 * 
	 * @param invocationOffset コード補完のオフセット.
	 * @return コード補完を生成する必要があるかどうか.
	 * @throws ProposalCheckException チェック時の例外.
	 */
	boolean check(int invocationOffset) throws ProposalCheckException;

	/**
	 * ダミーコード情報を取得する.
	 * 
	 * @return ダミーコード情報.
	 */
	DummyCodeInfo[] getDummyCodeInfo();

	/**
	 * コード補完時の文字列を取得する.
	 * 
	 * @return コード補完時の文字列
	 */
	String getCodeAssistStr();

	/**
	 * デフォルトでコードアシストが存在するかどうかを取得する.
	 * 
	 * @return デフォルトでコードアシストが存在するかどうか.
	 */
	boolean existDefaultCodeAssist();

}
