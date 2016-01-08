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
package com.htmlhifive.tools.codeassist.core.config.bean;

import java.util.regex.Pattern;

import com.htmlhifive.tools.codeassist.core.config.xml.Logic;


/**
 * ロジックのコード補完に必要な情報ビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class LogicBean extends ObjectLiteralBean implements RegExPatternRootChild {

	/**
	 * 突き合わせる正規表現.
	 */
	private Pattern regExPattern;

	/**
	 * コントローラの正規表現.
	 */
	private Pattern regExControllerPattern;

	/**
	 * コンストラクタ.
	 * 
	 * @param logic 設定ファイルのロジック.
	 */
	public LogicBean(Logic logic) {

		super(logic.getFunctionOrVarRef());
		// サフィックス
		this.regExPattern = Pattern.compile(".*" + logic.getSuffix());
		this.regExControllerPattern = Pattern.compile(".*" + logic.getControllerSuffix());

	}

	/**
	 * 突き合わせる正規表現を取得する.
	 * 
	 * @return 突き合わせる正規表現
	 */
	public Pattern getRegExPattern() {

		return regExPattern;
	}

	/**
	 * コントローラの正規表現を取得する.
	 * 
	 * @return コントローラの正規表現
	 */
	public Pattern getRegExControllerPattern() {

		return regExControllerPattern;
	}
}
