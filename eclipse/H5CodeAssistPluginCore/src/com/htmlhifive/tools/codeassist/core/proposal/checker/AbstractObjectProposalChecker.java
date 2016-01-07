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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.WorkingCopyOwner;
import org.eclipse.wst.jsdt.internal.codeassist.CompletionEngine;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.core.SearchableEnvironment;
import org.eclipse.wst.jsdt.ui.text.java.CompletionProposalCollector;

import com.htmlhifive.tools.codeassist.core.config.bean.RootChildrenElem;
import com.htmlhifive.tools.codeassist.core.exception.ProposalCheckException;

/**
 * 
 * オブジェクトが指定した正規表現にマッチするかどうかをチェックするチェッカ抽象クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public abstract class AbstractObjectProposalChecker implements ProposalChecker {

	/**
	 * コード補完対象ユニット.
	 */
	private IJavaScriptUnit unit;

	/**
	 * プロジェクト.
	 */
	private IJavaScriptProject project;

	/**
	 * パーサ実体.
	 */
	private CompletionParser parser;

	/**
	 * ダミーコード情報.
	 */
	private List<DummyCodeInfo> dummyCodeInfoList;

	/**
	 * リクエスター.
	 */
	private CompletionProposalCollector requestor;

	/**
	 * SearchableEnvironment.
	 */
	private SearchableEnvironment environment;

	/**
	 * 補完出力エンジン.
	 */
	private CompletionEngine engine;

	/**
	 * コンストラクタ.
	 * 
	 * @param unit ユニット
	 * @param project プロジェクト
	 * @throws JavaScriptModelException チェッカ生成例外.
	 */
	public AbstractObjectProposalChecker(IJavaScriptUnit unit, IJavaScriptProject project)
			throws JavaScriptModelException {

		this.project = project;
		this.unit = unit;
		this.dummyCodeInfoList = new ArrayList<DummyCodeInfo>();
		this.requestor = new CompletionProposalCollector(unit);
		this.environment = newSearchableNameEnvironment(unit, new WorkingCopyOwner() {
		});
		engine = new CompletionEngine(environment, requestor, project.getOptions(true), project);
		this.parser = (CompletionParser) engine.getParser();
	}

	@Override
	public boolean check(int invocationOffset) throws ProposalCheckException {

		// パースの前処理.
		CompilerOptions compilerOptions = new CompilerOptions(project.getOptions(true));
		CompilationResult compilationResult = new CompilationResult((ICompilationUnit) unit, 1, 1,
				compilerOptions.maxProblemsPerUnit);
		try {
			// ASTパースを行いルートオブジェクトを取得
			CompilationUnitDeclaration unitDeclaration = parser.dietParse((ICompilationUnit) unit, compilationResult,
					invocationOffset - 1);
			// プラグインでコード補完をする必要があるかチェック.
			return doCheckCodeAssist(unitDeclaration);
		} catch (Throwable e) {
			// 例外が発生したらfalseを返す
			return false;
		}
	}

	/**
	 * 解析した構文木ルートからコード補完を生成する必要があるかどうかを判定する.
	 * 
	 * @param unitDeclaration jsFileの構文木ルート.
	 * @return コード補完を生成する必要があるかどうか.
	 */
	protected abstract boolean doCheckCodeAssist(CompilationUnitDeclaration unitDeclaration);

	@Override
	public DummyCodeInfo[] getDummyCodeInfo() {

		// 挿入位置の降順にソートする.
		Collections.sort(dummyCodeInfoList, new Comparator<DummyCodeInfo>() {
			@Override
			public int compare(DummyCodeInfo arg0, DummyCodeInfo arg1) {

				Integer thisInsertPosition = Integer.valueOf(arg0.getInsertPosition());
				Integer anotherInsertPositionInteger = Integer.valueOf(arg1.getInsertPosition());
				return anotherInsertPositionInteger.compareTo(thisInsertPosition);
			}
		});
		return (DummyCodeInfo[]) dummyCodeInfoList.toArray(new DummyCodeInfo[dummyCodeInfoList.size()]);
	}

	/**
	 * SearchableEnvironmentを生成する.
	 * 
	 * @param iJavaScriptUnit ユニット
	 * @param primary ワーキングコピーオーナー
	 * @return SearchableEnvironment
	 * @throws JavaScriptModelException 生成例外.
	 */
	private SearchableEnvironment newSearchableNameEnvironment(IJavaScriptUnit iJavaScriptUnit, WorkingCopyOwner primary)
			throws JavaScriptModelException {

		return iJavaScriptUnit.getParent() != null ? iJavaScriptUnit.getParent().newSearchableNameEnvironment(primary)
				: iJavaScriptUnit.getJavaScriptProject().newSearchableNameEnvironment(primary);
	}

	/**
	 * ダミーコード情報を追加する.
	 * 
	 * @param dummyCodeInfo ダミーコード情報
	 */
	protected void addDummyCodeInfoList(DummyCodeInfo dummyCodeInfo) {

		this.dummyCodeInfoList.add(dummyCodeInfo);
	}

	/**
	 * ルートの子要素を取得する.
	 * 
	 * @return ルートの子要素.
	 */
	protected abstract RootChildrenElem getBean();

	/**
	 * コード補完ノードを取得する.
	 * 
	 * @return コード補完ノード
	 */
	abstract CompletionOnMemberAccess getMemberAccess();

}
