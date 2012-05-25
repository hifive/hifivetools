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

import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;

/**
 * コード補完に必要なコンテキスト情報を取得するインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface ProposalContext {

	/**
	 * コード補完対象ファイルのドキュメントを取得する.
	 * 
	 * @return ドキュメント.
	 */
	public abstract IDocument getDocument();

	/**
	 * コード補完対象ファイルがあるプロジェクトを取得する.
	 * 
	 * @return jsプロジェクト.
	 */
	public abstract IJavaScriptProject getProject();

	/**
	 * コード補完のオフセットを取得する.
	 * 
	 * @return オフセット.
	 */
	public abstract int getInvocationOffset();

	/**
	 * コード補完対象ユニットを取得する.
	 * 
	 * @return コード補完対象ユニット.
	 */
	public abstract IJavaScriptUnit getCompilationUnit();

}
