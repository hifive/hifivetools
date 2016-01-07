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
package com.htmlhifive.tools.codeassist.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.osgi.framework.BundleContext;

import com.htmlhifive.tools.codeassist.core.config.ConfigFileParser;
import com.htmlhifive.tools.codeassist.core.config.ConfigFileParserFactory;
import com.htmlhifive.tools.codeassist.core.config.bean.AllBean;
import com.htmlhifive.tools.codeassist.core.exception.ParseException;
import com.htmlhifive.tools.codeassist.core.exception.ProposalCreateException;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.core.messages.Messages;
import com.htmlhifive.tools.codeassist.core.proposal.H5ProposalCreater;
import com.htmlhifive.tools.codeassist.core.proposal.ProposalContext;

/**
 * コードアシストコアプラグインのアクティベータクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class H5CodeAssistCorePlugin extends Plugin {
	/**
	 * プラグインIDを取得する.
	 */
	public static final String PLUGIN_ID = "com.htmlhifive.tools.codeassist.core.Hi5CodeAssistCorePlugin";
	/**
	 * ロガー.
	 */
	private static H5CodeAssistPluginLogger logger;

	/**
	 * プラグイン.
	 */
	private static H5CodeAssistCorePlugin plugin;

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		super.start(bundleContext);
		plugin = this;
		Properties prop = new Properties();
		prop.load(this.getClass().getClassLoader().getResourceAsStream("/log4j.properties"));
		PropertyConfigurator.configure(prop);
		logger = H5CodeAssistPluginLoggerFactory.getLogger(H5CodeAssistCorePlugin.class);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		plugin = null;
		super.stop(bundleContext);
	}

	/**
	 * プラグインを取得する.
	 * 
	 * @return プラグイン.
	 */
	public static H5CodeAssistCorePlugin getDefault() {

		return plugin;
	}

	/**
	 * オプションファイルから指定したサフィックスのコード補完情報を取得する.<br>
	 * optionファイルがnullの場合はデフォルトの補完が情報が利用される.
	 * 
	 * @param context 補完対象javascriptファイルの補完情報.
	 * @param monitor モニター.
	 * @param option オプションファイル.
	 * @return コード補完情報.
	 * @throws CoreException 例外
	 */
	public List<ICompletionProposal> getCompletionProposals(ProposalContext context, IProgressMonitor monitor,
			IFile option) throws CoreException {

		InputStream is = null;
		String fileExtension = H5CodeAssistCorePluginConst.EXTENTION_XML;
		String fileName = "default";
		if (option == null) {
			is = this.getClass().getResourceAsStream("/h5-code-assist.xml");
		} else {
			is = option.getContents();
			fileExtension = option.getFileExtension();
			fileName = option.getName();
		}
		return getCompletionProposals(context, monitor, is, fileExtension, fileName);
	}

	/**
	 * オプションファイルから指定したサフィックスのコード補完情報を取得する.
	 * 
	 * @param context 補完対象javascriptファイルの補完情報.
	 * @param monitor モニター.
	 * @param option オプションファイル.
	 * @return コード補完情報.
	 * @throws CoreException 例外
	 */
	private List<ICompletionProposal> getCompletionProposals(ProposalContext context, IProgressMonitor monitor,
			InputStream is, String fileExtension, String fileName) throws CoreException {

		try {
			ConfigFileParser parser = ConfigFileParserFactory.createParser(is, fileExtension);
			AllBean bean = parser.getCodeAssistBean();
			List<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
			H5ProposalCreater creater = new H5ProposalCreater(context, bean);
			resultList.addAll(creater.createProposal());
			return resultList;
		} catch (ParseException e) {
			logger.log(Messages.EM0001, e, fileName);
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.EM0001.format(fileName)));
		} catch (ProposalCreateException e) {
			logger.log(Messages.EM0002, e);
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.EM0002.getText()));
		} finally {
			IOUtils.closeQuietly(is);
		}

	}
}
