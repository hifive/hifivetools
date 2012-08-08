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
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wst.jsdt.internal.ui.wizards.JavaProjectWizardFirstPage;
import org.eclipse.wst.jsdt.internal.ui.wizards.NewWizardMessages;

import com.htmlhifive.tools.wizard.RemoteContentManager;
import com.htmlhifive.tools.wizard.library.LibraryList;
import com.htmlhifive.tools.wizard.library.xml.BaseProject;
import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.UIEventHelper;
import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * ディレクトリ構造を決定するウィザードのページ.
 * 
 * @author fkubo
 */
public class StructureSelectPage extends WizardPage {
	/** ロガー. */
	private static PluginLogger logger = PluginLoggerFactory.getLogger(StructureSelectPage.class);

	/** container. */
	private StructureSelectComposite container;

	/** changeProject. */
	private boolean changeProject = true;

	/** initFlag. */
	private boolean initFlag = false;

	/**
	 * コンストラクタ.
	 * 
	 * @param pageName ページ名
	 */
	public StructureSelectPage(String pageName) {

		super(pageName);

		logger.log(Messages.TR0011, getClass().getSimpleName(), "<init>");

		setDescription("");
		setMessage(UIMessages.StructureSelectPage_this_message);
		setTitle(UIMessages.StructureSelectPage_this_title);
		//setPageComplete(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControl(Composite parent) {

		logger.log(Messages.TR0011, getClass().getSimpleName(), "createControl");

		container = new StructureSelectComposite(parent, SWT.NONE);
		setControl(container);

		// 下からのメッセージを受ける.
		container.addListener(UIEventHelper.SET_MESSAGE, new Listener() {

			@Override
			public void handleEvent(Event event) {

				// メッセージを設定.
				setErrorMessage(event.text); // WizardPage

				// 次に遷移できるかどうかを設定
				setPageComplete(event.text == null);
			}
		});
		container.addListener(UIEventHelper.PROJECT_CHANGE, new Listener() {

			@Override
			public void handleEvent(Event event) {

				JavaProjectWizardFirstPage javaProjectWizardFirstPage = (JavaProjectWizardFirstPage) getWizard()
						.getPage(NewWizardMessages.JavaProjectWizardFirstPage_page_pageName);

				// プロジェクト名を設定する.
				javaProjectWizardFirstPage.setName(getProjectName());
			}
		});

		// ページ初期表示時の処理.
		((IPageChangeProvider) getContainer()).addPageChangedListener(new IPageChangedListener() {
			@Override
			public void pageChanged(PageChangedEvent event) {

				if (!initFlag && event.getSelectedPage() == StructureSelectPage.this
						&& event.getSource() == getContainer()) {
					// 初期プロジェクト名.
					container.setProjectName("hifive-web");

					// チェック.
					container.setInputComboZip();
					initFlag = true;
				}
				StructureSelectPage.this.container.validatePage();
			}
		});

		// ページ切替時の処理.
		((WizardDialog) getContainer()).addPageChangingListener(new IPageChangingListener() {

			@Override
			public void handlePageChanging(PageChangingEvent event) {

				//				// 画面表示時.
				//				if (!initFlag && event.getCurrentPage() != getNextPage()
				//						&& event.getTargetPage() == StructureSelectPage.this) {
				//					// 初期プロジェクト名.
				//					container.setProjectName("hifive-web");
				//
				//					// チェック.
				//					container.setInputComboZip();
				//					initFlag = true;
				//					return;
				//				}
				// 次のページ遷移時.
				if (event.getCurrentPage() == StructureSelectPage.this && event.getTargetPage() == getNextPage()) {

					if (StructureSelectPage.this.changeProject) {
						ConfirmLicensePage confirmLicensePage = (ConfirmLicensePage) getWizard().getPage(
								"confirmLicensePage");
						LibraryImportPage libraryImportPage = (LibraryImportPage) getWizard().getPage(
								"libraryImportPage");
						//						JavaProjectWizardFirstPage javaProjectWizardFirstPage = (JavaProjectWizardFirstPage) getWizard()
						//								.getPage(NewWizardMessages.JavaProjectWizardFirstPage_page_pageName);

						//						// プロジェクト名を設定する.
						//						javaProjectWizardFirstPage.setName(getProjectName());

						BaseProject baseProject = getBaseProject();
						if (baseProject != null) {
							if (libraryImportPage.initialize(null, getProjectName(), baseProject.getDefaultJsLibPath())) {
								// 変更あり.
								confirmLicensePage.clearCategory();

								StructureSelectPage.this.changeProject = false;
							}
						}
					}
				}

			}
		});

		//		// 初期プロジェクト名.
		//		container.setProjectName("hifive-web");
		//
		//		// チェック.
		//		container.setInputComboZip();
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
