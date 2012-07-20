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

import java.util.ArrayList;
import java.util.List;

import jp.co.nssol.h5.tool.jslint.Activator;
import jp.co.nssol.h5.tool.jslint.configure.ConfigBean;
import jp.co.nssol.h5.tool.jslint.configure.JSLintConfigManager;
import jp.co.nssol.h5.tool.jslint.dialog.JSLintStatusDialog;
import jp.co.nssol.h5.tool.jslint.event.ConfigBeanChangeEvent;
import jp.co.nssol.h5.tool.jslint.event.ConfigBeanChangeListener;
import jp.co.nssol.h5.tool.jslint.exception.JSLintPluginException;
import jp.co.nssol.h5.tool.jslint.messages.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * .jslintを使用するときのコンポジット抽象クラス.
 *
 * @author NS Solutions Corporation
 *
 */
public abstract class AbstractJsLintPropertyComposite extends Composite {

	/**
	 * 設定ビーン.
	 */
	private ConfigBean configBean;

	/**
	 * プロジェクト.
	 */
	private IProject project;

	/**
	 * コンフィグビーンリスﾅリスト.
	 */
	private List<ConfigBeanChangeListener> listenerList;

	/**
	 * コンストラクタ.
	 *
	 * @param parent 親コンポジット
	 * @param project プロジェクト
	 */
	public AbstractJsLintPropertyComposite(Composite parent, IProject project) {

		super(parent, SWT.None);
		this.project = project;
		listenerList = new ArrayList<ConfigBeanChangeListener>();
		configBean = JSLintConfigManager.getConfigBean(this.project);
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(data);
		createMainArea();
		this.pack();
		setupVariable((ConfigBean) getConfigBean().clone());
	}

	/**
	 * 主となるエリアを作成する.
	 */
	protected abstract void createMainArea();

	/**
	 * 設定ビーンを取得する.
	 *
	 * @return 設定ビーン.
	 */
	protected ConfigBean getConfigBean() {

		return configBean;
	}

	/**
	 * リスナを追加する.
	 *
	 * @param listener 追加するリスナ.
	 */
	protected void addConfigBeanChangeListener(ConfigBeanChangeListener listener) {

		listenerList.add(listener);
	}

	/**
	 * 設定情報を更新する.
	 */
	protected void updateVariable() {

		doUpdate();
		for (ConfigBeanChangeListener listener : listenerList) {
			listener.modified(new ConfigBeanChangeEvent(getConfigBean()));
		}
	}

	/**
	 * 更新処理を行う.
	 */
	protected abstract void doUpdate();

	/**
	 * 表示を設定する.
	 *
	 * @param initBean 初期状態の設定ファイルビーン
	 */
	protected void setupVariable(ConfigBean initBean) {

		try {
			doSetup(initBean);
		} catch (JSLintPluginException e) {
			JSLintStatusDialog.openDialog(getShell(), Messages.DT0004.getText(), new Status(IStatus.INFO,
					Activator.PLUGIN_ID, e.getMessage()));
			throw new RuntimeException();
		}
	}

	/**
	 * 表示設定処理を行う.
	 *
	 * @param configBean コンフィグビーン
	 * @throws JSLintPluginException 初期化例外.
	 */
	protected abstract void doSetup(ConfigBean configBean) throws JSLintPluginException;

	/**
	 * プロジェクトを取得する.
	 *
	 * @return プロジェクト
	 */
	protected IProject getProject() {

		return project;
	}

}
