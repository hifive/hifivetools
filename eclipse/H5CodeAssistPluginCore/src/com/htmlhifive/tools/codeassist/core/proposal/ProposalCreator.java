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
package com.htmlhifive.tools.codeassist.core.proposal;

import java.util.List;


import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.htmlhifive.tools.codeassist.core.exception.ProposalCreateException;

/**
 * コード補完情報を生成する.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface ProposalCreator {

	/**
	 * コンテキスト情報と設定ファイルの情報からプロポーザルオブジェクトを生成する.
	 * 
	 * @return コード補完のリスト.
	 * @throws ProposalCreateException コード補完生成例外.
	 */
	List<ICompletionProposal> createProposal() throws ProposalCreateException;

}
