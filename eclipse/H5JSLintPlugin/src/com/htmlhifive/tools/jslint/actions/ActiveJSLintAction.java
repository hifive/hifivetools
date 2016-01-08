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
package com.htmlhifive.tools.jslint.actions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.htmlhifive.tools.jslint.JSLintPlugin;
import com.htmlhifive.tools.jslint.JSLintPluginNature;
import com.htmlhifive.tools.jslint.dialog.StatusList;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * JSLintのアクティブ/非アクティブを切り替えるアクションクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ActiveJSLintAction extends AbstractJavaScriptAction {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(ActiveJSLintAction.class);

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.actions.AbstractJavaScriptAction#doRun(org
	 * .eclipse.jface.action.IAction, org.eclipse.core.runtime.MultiStatus)
	 */
	@Override
	protected void doRun(final IAction action, StatusList statusList) {

		IResource resource = getResource();
		IProject project = null;
		if (resource instanceof IProject) {
			project = (IProject) resource;
			// try {
			if (JSLintPlugin.hasJSLintNature(project)) {
				// natureが既にある場合は削除する.
				JSLintPlugin.removeJSLintNature(project);
				// 削除したのでチェックは外す.
				action.setChecked(false);

			} else {
				// ない場合は追加する.
				JSLintPlugin.addJSLintNature(project);
				// 追加したのでチェックをつける.
				action.setChecked(true);
			}
		}
	}

	/**
	 * 自動的にビルドするかどうかをチェックする.
	 * 
	 * @param action アクション
	 */
	private void checkAutoBuild(IAction action) {

		if (getResource() == null) {
			action.setChecked(true);
			return;
		}
		try {
			String[] ids = getResource().getProject().getDescription().getNatureIds();
			for (String id : ids) {
				if (StringUtils.equals(id, JSLintPluginNature.NATURE_ID)) {
					action.setChecked(true);
					return;
				}
			}
			action.setChecked(false);
		} catch (CoreException e) {
			logger.put(Messages.EM0011, e, getResource().getProject().getName());
		}

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.actions.AbstractJavaScriptAction#selectionChanged
	 * (org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		super.selectionChanged(action, selection);
		checkAutoBuild(action);
	}

}
