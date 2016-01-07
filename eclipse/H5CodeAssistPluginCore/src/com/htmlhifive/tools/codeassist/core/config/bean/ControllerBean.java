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

import com.htmlhifive.tools.codeassist.core.config.xml.Controller;


/**
 * 設定ファイルから読み込んだコントローラコード補完に必要な情報を保持するビーン.<br>
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ControllerBean extends ObjectLiteralBean implements RegExPatternRootChild {

	/**
	 * 突き合わせる正規表現.
	 */
	private Pattern regExPattern;

	/**
	 * コンストラクタ.
	 * 
	 * @param controller 設定ファイルから読み込んだコントローラ.
	 */
	public ControllerBean(Controller controller) {

		super(controller.getFunctionOrVarRef());
		this.regExPattern = Pattern.compile(".*" + controller.getSuffix());

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
