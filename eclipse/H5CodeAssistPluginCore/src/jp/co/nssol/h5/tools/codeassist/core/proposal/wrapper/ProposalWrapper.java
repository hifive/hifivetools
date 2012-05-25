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
package jp.co.nssol.h5.tools.codeassist.core.proposal.wrapper;

import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

/**
 * インターナルパッケージのプロポーザルを使用するときの<br>
 * ラッパーインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface ProposalWrapper extends IJavaCompletionProposal, ICompletionProposalExtension,
		ICompletionProposalExtension2, ICompletionProposalExtension3 {

	/**
	 * 関連するJavaElementを取得する.
	 * 
	 * @return JavaElement
	 */
	public abstract IJavaScriptElement getJavaElement();

	/**
	 * 追加情報を取得する.
	 * 
	 * @return 追加情報.
	 */
	public abstract String getAdditionalProposalInfo();

	/**
	 * コード補完のアイコンを取得する.
	 * 
	 * @return コード補完のアイコン.
	 */
	public abstract Image getImage();

	/**
	 * 置き換える文字列を取得する.
	 * 
	 * @return 置き換える文字列.
	 */
	public abstract String getReplacementString();

	/**
	 * 置き換える文字列の長さを取得する.
	 * 
	 * @return 置き換える文字列の長さ.
	 */
	public abstract int getReplacementLength();

	/**
	 * 置き換える文字列のオフセットを取得する.
	 * 
	 * @return 置き換える文字列のオフセット.
	 */
	public abstract int getReplacementOffset();

	/**
	 * 表示文字列を取得する.
	 * 
	 * @return 表示文字列.
	 */
	public abstract String getDisplayString();

	/**
	 * コンテキスト情報を取得する.
	 * 
	 * @return コンテキスト情報.
	 */
	public abstract IContextInformation getContextInformation();

	/**
	 * プロポーザルの実体を取得する.
	 * 
	 * @return プロポーザル実体.
	 */
	public abstract IJavaCompletionProposal getRealProp();

}
