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

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;

import com.htmlhifive.tools.codeassist.core.config.bean.ObjectLiteralBean;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.core.messages.Messages;
import com.htmlhifive.tools.codeassist.core.proposal.build.CodeBuilderType;
import com.htmlhifive.tools.codeassist.core.proposal.collector.NodeCollector;
import com.htmlhifive.tools.codeassist.core.proposal.collector.NodeCollectorFactory;

/**
 * サフィックス一致でプロポーザルを判定する抽象クラス.<br>
 * (主にロジック、コントローラ用)
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
abstract class AbstractSuffixProposalChecker extends AbstractObjectProposalChecker {

	/**
	 * ロガー.
	 */
	private static H5CodeAssistPluginLogger logger = H5CodeAssistPluginLoggerFactory
			.getLogger(AbstractSuffixProposalChecker.class);

	/**
	 * コード補完時の文字列.
	 */
	private String codeAssistStr;

	/**
	 * 設定ファイルビーン.
	 */
	private ObjectLiteralBean bean;

	/**
	 * コード補完ノード.
	 */
	private CompletionOnMemberAccess memberAccess;

	/**
	 * コンストラクタ.
	 * 
	 * @param unit ユニット
	 * @param project プロジェクト.
	 * @param bean 設定ファイルビーン.
	 * @throws JavaScriptModelException 生成例外.
	 */
	public AbstractSuffixProposalChecker(IJavaScriptUnit unit, IJavaScriptProject project, ObjectLiteralBean bean)
			throws JavaScriptModelException {

		super(unit, project);
		this.bean = bean;
	}

	@Override
	protected boolean doCheckCodeAssist(CompilationUnitDeclaration unitDeclaration) {

		return suffixCheckCodeAssist(unitDeclaration, bean, bean.getRegExPattern());
	}

	/**
	 * サフィックス一致を評価するチェッカメソッド(主にコントローラ、ロジック).
	 * 
	 * @param unitDeclaration 構文木ルート
	 * @param suffixBean オプションファイルビーン.
	 * @param suffixPattern サフィックスパターン.
	 * @return コード補完を生成する必要があるかどうか.
	 */
	boolean suffixCheckCodeAssist(CompilationUnitDeclaration unitDeclaration, ObjectLiteralBean suffixBean,
			Pattern suffixPattern) {

		// サフィックス一致のノードを収集
		NodeCollector collector = NodeCollectorFactory.createNodeCollector(suffixBean);
		// unitDeclarationをウォークしサフィックス一致のノードを収集する.
		collector.collect(unitDeclaration);
		// コレクターからコード補完情報を取得
		SuffixAssistNodeInfo info = CheckerUtils.getAssistNodeInfo(collector, unitDeclaration);
		// コード補完呼び出しのコードがnullだったらfalse
		memberAccess = info.getMemberAccess();
		if (memberAccess == null) {
			return false;
		}
		// codeAssistをチェックする.
		boolean checkCodeAssistNodeFlg = CheckerUtils.checkCodeAssistNode(suffixBean, memberAccess);
		logger.log(Messages.DB0004, checkCodeAssistNodeFlg);
		if (!checkCodeAssistNodeFlg) {
			return false;
		}
		// コード補完時の文字列
		this.codeAssistStr = memberAccess.getReceiver().toString();
		logger.log(Messages.DB0005, this.codeAssistStr);
		String firstSegMemberAccess = CheckerUtils.getRootObject(codeAssistStr);
		// 大元のオブジェクトがサフィックス(xxxController等)一致していた場合
		if (suffixPattern.matcher(firstSegMemberAccess).matches()) {
			logger.log(Messages.DB0006);
			return checkParentObjController() && checkCodeAssistNodeFlg;
		}
		// 大元のオブジェクトがthisだった場合
		if (StringUtils.equals(firstSegMemberAccess, "this")) {
			logger.log(Messages.DB0007);
			return checkParentObjThis(info) && checkCodeAssistNodeFlg;
		}
		// それ以外だった場合.
		logger.log(Messages.DB0008);
		return checkOtherCase(info);

	}

	/**
	 * コード補完の大元オブジェクトがthisにもサフィックスにも当てはまらない場合.<br>
	 * 親オブジェクトがサフィックスと一致したらそのオブジェクトの<br>
	 * ダミーコード生成情報を生成する.
	 * 
	 * @param info コントローラビジターが取得した情報.
	 * @return コード補完が必要かどうか.
	 */
	private boolean checkOtherCase(SuffixAssistNodeInfo info) {

		if (info.getTargetNodes() == null || info.getTargetNodes().length == 0) {
			return false;
		}
		return checkParentObj(info.getTargetNodes());
	}

	/**
	 * コード補完呼び出しの大元オブジェクトがthisだった場合の処理.<br>
	 * 親オブジェクトをたどりサフィックスが一致したオブジェクトのダミーコード情報を<br>
	 * 生成する.
	 * 
	 * @param info コントローラビジターが取得した情報.
	 * @return コード補完が必要かどうか.
	 */
	private boolean checkParentObjThis(SuffixAssistNodeInfo info) {

		return checkParentObj(info.getTargetNodes());

	}

	/**
	 * ノードを走査して、定義名がサフィックスと同等だったらtrueを返し<br>
	 * サフィックスが同値だったオブジェクトのダミーコード生成情報フィールドを<br>
	 * 生成する.<br>
	 * 定義名がサフィックスと同じものがない場合はfalseを返す.
	 * 
	 * @param targetNodes 検査対象ノード.
	 * @return コード補完を生成する必要があるかどうか.
	 */
	private boolean checkParentObj(IASTNode[] targetNodes) {

		// コード補完が必要かどうか.
		boolean check = false;
		Pattern suffixPattern = getBean().getRegExPattern();
		for (IASTNode astNode : targetNodes) {
			int insertPosition = CheckerUtils.getInitializerSourceEnd(suffixPattern, astNode);
			if (insertPosition > 0) {
				this.addDummyCodeInfoList(createDummyCodeInfo(insertPosition, CodeBuilderType.OBJ_LITERAL));
				check = true;
			}
		}
		return check;
	}

	/**
	 * ダミーコード情報を生成する.
	 * 
	 * @param insertPosition 挿入位置.
	 * @param objLiteral ビルダータイプ.
	 * @return ダミーコード情報.
	 */
	private DummyCodeInfo createDummyCodeInfo(int insertPosition, CodeBuilderType objLiteral) {

		return new DummyCodeInfo(insertPosition, objLiteral);
	}

	/**
	 * オブジェクト名がサフィックスと一致した場合のチェック処理.<br>
	 * コード補完が必要な場合はダミーコード生成情報のフィールドを生成しtrueを返す.<br>
	 * それ以外はfalse.
	 * 
	 * @return コード補完を生成する必要があるかどうか.
	 */
	private boolean checkParentObjController() {

		String addedObjectName = null;
		Pattern suffixPattern = getBean().getRegExPattern();
		if (suffixPattern.matcher(this.getCodeAssistStr()).matches()) {
			addedObjectName = this.getCodeAssistStr();
		} else {
			addedObjectName = StringUtils.substringBeforeLast(this.getCodeAssistStr().toString(), ".");
		}
		this.addDummyCodeInfoList(new DelegateDummyCodeInfo(memberAccess.sourceEnd() + 1, addedObjectName,
				CodeBuilderType.REFERENCE_OBJ));
		// }
		return true;
	}

	@Override
	public String getCodeAssistStr() {

		return codeAssistStr;
	}

	@Override
	protected ObjectLiteralBean getBean() {

		return bean;
	}

	@Override
	CompletionOnMemberAccess getMemberAccess() {

		return memberAccess;
	}

}
