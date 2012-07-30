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
 */
package com.htmlhifive.tools.wizard.ui.page;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.htmlhifive.tools.wizard.PluginConstant;
import com.htmlhifive.tools.wizard.RemoteContentManager;
import com.htmlhifive.tools.wizard.library.model.LibraryList;
import com.htmlhifive.tools.wizard.library.model.xml.BaseProject;
import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The page will only
 * accept file name without the extension OR with the extension that matches the expected one (mpe).
 */

public class LibraryImportPage extends WizardPage {

	/** container. */
	LibraryImportComposite container;

	/**
	 * コンストラクタ.
	 * 
	 * @param pageName ページ名
	 */
	public LibraryImportPage(String pageName) {

		super(pageName);
		setMessage(UIMessages.LibraryImportPage_this_message);
		setTitle(UIMessages.LibraryImportPage_this_title);
		setPageComplete(true); // 別に設定不要なので
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		container = new LibraryImportComposite(parent, SWT.NONE);
		setControl(container);

		// 初期化.
		container.initialize(null);

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {

		String errorMessage = getWizard().getPage(PluginConstant.JavaProjectWizardFirstPageName).getErrorMessage();
		if (errorMessage != null) {
			setErrorMessage(errorMessage);
		}

		return super.canFlipToNextPage();
	}

	@Override
	public void setVisible(boolean visible) {

		super.setVisible(visible);

		if (visible) {

			// libraryListのnull対応.
			LibraryList libraryList = RemoteContentManager.getLibraryList();
			if (libraryList != null) {
				BaseProject baseProject = ((StructureSelectPage) getPreviousPage()).getBaseProject();
				if (baseProject != null) {
					if (!StringUtils.equals(libraryList.getDefaultJsLibPath(), baseProject.getDefaultJsLibPath())) {
						libraryList.setDefaultJsLibPath(baseProject.getDefaultJsLibPath());
						//container.refreshTreeLibrary(false, false);
						container.initialize(null);
					}
				}
			}
		}
	}
}