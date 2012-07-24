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
package com.htmlhifive.tools.jslint.view;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.wst.jsdt.core.JavaScriptCore;

import com.htmlhifive.tools.jslint.configure.JSLintConfigManager;
import com.htmlhifive.tools.jslint.event.ConfigBeanChangeEvent;
import com.htmlhifive.tools.jslint.event.ConfigBeanChangeListener;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;
import com.htmlhifive.tools.jslint.parse.JsParserFactory;
import com.htmlhifive.tools.jslint.parse.Parser;
import com.htmlhifive.tools.jslint.parse.ParserManager;
import com.htmlhifive.tools.jslint.util.CheckJavaScriptUtils;
import com.htmlhifive.tools.jslint.util.ConfigBeanUtil;

/**
 * Jslint設定ページ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JslintPropertyPage extends PropertyPage {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JslintPropertyPage.class);

	/**
	 * 選択プロジェクト.
	 */
	private IProject project;

	/**
	 * 設定ページコンポジット.
	 */
	private JslintPropertyComposite propertyComp;

	/**
	 * タブフォルダー.
	 */
	private TabFolder tabFolder;

	/**
	 * コンストラクタ.
	 */
	public JslintPropertyPage() {

		super();
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		// JSLint設定のロード.
		project = (IProject) getElement().getAdapter(IProject.class);
		JSLintConfigManager.loadConfig(project);
		tabFolder = new TabFolder(parent, SWT.TOP);
		propertyComp = new JslintPropertyComposite(tabFolder, project);

		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabFolder.setLayoutData(GridDataFactory.fillDefaults().create());
		tabItem.setText(Messages.TT0000.getText());
		tabItem.setControl(propertyComp);
		noDefaultAndApplyButton();
		GridLayout layout = new GridLayout();
		propertyComp.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		propertyComp.setLayoutData(data);

		propertyComp.addConfigBeanChangeListener(new ConfigBeanChangeListener() {

			@Override
			public void modified(ConfigBeanChangeEvent event) {

				initCheck();
				String[] errorMessages = ConfigBeanUtil.checkProperty(event.getChangedBean());
				if (errorMessages.length > 0) {
					setErrorMessage(buildMessage(errorMessages));
				}
			}

		});
		// 構成ページ
		final TabItem targetStracture = new TabItem(tabFolder, SWT.NONE);
		targetStracture.setText(Messages.TT0001.getText());
		TargetStructureComposite structureComposite = new TargetStructureComposite(tabFolder,
				JavaScriptCore.create(project), (IWorkbenchPreferenceContainer) getContainer());
		structureComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		targetStracture.setControl(structureComposite);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item == targetStracture) {
					((TargetStructureComposite) targetStracture.getControl()).refreshViewer();
				}
			}
		});
		return tabFolder;
	}

	@Override
	public boolean okToLeave() {
		tabFolder.setSelection(0);
		return super.okToLeave();
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {

		super.performDefaults();
		propertyComp.setupVariable(JSLintConfigManager.getDefaultConfigBean(project));

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		JSLintConfigManager.saveConfig(project);
		if (JSLintConfigManager.getConfigBean(project).equals(JSLintConfigManager.getDefaultConfigBean(project))) {
			return true;
		}

		try {
			if (CheckJavaScriptUtils.isIncludeJslintNature(project)) {
				// .jslintファイルがパーサーによってロックされてしまうので解除する.
				ParserManager.cancelCurrentParser();
				JSLintConfigManager.saveConfig(project);

				WorkspaceJob job = new WorkspaceJob(Messages.T0005.getText()) {

					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) {

						try {
							Parser parse = JsParserFactory.createParser(project);
							parse.parse(monitor);
						} catch (CoreException e) {
							logger.put(Messages.EM0001, e);
							return e.getStatus();
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
			} else {
				JSLintConfigManager.saveConfig(project);
			}
		} catch (CoreException e) {
			logger.put(Messages.EM0001, e);
		}
		return true;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {

	}

	/**
	 * 入力項目のチェックの前の初期設定.<br>
	 * エラーメッセージをクリア.validをtrueにする.
	 */
	private void initCheck() {

		setErrorMessage(null);
		setValid(true);

	}

	/**
	 * メッセージリストを組み立てる.
	 * 
	 * @param errorMessages メッセージ.
	 * @return 組立後のメッセージ.
	 */
	private String buildMessage(String[] errorMessages) {

		StringBuilder sb = new StringBuilder();

		for (String message : errorMessages) {
			sb.append(message);
		}
		return sb.toString();
	}

}