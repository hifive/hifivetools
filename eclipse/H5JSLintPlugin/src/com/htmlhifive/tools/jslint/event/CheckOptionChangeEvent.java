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
package com.htmlhifive.tools.jslint.event;

import com.htmlhifive.tools.jslint.engine.option.CheckOptionFileWrapper;

/**
 * オプションが変更された時のイベント.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CheckOptionChangeEvent {
	/**
	 * 変更後オプションファイル.
	 */
	private CheckOptionFileWrapper optionFile;

	/**
	 * コンストラクタ.
	 * 
	 * @param optionFile 変更後オプションファイル.
	 */
	public CheckOptionChangeEvent(CheckOptionFileWrapper optionFile) {

		this.optionFile = optionFile;
	}

	/**
	 * optionFileを取得する.
	 * 
	 * @return optionFile
	 */
	public CheckOptionFileWrapper getOptionFile() {

		return optionFile;
	}
}
