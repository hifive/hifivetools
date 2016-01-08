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
 *
 */
package com.htmlhifive.tools.jslint.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

import com.htmlhifive.tools.jslint.JSLintPlugin;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * JSLintプラグインで使用するステータス表示ダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSLintStatusDialog {

	/**
	 * シェル.
	 */
	private final Shell shell;

	/**
	 * ステータスリスト.
	 */
	private final StatusList statusList;

	/**
	 * コンストラクタ.
	 * 
	 * @param shell シェル.
	 * @param statusList ステータスリスト.
	 */
	public JSLintStatusDialog(Shell shell, StatusList statusList) {

		this.shell = shell;
		this.statusList = statusList;
	}

	/**
	 * ダイアログを開く.
	 */
	public void open() {

		MultiStatus status = null;
		if (!statusList.isOK()) {
			if (statusList.matches(IStatus.INFO)) {
				status = new MultiStatus(JSLintPlugin.PLUGIN_ID, IStatus.ERROR, Messages.DM0002.getText(), null);
				openDialog(status, Messages.DT0005.getText());
			} else if (statusList.matches(IStatus.WARNING)) {
				status = new MultiStatus(JSLintPlugin.PLUGIN_ID, IStatus.ERROR, Messages.EM0001.getText(), null);
				openDialog(status, Messages.DT0004.getText());
			} else if (statusList.matches(IStatus.ERROR)) {
				status = new MultiStatus(JSLintPlugin.PLUGIN_ID, IStatus.ERROR, Messages.EM0100.getText(), null);
				openDialog(status, Messages.DT0003.getText());
			}
		}

	}

	/**
	 * ダイアログを開く.
	 * 
	 * @param status マルチステータス.
	 * @param dialogTitle ダイアログのタイトル.
	 */
	private void openDialog(MultiStatus status, String dialogTitle) {

		for (IStatus iStatus : statusList.getStatuses()) {
			status.add(iStatus);
		}
		ErrorDialog.openError(shell, dialogTitle, null, status);

	}

	/**
	 * ダイアログを開く.
	 * 
	 * @param shell シェル.
	 * @param dialogTitle ダイアログタイトル.
	 * @param status ステータス.
	 */
	public static void openDialog(Shell shell, String dialogTitle, IStatus status) {

		ErrorDialog.openError(shell, dialogTitle, null, status);
	}

}
