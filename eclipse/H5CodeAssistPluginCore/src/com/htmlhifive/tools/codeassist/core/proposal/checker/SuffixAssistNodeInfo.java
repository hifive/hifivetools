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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;

/**
 * サフィックスが一致した時(主にコントローラ、ロジック)用のコード補完に必要なノード情報.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
class SuffixAssistNodeInfo {

	/**
	 * コード補完ノード.
	 */
	private CompletionOnMemberAccess memberAccess;

	/**
	 * コード補完対象(メソッド等を追加する対象)のオブジェクト.
	 */
	private List<IASTNode> targetNodeList;

	/**
	 * コンストラクタ.
	 */
	public SuffixAssistNodeInfo() {

		targetNodeList = new ArrayList<IASTNode>();
	}

	/**
	 * targetNodeを追加する.
	 * 
	 * @param targetNode ノード.
	 */
	public void addTargetNodeList(IASTNode targetNode) {

		this.targetNodeList.add(targetNode);
	}

	/**
	 * コード補完ノード.を取得する.
	 * 
	 * @return コード補完ノード.
	 */
	public CompletionOnMemberAccess getMemberAccess() {

		return memberAccess;
	}

	/**
	 * コード補完ノード.を設定する.
	 * 
	 * @param memberAccess コード補完ノード.
	 */
	public void setMemberAccess(CompletionOnMemberAccess memberAccess) {

		this.memberAccess = memberAccess;
	}

	/**
	 * コード補完対象のオブジェクトを取得する.
	 * 
	 * @return コード補完対象のオブジェクトノード.
	 */
	public IASTNode[] getTargetNodes() {

		return (IASTNode[]) targetNodeList.toArray(new IASTNode[targetNodeList.size()]);
	}
}
