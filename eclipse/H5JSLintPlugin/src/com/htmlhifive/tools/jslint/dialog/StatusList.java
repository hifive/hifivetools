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

import com.htmlhifive.tools.jslint.JSLintPlugin;

/**
 * IStatusをリストとして保持するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class StatusList {

	/**
	 * ステータスをリストとして保持する.
	 */
	private MultiStatus multistatus;

	/**
	 * コンストラクタ.
	 */
	public StatusList() {

		multistatus = new MultiStatus(JSLintPlugin.PLUGIN_ID, IStatus.OK, null, null);
	}

	/**
	 * 
	 * ダイアログに表示するステータスを追加する.<br>
	 * MultiStatusの場合は子要素が追加される.
	 * 
	 * @param status 追加するステータス.
	 */
	public void add(IStatus status) {

		multistatus.merge(status);
	}

	/**
	 * ダイアログに登録されたステータスを取得する.
	 * 
	 * @return 登録されたステータス.
	 */
	public IStatus[] getStatuses() {

		return multistatus.getChildren();
	}

	/**
	 * 保持されたステータスが引数のレベルと等しいか判定する.
	 * 
	 * @param severityMask レベル(ERROR, WARNING, INFO, CANCEL)
	 * @return 等しければtrue、そうでない場合はfalse
	 */
	public boolean matches(int severityMask) {

		return multistatus.matches(severityMask);
	}

	/**
	 * ステータスリストの中身が全てokかどうかを判定する.
	 * 
	 * @return 全てokの場合はtrue、そうでない場合はfalse
	 */
	public boolean isOK() {

		return multistatus.isOK();
	}

	/**
	 * ステータスリストのレベルを取得する.
	 * 
	 * @return ステータスのレベル.
	 */
	public int getSeverity() {

		return multistatus.getSeverity();
	}

}
