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
package com.htmlhifive.tools.wizard.utils;

import java.util.regex.PatternSyntaxException;

/**
 * <H3>文字列ユーティリティ.</H3>
 * 
 * @author fkubo
 */
public abstract class H5StringUtils {

	/**
	 * 前後の\t\r\n 　を除去する.
	 * 
	 * @param str 文字列
	 * @return trimした文字列
	 */
	public static String trim(String str) {

		if (str == null) {
			return null;
		}
		try {
			return str.replaceAll("^[\\s]*", "").replaceAll("[\\s]*$", "");
		} catch (PatternSyntaxException ignore) {
			// ignore.
		}
		return str;
	}
}
