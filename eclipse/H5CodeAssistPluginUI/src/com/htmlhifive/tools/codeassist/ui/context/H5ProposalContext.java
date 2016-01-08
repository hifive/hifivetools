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
package com.htmlhifive.tools.codeassist.ui.context;

import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.ui.text.java.JavaContentAssistInvocationContext;

import com.htmlhifive.tools.codeassist.core.proposal.ProposalContext;

/**
 * Hi5のコード補完コンテキストデフォルト実装クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class H5ProposalContext implements ProposalContext {

	/**
	 * ドキュメント.
	 */
	private final IDocument document;

	/**
	 * プロジェクト.
	 */
	private final IJavaScriptProject project;

	/**
	 * コード補完のオフセット.
	 */
	private final int invocationOffset;

	/**
	 * ターゲットユニット.
	 */
	private final IJavaScriptUnit compilationUnit;

	/**
	 * コンストラクタ.
	 * 
	 * @param context コンテキスト.
	 */
	public H5ProposalContext(JavaContentAssistInvocationContext context) {

		document = context.getDocument();
		project = context.getProject();
		invocationOffset = context.getInvocationOffset();
		compilationUnit = context.getCompilationUnit();
	}

	@Override
	public IDocument getDocument() {

		return document;
	}

	@Override
	public IJavaScriptProject getProject() {

		return project;
	}

	@Override
	public int getInvocationOffset() {

		return invocationOffset;
	}

	@Override
	public IJavaScriptUnit getCompilationUnit() {

		return compilationUnit;
	}

}
