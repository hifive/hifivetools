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
package jp.co.nssol.h5.tool.jslint.view;

import jp.co.nssol.h5.tool.jslint.configure.JSLintConfigManager;
import jp.co.nssol.h5.tool.jslint.event.ConfigBeanChangeEvent;
import jp.co.nssol.h5.tool.jslint.event.ConfigBeanChangeListener;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLoggerFactory;
import jp.co.nssol.h5.tool.jslint.messages.Messages;
import jp.co.nssol.h5.tool.jslint.parse.JsParserFactory;
import jp.co.nssol.h5.tool.jslint.parse.Parser;
import jp.co.nssol.h5.tool.jslint.parse.ParserManager;
import jp.co.nssol.h5.tool.jslint.util.CheckJavaScriptUtils;
import jp.co.nssol.h5.tool.jslint.util.ConfigBeanUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

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

		Composite composite = new Composite(parent, SWT.NONE);
		noDefaultAndApplyButton();
		project = (IProject) getElement().getAdapter(IProject.class);
		JSLintConfigManager.loadConfig(project);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		propertyComp = new JslintPropertyComposite(composite, project);

		propertyComp.addConfigBeanChangeListener(new ConfigBeanChangeListener() {

			@Override
			public void modified(ConfigBeanChangeEvent event) {

				initCheck();
				String[] errorMessages = ConfigBeanUtil.checkProperty(event.getChangedBean());
				if (errorMessages.length > 0) {
					setErrorMessage(buildMessage(errorMessages));
					setValid(false);
				}
			}

		});
		return composite;
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