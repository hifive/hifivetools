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
package com.htmlhifive.tools.codeassist.core.config;

import com.htmlhifive.tools.codeassist.core.config.bean.AllBean;
import com.htmlhifive.tools.codeassist.core.exception.ParseException;

/**
 * 設定ファイルを解析するインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface ConfigFileParser {

	/**
	 * コードアシストのオプションから全てのコード補完情報を取得する.
	 * 
	 * @return コード補完情報
	 * @throws ParseException 解析例外.
	 */
	AllBean getCodeAssistBean() throws ParseException;

	// /**
	// * コードアシストのオプションから、指定したサフィックスのコード補完情報を取得する.
	// *
	// * @param suffix サフィックス
	// * @return コード補完情報.
	// * @throws ParseException 解析例外.
	// */
	// ControllerBean getCodeAssistBean(String suffix) throws ParseException;

	// /**
	// * 指定したオブジェクト名がHi5用にコード補完をする必要があるかどうか判定する.
	// *
	// * @param objeName 検査するオブジェクト名.
	// * @return コード補完をする必要があるかどうか.
	// * @throws ParseException 解析例外
	// */
	// boolean isMatchs(String objeName) throws ParseException;

}
