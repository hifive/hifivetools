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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

import com.htmlhifive.tools.jslint.JSLintPluginConstant;
import com.htmlhifive.tools.jslint.dialog.StatusList;
import com.htmlhifive.tools.jslint.parse.ParserManager;

/**
 * 
 * マーカの削除アクション.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ClearJavaScriptAction extends AbstractJavaScriptAction {

	/**
	 * Constructor for Action1.
	 */
	public ClearJavaScriptAction() {

		super();
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void doRun(IAction action, StatusList statusList) {

		try {
			ParserManager.cancelCurrentParser();
			getResource().deleteMarkers(JSLintPluginConstant.JS_TYPE_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			statusList.add(e.getStatus());
		}
	}

}
