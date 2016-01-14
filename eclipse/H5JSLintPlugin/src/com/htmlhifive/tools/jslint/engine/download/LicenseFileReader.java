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
package com.htmlhifive.tools.jslint.engine.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;

/**
 * ライセンスファイル用読み込みクラス
 * 
 * @author NS Solutions Corporation
 *
 */
public class LicenseFileReader extends BufferedReader {

	public LicenseFileReader(Reader in) {
		super(in);
	}

	public LicenseFileReader(Reader in, int sz) {
		super(in, sz);
	}

	@Override
	public String readLine() throws IOException {
		// ライセンスファイルはコメントアウトされているので、先頭を除去する
		String temp = super.readLine();
		temp = StringUtils.trim(temp);
		temp = StringUtils.substring(temp, 2);
		temp = StringUtils.trim(temp);
		return temp;
	}
}
