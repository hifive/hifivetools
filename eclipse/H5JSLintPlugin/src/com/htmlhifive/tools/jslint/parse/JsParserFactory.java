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
package com.htmlhifive.tools.jslint.parse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.JavaScriptCore;

/**
 * 
 * jsスクリプトファイルのパースクラスのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class JsParserFactory {

	/**
	 * コンストラクタ.
	 */
	private JsParserFactory() {

	}

	/**
	 * JSパーサーを生成する.
	 * 
	 * @param selection 対象リソース.
	 * @return パーサー
	 */
	public static Parser createParser(IResource selection) {

		IProject project = selection.getProject();
		IJavaScriptProject jsProject = JavaScriptCore.create(project);

		if (jsProject.exists()) {
			return new JsProjectParser(selection);
		}
		return new JsParser(selection);

	}

}
