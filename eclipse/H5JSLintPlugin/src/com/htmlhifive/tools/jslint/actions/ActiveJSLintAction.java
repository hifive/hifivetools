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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;

import com.htmlhifive.tools.jslint.JSLintPluginNature;
import com.htmlhifive.tools.jslint.dialog.StatusList;

/**
 * JSLintのアクティブ/非アクティブを切り替えるアクションクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ActiveJSLintAction extends AbstractJavaScriptAction {

	/**
	 * ネーチャー追加フラグ.
	 */
	private Boolean addflg = null;

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.actions.AbstractJavaScriptAction#doRun(org
	 * .eclipse.jface.action.IAction, org.eclipse.core.runtime.MultiStatus)
	 */
	@Override
	protected void doRun(final IAction action, StatusList statusList) {

		action.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				addflg = (Boolean) event.getNewValue();
				action.setChecked((Boolean) event.getNewValue());
			}
		});
		IResource resource = getResource();
		IProject project = null;
		if (resource instanceof IProject) {
			project = (IProject) resource;
			try {
				IProjectDescription description = project.getDescription();
				String[] natures = description.getNatureIds();
				if (addflg == null || addflg) {
					action.setChecked(true);
					for (int i = 0; i < natures.length; ++i) {
						if (JSLintPluginNature.NATURE_ID.equals(natures[i])) {
							return;
						}
					}
					// ネーチャー追加処理.
					String[] newNatures = new String[natures.length + 1];
					System.arraycopy(natures, 0, newNatures, 0, natures.length);
					newNatures[natures.length] = JSLintPluginNature.NATURE_ID;
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
				} else {
					for (int i = 0; i < natures.length; ++i) {
						if (JSLintPluginNature.NATURE_ID.equals(natures[i])) {
							// ネーチャー削除処理.
							String[] newNatures = new String[natures.length - 1];
							System.arraycopy(natures, 0, newNatures, 0, i);
							System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
							description.setNatureIds(newNatures);
							project.setDescription(description, null);
							return;
						}
					}
				}
			} catch (CoreException e) {
				statusList.add(e.getStatus());
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
			e.printStackTrace();
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
