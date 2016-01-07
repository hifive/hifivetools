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
package com.htmlhifive.tools.jslint.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.htmlhifive.tools.jslint.JSLintPlugin;
import com.htmlhifive.tools.jslint.dialog.StatusList;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;
import com.htmlhifive.tools.jslint.parse.JsParserFactory;
import com.htmlhifive.tools.jslint.parse.Parser;

/**
 * javaScriptコードチェック選択時のアクションクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CheckJavaScriptAction extends AbstractJavaScriptAction {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(CheckJavaScriptAction.class);

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.actions.AbstractJavaScriptAction#doRun(org
	 * .eclipse.jface.action.IAction, org.eclipse.core.runtime.MultiStatus)
	 */
	@Override
	public void doRun(IAction action, final StatusList statusList) {

		IProgressService service = PlatformUI.getWorkbench().getProgressService();
		try {
			service.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InterruptedException {

					Parser parse = JsParserFactory.createParser(getResource());
					try {
						parse.parse(monitor);
					} catch (CoreException e) {
						if (e.getCause() != null) {
							logger.put(Messages.EM0100, e.getCause());
						}
						statusList.add(e.getStatus());
					}

				}
			});
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.ui.views.ProblemView");
		} catch (InvocationTargetException e) {
			logger.put(Messages.EM0001, e);
			statusList.add(new Status(IStatus.ERROR, JSLintPlugin.PLUGIN_ID, Messages.EM0100.getText(), e));
		} catch (InterruptedException e) {
			// ignore
		} catch (PartInitException e) {
			logger.put(Messages.EM0100, e);
			statusList.add(e.getStatus());
		}

	}
}
