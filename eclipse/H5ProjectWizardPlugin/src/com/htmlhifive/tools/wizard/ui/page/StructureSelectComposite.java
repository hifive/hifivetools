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
 */
package com.htmlhifive.tools.wizard.ui.page;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import com.htmlhifive.tools.wizard.RemoteContentManager;
import com.htmlhifive.tools.wizard.library.LibraryList;
import com.htmlhifive.tools.wizard.library.xml.Info;
import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.UIEventHelper;
import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * <H3>ディレクトリ構造を決定するウィザードのコンポジット.</H3>
 * 
 * @author fkubo
 */
public class StructureSelectComposite extends Composite {
	/** ロガー. */
	private static PluginLogger logger = PluginLoggerFactory.getLogger(StructureSelectComposite.class);

	final Combo comboZip;
	private final Button buttonReload;

	private final Link linkInfo;
	final Text textProject;
	private final Label lblNewLabel;
	private final Label label;
	private final Label lblInfo;
	private final Label lblListInfo;

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット
	 * @param style スタイル
	 */
	public StructureSelectComposite(Composite parent, int style) {

		super(parent, style);

		logger.log(Messages.TR0001, getClass().getSimpleName(), "<init>");

		setLayout(new GridLayout(2, false));

		final GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.CENTER;

		lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText(UIMessages.StructureSelectComposite_lblNewLabel_text);

		textProject = new Text(this, SWT.BORDER);
		textProject.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				do_textProject_modifyText(e);
			}
		});

		textProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		label.setText("");

		GridLayout gridLayout1 = new GridLayout(3, false);

		Group structureGroup = new Group(this, SWT.NONE);
		structureGroup.setText(UIMessages.StructureSelectComposite_structureGroup_text);
		structureGroup.setLayout(gridLayout1);
		structureGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 2, 1));

		lblInfo = new Label(structureGroup, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblInfo.setText(UIMessages.StructureSelectComposite_lblInfo_text);
		new Label(structureGroup, SWT.NONE);

		lblListInfo = new Label(structureGroup, SWT.NONE);
		lblListInfo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));

		comboZip = new Combo(structureGroup, SWT.READ_ONLY);
		comboZip.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				do_comboZip_widgetSelected(e);
			}
		});
		comboZip.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		buttonReload = new Button(structureGroup, SWT.NONE);
		buttonReload.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				do_buttonReload_widgetSelected(e);
			}
		});
		buttonReload.setText(UIMessages.StructureSelectComposite_buttonReload_text);
		new Label(structureGroup, SWT.NONE);

		linkInfo = new Link(structureGroup, SWT.NONE);
		linkInfo.setText("");
		linkInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		linkInfo.setVisible(false);

		// 開始後、ページ経由で初期設定されるので、ここでは呼ばない.
		// 初期設定(初期表示なのでここでOK).
		//setInputComboZip();

		//textProject.setFocus();
	}

	/**
	 * Creates a project resource handle for the current project name field value.
	 * <p>
	 * This method does not create the project resource; this is the responsibility of <code>IProject::create</code>
	 * invoked by the new project resource wizard.
	 * </p>
	 * 
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {

		return ResourcesPlugin.getWorkspace().getRoot().getProject(textProject.getText());
	}

	// イベント処理系

	/**
	 * プロジェクト名を設定する.
	 * 
	 * @param name プロジェクト名
	 */
	public void setProjectName(String name) {

		logger.log(Messages.TR0001, getClass().getSimpleName(), "setProjectName");

		textProject.setText(name);
		textProject.setSelection(textProject.getText().length());
		textProject.setFocus();

	}

	/**
	 * 再読込ボタン処理.
	 * 
	 * @param e イベント
	 */
	protected void do_buttonReload_widgetSelected(SelectionEvent e) {

		logger.log(Messages.TR0001, getClass().getSimpleName(), "do_buttonReload_widgetSelected");

		RemoteContentManager.getLibraryList(true);

		setInputComboZip();

		UIEventHelper.notifyListeners(this, UIEventHelper.LIST_RELOAD);
	}

	/**
	 * コンボ最新化.
	 */
	public void setInputComboZip() {

		logger.log(Messages.TR0001, getClass().getSimpleName(), "setInputComboZip");

		comboZip.removeAll();

		LibraryList libraryList = RemoteContentManager.getLibraryList();
		// libraryListのnull対応 基本プロジェクトが空の場合も含む.
		if (libraryList == null || libraryList.getInfoBaseProjectMap().isEmpty()) {
			UIEventHelper.setErrorMessage(this, Messages.SE0053.format());
			lblListInfo.setText(Messages.PI0151.format());
			lblListInfo.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
			linkInfo.getParent().layout();
			linkInfo.getParent().getParent().layout();
			return;
		}
		UIEventHelper.setErrorMessage(this, null);
		lblListInfo.setText(libraryList.getInfo());
		if (libraryList.getSource() == null) {
			lblListInfo.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
		} else {
			lblListInfo.setForeground(null);
		}

		for (Info info : libraryList.getInfoMap().values()) {
			comboZip.add(info.getTitle());
		}
		if (comboZip.getItemCount() > 0) {
			comboZip.select(0);
			Info info = libraryList.getInfoMap().get(comboZip.getText());
			linkInfo.setText(info.getDescription());
			linkInfo.setVisible(true);
		}
		linkInfo.getParent().layout();
		linkInfo.getParent().getParent().layout();
	}

	/**
	 * コンボ選択.
	 * 
	 * @param e イベント
	 */
	protected void do_comboZip_widgetSelected(SelectionEvent e) {

		logger.log(Messages.TR0001, getClass().getSimpleName(), "do_comboZip_widgetSelected");

		if (comboZip.getText() == null) {
			linkInfo.setVisible(false);
		} else {
			LibraryList libraryList = RemoteContentManager.getLibraryList();
			// libraryListのnull対応.
			if (libraryList == null) {
				return;
			}
			Info info = libraryList.getInfoMap().get(comboZip.getText());
			linkInfo.setText(info.getDescription());
			linkInfo.setVisible(true);
		}
		linkInfo.getParent().layout();
		linkInfo.getParent().getParent().layout();
		UIEventHelper.notifyListeners(this, UIEventHelper.PROJECT_CHANGE);
	}

	/**
	 * テキスト変更処理.
	 * 
	 * @param e イベント
	 */
	protected void do_textProject_modifyText(ModifyEvent e) {

		logger.log(Messages.TR0001, getClass().getSimpleName(), "do_textProject_modifyText");

		validatePage();
		UIEventHelper.notifyListeners(this, UIEventHelper.PROJECT_CHANGE);
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 */
	void validatePage() {

		logger.log(Messages.TR0001, getClass().getSimpleName(), "validatePage");

		// プロジェクト名のチェック.
		String name = textProject.getText();

		// check whether the project name field is empty
		if (name.length() == 0) {
			// setMessage(NewWizardMessages.JavaProjectWizardFirstPage_Message_enterProjectName);
			// setErrorMessage(NewWizardMessages.JavaProjectWizardFirstPage_Message_enterProjectName);
			UIEventHelper.setErrorMessage(this, Messages.SE0051.format());
			return;
		}

		// IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		// if (project == null){
		// setErrorMessage(nameStatus.getMessage());
		// return;
		// }
		//
		// check whether the project name is valid
		final IStatus nameStatus = ResourcesPlugin.getWorkspace().validateName(name, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			UIEventHelper.setErrorMessage(this, nameStatus.getMessage());
			return;
		}

		// check whether project already exists
		final IProject handle = getProjectHandle();
		if (handle.exists()) {
			// setErrorMessage(NewWizardMessages.JavaProjectWizardFirstPage_Message_projectAlreadyExists);
			UIEventHelper.setErrorMessage(this, Messages.SE0052.format());
			return;
		}

		// 正常.
		UIEventHelper.setErrorMessage(this, null);
	}
}
