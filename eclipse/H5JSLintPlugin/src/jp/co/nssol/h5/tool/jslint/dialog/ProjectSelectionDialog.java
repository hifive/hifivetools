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
 package jp.co.nssol.h5.tool.jslint.dialog;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * リソース選択ダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ProjectSelectionDialog extends ElementListSelectionDialog {

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param parent シェル
	 * @param title ダイアログタイトル
	 * @param message ダイアログメッセージ
	 */
	public ProjectSelectionDialog(Shell parent, String title, String message) {

		super(parent, new WorkbenchLabelProvider());
		setTitle(title);
		setMessage(message);
		setElements(ResourcesPlugin.getWorkspace().getRoot().getProjects());

	}

}
