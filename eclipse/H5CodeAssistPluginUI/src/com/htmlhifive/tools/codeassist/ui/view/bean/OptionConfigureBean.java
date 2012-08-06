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
package com.htmlhifive.tools.codeassist.ui.view.bean;

import com.htmlhifive.tools.codeassist.ui.view.bean.BeanChangeEvent.CompositeBean;

/**
 * オプションファイル設定コンポジットのビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class OptionConfigureBean implements CompositeBean {

	/**
	 * オプションファイルパス.
	 */
	private String optionFilePath = "";

	/**
	 * オプションファイルパスを取得する.
	 * 
	 * @return オプションファイルパス
	 */
	public String getOptionFilePath() {

		return optionFilePath;
	}

	/**
	 * オプションファイルパスを設定する.
	 * 
	 * @param optionFilePath オプションファイルパス
	 */
	public void setOptionFilePath(String optionFilePath) {

		this.optionFilePath = optionFilePath;
	}
}
