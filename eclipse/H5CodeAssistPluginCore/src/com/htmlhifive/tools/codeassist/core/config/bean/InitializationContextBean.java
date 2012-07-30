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
package com.htmlhifive.tools.codeassist.core.config.bean;

import java.util.regex.Pattern;

import com.htmlhifive.tools.codeassist.core.config.xml.InitializationContext;


/**
 * イベントコンテキスト補完候補用のビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class InitializationContextBean extends ObjectLiteralBean implements RegExPatternRootChild {

	/**
	 * コントローラの正規表現パターン.
	 */
	private Pattern regExPattern;

	/**
	 * コンストラクタ.
	 * 
	 * @param eventcontext イベントコンテキスト情報
	 */
	public InitializationContextBean(InitializationContext eventcontext) {

		super(eventcontext.getFunctionOrVarRef());
		// サフィックスで設定.
		regExPattern = Pattern.compile(".*" + eventcontext.getControllerSuffix());
	}

	/**
	 * 突き合わせる正規表現を取得する.
	 * 
	 * @return 突き合わせる正規表現
	 */
	public Pattern getRegExPattern() {

		return regExPattern;
	}

}
