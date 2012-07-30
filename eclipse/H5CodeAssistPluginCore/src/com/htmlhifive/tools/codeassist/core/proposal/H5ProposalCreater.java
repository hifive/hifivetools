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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

import com.htmlhifive.tools.codeassist.core.config.bean.AllBean;
import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.ObjectLiteralBean;
import com.htmlhifive.tools.codeassist.core.config.bean.RootChildrenElem;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;
import com.htmlhifive.tools.codeassist.core.exception.ProposalCheckException;
import com.htmlhifive.tools.codeassist.core.exception.ProposalCreateException;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.core.messages.Messages;
import com.htmlhifive.tools.codeassist.core.proposal.build.CodeBuilder;
import com.htmlhifive.tools.codeassist.core.proposal.build.CodeBuilderFactory;
import com.htmlhifive.tools.codeassist.core.proposal.checker.CheckerFactory;
import com.htmlhifive.tools.codeassist.core.proposal.checker.DummyCodeInfo;
import com.htmlhifive.tools.codeassist.core.proposal.checker.ProposalChecker;
import com.htmlhifive.tools.codeassist.core.proposal.wrapper.ProposalWrapper;
import com.htmlhifive.tools.codeassist.core.proposal.wrapper.ProposalWrapperFactory;

/**
 * 一つのオプションファイルの定義から、コード補完を生成する.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class H5ProposalCreater extends AbstractProposalCreator {

	/**
	 * ロガー.
	 */
	private static H5CodeAssistPluginLogger logger = H5CodeAssistPluginLoggerFactory.getLogger(H5ProposalCreater.class);

	/**
	 * オプションファイルから取得したHi5用コード補完情報.
	 */
	private AllBean bean;

	/**
	 * コンストラクタ.
	 * 
	 * @param context コード補完コンテキスト.
	 * @param bean オプションファイルから取得したHi5用コード補完情報.
	 */
	public H5ProposalCreater(ProposalContext context, AllBean bean) {

		super(context);
		this.bean = bean;
	}

	@Override
	public List<ICompletionProposal> createProposal() throws ProposalCreateException {

		try {
			ProposalChecker checker = null;
			RootChildrenElem targetElem = null;
			RootChildrenElem[] allElem = bean.getElemList();
			for (RootChildrenElem rootChildrenElem : allElem) {

				checker = CheckerFactory.createChecker(getContext().getCompilationUnit(), getContext().getProject(),
						rootChildrenElem);
				boolean createFlg = checker.check(getContext().getInvocationOffset());
				if (createFlg) {
					targetElem = rootChildrenElem;
					break;
				}
			}
			if (targetElem == null) {
				logger.log(Messages.DB0001, false);
				return Collections.emptyList();
			}
			logger.log(Messages.DB0001, true);
			// 振り分け
			if (targetElem instanceof ObjectLiteralBean) {
				return createProposalForObjectLiteralBean((ObjectLiteralBean) targetElem,
						checker.existDefaultCodeAssist(), checker.getDummyCodeInfo());
			}

		} catch (JavaScriptModelException e) {
			logger.log(Messages.EM0005, e);
		} catch (ProposalCheckException e) {
			logger.log(Messages.EM0006, e);
		}
		return Collections.emptyList();
	}

	/**
	 * コード生成オプションがオブジェクトリテラルで表す補完情報のときのコード生成メソッド.
	 * 
	 * 
	 * @param objBean コード補完情報.
	 * @param existDefaultCodeAssist デフォルトのコード補完が存在するかどうか.
	 * @param codeInfo ダミーコード生成情報.
	 * @return コード補完リスト.
	 * @throws JavaScriptModelException 生成例外.
	 */
	private List<ICompletionProposal> createProposalForObjectLiteralBean(ObjectLiteralBean objBean,
			boolean existDefaultCodeAssist, DummyCodeInfo[] codeInfo) throws JavaScriptModelException {

		List<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		ProposalContext context = getContext();

		// ダミーコード生成用.
		StringBuilder sb = new StringBuilder();
		IJavaScriptUnit targetUnit = context.getCompilationUnit();
		IPackageFragment fragment = context.getProject().getPackageFragments()[0];
		// 疑似jsファイル.
		IJavaScriptUnit tempUnit = fragment.getJavaScriptUnit(NAME_TEMPJSFILE)
				.getWorkingCopy(new NullProgressMonitor());
		// ダミーに実コードを追加.
		sb.append(targetUnit.getBuffer().getContents());
		for (DummyCodeInfo dummyCodeInfo : codeInfo) {
			CodeBuilder builder = CodeBuilderFactory.createCodeBuilder(dummyCodeInfo);
			addObject(builder, objBean);
			builder.build(sb, dummyCodeInfo.getInsertPosition());
		}
		tempUnit.getBuffer().setContents(sb.toString());
		CompletionProposalCollector collector = new CompletionProposalCollector(tempUnit);
		tempUnit.codeComplete(context.getInvocationOffset(), collector);
		IJavaCompletionProposal[] proposals = collector.getJavaCompletionProposals();
		logger.log(Messages.DB0002, existDefaultCodeAssist);
		if (existDefaultCodeAssist) {
			// デフォルトのコード補完と被らないように定義したメソッドのみ抽出する.
			for (IJavaCompletionProposal proposal : proposals) {
				addOptionProposal(resultList, proposal, objBean);
			}
		} else {
			for (IJavaCompletionProposal proposal : proposals) {
				if (!addOptionProposal(resultList, proposal, objBean)) {
					resultList.add(proposal);
				}
			}
		}
		return resultList;
	}

	/**
	 * コードビルダーに要素を追加する.
	 * 
	 * @param builder 追加されるビルダー.
	 * @param objBean 要素.
	 */
	private void addObject(CodeBuilder builder, ObjectLiteralBean objBean) {

		for (FunctionBean elem : objBean.getFunctions()) {
			builder.addFunction(elem);
		}
		for (VarReferenceBean elem : objBean.getVarRefs()) {
			builder.addField(elem);
		}

	}

	/**
	 * 補完情報がオプションファイルで設定した補完情報だった場合指定したリストに追加する.
	 * 
	 * @param resultList 追加されるリスト.
	 * @param proposal 対象プロポーザル
	 * @param objBean オプションファイル定義情報.
	 * @return 追加されたらtrue,されなければfalse.
	 */
	private boolean addOptionProposal(List<ICompletionProposal> resultList, IJavaCompletionProposal proposal,
			ObjectLiteralBean objBean) {

		ProposalWrapper propWrapper = null;
		for (FunctionBean elem : objBean.getFunctions()) {
			if (isOptionFileProposal(proposal, elem)) {
				propWrapper = ProposalWrapperFactory.createProposalWrapper(proposal, elem);
				logger.log(Messages.DB0003, propWrapper.getDisplayString());
				resultList.add(propWrapper);
				return true;
			}
		}
		for (VarReferenceBean elem : objBean.getVarRefs()) {
			if (isOptionFileProposal(proposal, elem)) {
				propWrapper = ProposalWrapperFactory.createProposalWrapper(proposal);
				logger.log(Messages.DB0003, propWrapper.getDisplayString());
				resultList.add(propWrapper);
				return true;
			}
		}
		return false;
	}

	/**
	 * 指定したプロポーザルがオプションファイルで指定されたものかどうか.
	 * 
	 * @param proposal 補完候補
	 * @param elem 設定ファイルの要素
	 * @return オプションファイルで指定されたものかどうか
	 */
	private boolean isOptionFileProposal(IJavaCompletionProposal proposal, VarReferenceBean elem) {

		return StringUtils.startsWith(proposal.getDisplayString(), elem.getKey());
	}

	/**
	 * 指定したプロポーザルがオプションファイルで指定されたものかどうか.
	 * 
	 * @param proposal 補完候補
	 * @param elem 設定ファイルの要素
	 * @return オプションファイルで指定されたものかどうか
	 */
	private boolean isOptionFileProposal(IJavaCompletionProposal proposal, FunctionBean elem) {

		Pattern pattern = Pattern.compile(Pattern.quote(elem.getName()) + "\\(.*\\).*");
		return pattern.matcher(proposal.getDisplayString()).matches();
	}

}
