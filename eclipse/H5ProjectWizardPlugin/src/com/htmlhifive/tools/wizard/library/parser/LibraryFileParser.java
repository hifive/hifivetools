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
 */
package com.htmlhifive.tools.wizard.library.parser;

import com.htmlhifive.tools.wizard.library.LibraryList;

/**
 * 設定ファイルを解析するインターフェース.
 * 
 * @author fkubo
 */
public interface LibraryFileParser {

	/**
	 * ライブラリ情報を取得する.
	 * 
	 * @return ライブラリ情報.
	 * @throws ParseException 解析例外.
	 */
	LibraryList getLibraryList() throws ParseException;

}
