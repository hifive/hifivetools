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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.htmlhifive.tools.wizard.RemoteContentManager;
import com.htmlhifive.tools.wizard.library.model.LibraryList;
import com.htmlhifive.tools.wizard.library.model.xml.BaseProject;
import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * ディレクトリ構造を決定するウィザードのページ.
 * 
 * @author fkubo
 */
public class StructureSelectPage extends WizardPage {

	/** container. */
	private StructureSelectComposite container;

	/**
	 * コンストラクタ.
	 * 
	 * @param pageName ページ名
	 */
	public StructureSelectPage(String pageName) {

		super(pageName);
		setDescription("");
		setMessage(UIMessages.StructureSelectPage_this_message);
		setTitle(UIMessages.StructureSelectPage_this_title);
		setPageComplete(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {

		container = new StructureSelectComposite(parent, SWT.NONE);
		setControl(container);

		// 下からのメッセージを受ける.
		container.addListener(SWT.ERROR_UNSPECIFIED, new Listener() {

			@Override
			public void handleEvent(Event event) {

				// メッセージを設定.
				setErrorMessage(event.text); // WizardPage

				setPageComplete(event.text == null);
			}
		});

		// チェック.
		container.validatePage();

		// 初期プロジェクト名.
		container.setProjectName("hifive-web");
	}

	/**
	 * Creates a project resource handle for the current project name field value. The project handle is created
	 * relative to the workspace root.
	 * <p>
	 * This method does not create the project resource; this is the responsibility of <code>IProject::create</code>
	 * invoked by the new project resource wizard.
	 * </p>
	 * 
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {

		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}

	/**
	 * プロジェクト名を取得する.
	 * 
	 * @return プロジェクト名
	 */
	public String getProjectName() {

		return container.textProject.getText().trim();
	}

	/**
	 * 選択済のベースのプロジェクト.
	 * 
	 * @return 選択済のベースのプロジェクト.
	 */
	public BaseProject getBaseProject() {

		// libraryListのnull対応.
		LibraryList libraryList = RemoteContentManager.getLibraryList();
		if (libraryList == null) {
			return null;
		}
		return libraryList.getInfoBaseProjectMap().get(container.comboZip.getText());
	}
}
