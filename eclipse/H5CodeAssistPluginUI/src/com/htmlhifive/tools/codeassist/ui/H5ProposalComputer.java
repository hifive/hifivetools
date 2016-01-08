/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.htmlhifive.tools.codeassist.ui;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.wst.jsdt.ui.text.java.JavaContentAssistInvocationContext;

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePlugin;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.core.proposal.ProposalContext;
import com.htmlhifive.tools.codeassist.ui.config.CodeAssistConfigManager;
import com.htmlhifive.tools.codeassist.ui.context.H5ProposalContext;
import com.htmlhifive.tools.codeassist.ui.messages.UIMessages;

/**
 * Hi5用コード補完生成コンピュータ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class H5ProposalComputer implements IJavaCompletionProposalComputer {

	/**
	 * ロガー.
	 */
	private H5CodeAssistPluginLogger logger = H5CodeAssistPluginLoggerFactory.getLogger(H5ProposalComputer.class);

	/**
	 * コンストラクタ.
	 */
	public H5ProposalComputer() {

	}

	@Override
	public void sessionStarted() {

	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		if (!(context instanceof JavaContentAssistInvocationContext)) {
			return Collections.emptyList();
		}
		JavaContentAssistInvocationContext jsContext = (JavaContentAssistInvocationContext) context;
		try {
			// オプションファイルの取得
			String path = CodeAssistConfigManager.getConfig(jsContext.getProject().getProject(), true).getConfigBean()
					.getOptionFilePath();
			IFile option = null;
			if (StringUtils.isNotEmpty(path)) {
				option = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
			}
			ProposalContext hi5Context = new H5ProposalContext(jsContext);
			List<ICompletionProposal> resultList = H5CodeAssistCorePlugin.getDefault().getCompletionProposals(
					hi5Context, monitor, option);
			return resultList;
		} catch (CoreException e) {
			logger.log(UIMessages.UIEM0003);
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					UIMessages.UIDT0002.getText(), UIMessages.UIEM0003.getText(), e.getStatus());
		}
		return Collections.emptyList();
	}

	@Override
	public List<ICompletionProposal> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		return Collections.emptyList();
	}

	@Override
	public String getErrorMessage() {

		return null;
	}

	@Override
	public void sessionEnded() {

	}

}
