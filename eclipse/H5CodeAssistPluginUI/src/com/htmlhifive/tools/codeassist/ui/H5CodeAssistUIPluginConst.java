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
 package com.htmlhifive.tools.codeassist.ui;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Hi5CodeAssistUIPluginの定数クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class H5CodeAssistUIPluginConst {
	/**
	 * プロジェクトに設定したオプションファイルのキー.
	 */
	public static final String OPTION_FILE_KEY = "option_file_key";

	/**
	 * オプションファイルを取得するキー値.
	 */
	public static final QualifiedName GET_OPTION_QUALIFIED_NAME = new QualifiedName(H5CodeAssistUIPlugin.PLUGIN_ID,
			H5CodeAssistUIPluginConst.OPTION_FILE_KEY);

	/**
	 * コンストラクタ.
	 */
	private H5CodeAssistUIPluginConst() {

	}

}
