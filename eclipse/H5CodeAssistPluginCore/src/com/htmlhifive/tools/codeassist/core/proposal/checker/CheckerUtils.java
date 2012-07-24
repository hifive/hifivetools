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

import java.util.List;
import java.util.regex.Pattern;


import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IAssignment;
import org.eclipse.wst.jsdt.core.ast.ILocalDeclaration;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.core.ast.IStatement;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;

import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.ObjectLiteralBean;
import com.htmlhifive.tools.codeassist.core.proposal.collector.FunctionNameVisitor;
import com.htmlhifive.tools.codeassist.core.proposal.collector.MemberAccessVisitor;
import com.htmlhifive.tools.codeassist.core.proposal.collector.NodeCollector;

/**
 * 
 * チェッカのユーティルクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public final class CheckerUtils {

	/**
	 * プロパティ(変数名)のパターン.
	 */
	private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z]\\w*");

	/**
	 * コンストラクタ.
	 */
	private CheckerUtils() {

		// no create
	}

	/**
	 * コード補完時の文字列から呼び出し元のオブジェクト名を取得する.
	 * 
	 * @param codeAssistStr コード補完時の文字列.
	 * @return 呼び出し元
	 */
	public static String getRootObject(String codeAssistStr) {

		String[] codeAssistStrParts = StringUtils.split(codeAssistStr, ".");
		// ひとつ前のアシスト部.
		String beforePart = null;
		for (String part : codeAssistStrParts) {
			if (KEY_PATTERN.matcher(part).matches() || beforePart == null) {
				beforePart = part;
				continue;
			}
			return beforePart;
		}
		return beforePart;
	}

	/**
	 * 構文木のルートとコレクターから、ノード情報を取得する.
	 * 
	 * @param collector コレクタ
	 * @param unitDeclaration jsFileの構文木ルート.
	 * @return ノード情報
	 */
	static SuffixAssistNodeInfo getAssistNodeInfo(NodeCollector collector, CompilationUnitDeclaration unitDeclaration) {

		// 収集したノードを取り出す
		IStatement[] collectedNodes = (IStatement[]) collector.getNodes();
		// List<IStatement> relateControllerNodes = new ArrayList<IStatement>();
		SuffixAssistNodeInfo info = new SuffixAssistNodeInfo();
		// コード補完時のノードを取得
		MemberAccessVisitor visitor = new MemberAccessVisitor();
		unitDeclaration.traverse(visitor);
		info.setMemberAccess(visitor.getMemberAccess());
		for (IStatement collectedNode : collectedNodes) {
			visitor = new MemberAccessVisitor();
			collectedNode.traverse(visitor);
			if (visitor.getMemberAccess() != null) {
				// relateControllerNodes.add(controller);
				info.setMemberAccess(visitor.getMemberAccess());
				// ノード内にコード補完ノードがあった場合対象ノードを追加
				info.addTargetNodeList(collectedNode);
			}
		}
		return info;
	}

	/**
	 * コード補完文字列から補完する必要があるか判定する.
	 * 
	 * @param bean オブジェクトリテラルビーン
	 * @param codeAssistNode 補完文字列
	 * @return コード補完する必要があるかどうか
	 */
	static boolean checkCodeAssistNode(ObjectLiteralBean bean, CompletionOnMemberAccess codeAssistNode) {

		FunctionNameVisitor visitor = new FunctionNameVisitor();
		codeAssistNode.traverse(visitor);
		List<String> funcNames = visitor.getLastFunctionNames();
		if (funcNames.size() == 0) {
			return true;
		}
		FunctionBean[] elems = bean.getFunctions();
		for (FunctionBean elem : elems) {
			if (funcNames.contains(elem.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * astNodeの名前がサフィックスにマッチする場合ソース終了位置を返す.<br>
	 * そうでない場合は-1を返す.<br>
	 * ILocalDeclarationとIObjectLiteralFieldが対象.
	 * 
	 * @param suffixPattern サフィックスの正規表現.
	 * @param astNode 検査対象ノード
	 * @return そのオブジェクトリテラルのソース終了位置.
	 */
	static int getInitializerSourceEnd(Pattern suffixPattern, IASTNode astNode) {

		String name = null;
		int result = 0;
		if (astNode instanceof ILocalDeclaration) {
			ILocalDeclaration localDec = (ILocalDeclaration) astNode;
			name = String.valueOf(localDec.getName());
			result = localDec.getInitialization().sourceEnd();
		} else if (astNode instanceof IObjectLiteralField) {
			IObjectLiteralField objectLit = (IObjectLiteralField) astNode;
			name = objectLit.getFieldName().toString();
			result = objectLit.getInitializer().sourceEnd();
		} else if (astNode instanceof IAssignment) {
			// xxxSuffix.prototypeの時
			IAssignment assignment = (IAssignment) astNode;
			// xxxSuffixの取得
			name = StringUtils.split(assignment.getLeftHandSide().toString(), '.')[0];
			result = assignment.getExpression().sourceEnd();
		}
		if (name != null && suffixPattern.matcher(name).matches()) {
			return result;
		}
		return -1;
	}
}
