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
package com.htmlhifive.tools.wizard.utils;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.log.messages.MessagesBase.Message;


/**
 * <H3>ログユーティリティ.</H3>
 * 
 * @author fkubo
 */
public class H5LogUtils {

	/**
	 * エラーログを出力する.
	 * 
	 * @param e 例外
	 * @param message ダイアログに表示するメッセージ
	 * @param params メッセージ用パラメータ
	 * @return IStatus
	 */
	public static IStatus putLog(Throwable e, Message message, Object... params) {
	
		// eはnull可能
		final IStatus status =
				new Status(IStatus.INFO, H5WizardPlugin.getId(), IStatus.INFO, message.format(params), e);
		final ILog log = H5WizardPlugin.getInstance().getLog();
		// .metadata/.logにログを出力
		log.log(status);
		return status;
	}

	/**
	 * エラーログを出力する.
	 * 
	 * @param title ダイアログに表示するタイトル
	 * @param status IStatus
	 */
	public static void showLog(Message title, IStatus status) {
	
		// ダイアログにエラーを表示
		ErrorDialog.openError(null, title.format(), null, status);
	}

	/**
	 * エラーログを出力する.
	 * 
	 * @param e 例外
	 * @param title ダイアログに表示するタイトル
	 * @param message ダイアログに表示するメッセージ
	 * @param params メッセージ用パラメータ
	 */
	public static void showLog(Throwable e, Message title, Message message, Object... params) {
	
		// ログ出力.
		IStatus status = putLog(e, message, params);
	
		showLog(title, status);
	}

}
