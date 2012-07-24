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
package com.htmlhifive.tools.codeassist.core.proposal.wrapper;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.internal.ui.text.java.FilledArgumentNamesMethodProposal;
import org.eclipse.wst.jsdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VariableBean;
import com.htmlhifive.tools.codeassist.core.messages.Messages;

/**
 * ProposalWrapperのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public final class ProposalWrapperFactory {
	/**
	 * コンストラクタ.
	 */
	private ProposalWrapperFactory() {

		// nocreate
	}

	/**
	 * デフォルトのコード補完で取得したプロポーザルから、<br>
	 * 適切なラッパクラスを生成する.
	 * 
	 * @param proposal コード補完情報.
	 * @return プロポーザルラッパ.
	 */
	public static ProposalWrapper createProposalWrapper(ICompletionProposal proposal) {

		return createProposalWrapper(proposal, null);
	}

	/**
	 * デフォルトのコード補完で取得したプロポーザルから、<br>
	 * 適切なラッパクラスを生成する.
	 * 
	 * @param proposal コード補完情報.
	 * @param helpDocument ヘルプドキュメント.
	 * @return プロポーザルラッパ.
	 */
	private static ProposalWrapper createProposalWrapper(ICompletionProposal proposal, String helpDocument) {

		if (proposal instanceof FilledArgumentNamesMethodProposal) {
			return new FunctionProposalWrapper((FilledArgumentNamesMethodProposal) proposal, helpDocument);
		} else if (proposal instanceof JavaCompletionProposal) {
			// TODO ヘルプの有無
			return new ObjectProposalWrapper((JavaCompletionProposal) proposal);
		}
		return null;
	}

	/**
	 * デフォルトのコード補完で取得したプロポーザルから、<br>
	 * 適切なラッパクラスを生成する.
	 * 
	 * @param proposal コード補完情報.
	 * @param elem ファンクションビーン.
	 * @return プロポーザルラッパ.
	 */
	public static ProposalWrapper createProposalWrapper(IJavaCompletionProposal proposal, FunctionBean elem) {

		String helpDocument = buildHelpDocument(elem);
		return createProposalWrapper(proposal, helpDocument);
	}

	/**
	 * ファンクションビーンからドキュメントコードを生成する.
	 * 
	 * @param elem ファンクションビーン
	 * @return ドキュメントコード.
	 */
	private static String buildHelpDocument(FunctionBean elem) {

		StringBuilder sb = new StringBuilder();
		String desc = elem.getDescription();
		sb.append(desc == null ? "" : desc);
		sb.append("<dl>");
		if (elem.getArgments() != null && elem.getArgments().length != 0) {
			sb.append("<dt>");
			sb.append(Messages.DES0001.getText());
			sb.append("</dt>");
			for (VariableBean arg : elem.getArgments()) {
				addParams(sb, arg);
			}
		}
		if (StringUtils.isNotEmpty(elem.getReturnType()) && !StringUtils.equals(elem.getReturnType(), "void")) {
			addReturnType(sb, elem.getReturnType(), elem.getReturnDescription());
		}
		sb.append("</dl>");
		if (StringUtils.equals("<dl></dl>", sb.toString())) {
			return null;
		}
		return sb.toString();
	}

	/**
	 * 戻り値の情報をStringBuilderに追加する.
	 * 
	 * @param sb 追加されるStringBuilder
	 * @param returnType 戻り型
	 * @param description 戻り値の説明
	 */
	private static void addReturnType(StringBuilder sb, String returnType, String description) {

		sb.append("<dt>");
		sb.append(Messages.DES0002.getText());
		sb.append("</dt><dd>{");
		sb.append(returnType);
		sb.append("} ");
		String returnDescription = StringEscapeUtils.escapeHtml(description);
		sb.append(returnDescription == null ? "" : returnDescription);
		sb.append("</dd>");
	}

	/**
	 * 引数の情報をStringBuilderに追加する.
	 * 
	 * @param sb 追加されるStringBuilder
	 * @param arg 引数
	 */
	private static void addParams(StringBuilder sb, VariableBean arg) {

		sb.append("<dd>{");
		sb.append(arg.getType() == null ? "" : arg.getType());
		sb.append("} ");
		sb.append(arg.getName() == null ? "" : arg.getName());
		sb.append(" ");
		sb.append(arg.getDescription() == null ? "" : arg.getDescription());
		sb.append("</dd>");

	}
}
