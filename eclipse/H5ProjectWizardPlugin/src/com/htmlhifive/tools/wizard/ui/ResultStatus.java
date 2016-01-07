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
package com.htmlhifive.tools.wizard.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.log.LogLevel;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.log.messages.MessagesBase.Message;

/**
 * <H3>ログ出力用UI.</H3>
 * 
 * @author fkubo
 */
public class ResultStatus {

	/** ログ. */
	private final StringBuilder allLog = new StringBuilder();

	/** Statusリスト. */
	private final List<IStatus> statusList = new ArrayList<IStatus>();

	/** 結果. */
	private boolean success = true;

	/** 割り込み. */
	private boolean interrupted = false;

	// eはnull可能

	/**
	 * メッセージを追加する.
	 * 
	 * @param logLevel レベル
	 * @param msg メッセージ
	 * @param e 例外
	 */
	private void put(LogLevel logLevel, String msg, Throwable e) {

		IStatus status = null;
		switch (logLevel) {
			case FATAL:
			case ERROR:
				status = new Status(IStatus.ERROR, H5WizardPlugin.getId(), msg, e);
				break;
			case WARN:
				status = new Status(IStatus.WARNING, H5WizardPlugin.getId(), msg, e);
				break;
			case INFO:
				status = new Status(IStatus.INFO, H5WizardPlugin.getId(), msg, e);
				break;
			default:
		}
		if (status != null) {
			statusList.add(status);
			if (status.getSeverity() != IStatus.INFO) { // Info以外を出力
				// .metadata/.logにログを出力
				H5WizardPlugin.getInstance().getLog().log(status);
			}
		}
		allLog.append(msg);
	}

	/**
	 * ログ追加処理.
	 * 
	 * @param message メッセージ
	 * @param params パラメータ
	 */
	public void log(Message message, Object... params) {

		log(null, message, params);
	}

	/**
	 * ログ追加処理.
	 * 
	 * @param e 例外
	 * @param message メッセージ
	 * @param params パラメータ
	 */
	public void logIgnoreSetSuccess(Throwable e, Message message, Object... params) {

		boolean oldSuccess = isSuccess();
		log(e, message, params);
		setSuccess(oldSuccess);
	}

	/**
	 * ログ追加処理.
	 * 
	 * @param e 例外
	 * @param message メッセージ
	 * @param params パラメータ
	 */
	public void log(Throwable e, Message message, Object... params) {

		StringBuilder log = new StringBuilder();
		log.append("[");
		log.append(DateFormatUtils.format(System.currentTimeMillis(), "hh:mm:ss.SSS"));
		log.append("] ");
		log.append(message.getLevel().name());
		log.append(" ");
		log.append(message.getKey());
		log.append(" ");
		log.append(message.format(params));
		if (e != null) {
			setSuccess(false); // 例外発生で直ぐに実行失敗としておく
			log.append("\n");
			log.append(ExceptionUtils.getStackTrace(e));
		}
		log.append("\n");
		put(message.getLevel(), log.toString(), e);
	}

	/**
	 * 結果.を取得します。
	 * 
	 * @param method 処理名用メッセージ
	 */
	public void showDialog(Message method) {

		if (interrupted) {
			ErrorDialog.openError(null, Messages.PI0131.format(), null,
					getMultiStatus(IStatus.INFO, Messages.PI0134.format(method.format())));
		} else if (isSuccess()) {
			ErrorDialog.openError(null, Messages.PI0131.format(), null,
					getMultiStatus(IStatus.INFO, Messages.PI0132.format(method.format())));
		} else {
			ErrorDialog.openError(null, Messages.PI0131.format(), null,
					getMultiStatus(IStatus.WARNING, Messages.PI0133.format(method.format())));
		}

	}

	/**
	 * 結果.を設定します。
	 * 
	 * @return 結果.
	 */
	public boolean isSuccess() {

		return success;
	}

	/**
	 * 結果.を設定します。
	 * 
	 * @param success 結果.
	 */
	public void setSuccess(boolean success) {

		this.success = success;
	}

	/**
	 * 結果.
	 * 
	 * @return 結果
	 */
	public String getLog() {

		return allLog.toString();
	}

	/**
	 * 結果を取得します。
	 * 
	 * @return 結果.
	 */
	public List<IStatus> getStatusList() {

		return statusList;
	}

	/**
	 * 結果を取得します。
	 * 
	 * @return 結果.
	 */
	private MultiStatus getMultiStatus(final int level, String message) {

		return new MultiStatus(H5WizardPlugin.getId(), level, statusList.toArray(new IStatus[0]), message, null) {
			@Override
			public int getSeverity() {
				// 内部のStatusの最大がセットされるのを防ぐ.
				return level;
			}
		};
	}

	/**
	 * 割り込み.を取得します.
	 * 
	 * @return 割り込み.
	 */
	public boolean isInterrupted() {
		return interrupted;
	}

	/**
	 * 割り込み.を設定します.
	 * 
	 * @param interrupted 割り込み.
	 */
	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

}
