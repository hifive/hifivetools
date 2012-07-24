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
package com.htmlhifive.tools.jslint.util;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.htmlhifive.tools.jslint.JSLintPluginNature;

/**
 * 
 * JavaScriptチェックのユーティリティクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class CheckJavaScriptUtils {

	/**
	 * コンストラクタ.
	 */
	private CheckJavaScriptUtils() {

	}

	/**
	 * ReaderオブジェクトをString型に変換する.
	 * 
	 * @param reader 変換Readerオブジェクト
	 * @return 変換後文字列
	 * @throws IOException 入出力例外
	 */
	public static String readerToString(Reader reader) throws IOException {

		StringBuffer sb = new StringBuffer();
		int c;
		while ((c = reader.read()) != -1) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	/**
	 * 文字列が空文字かnullの判定をする.
	 * 
	 * @param str 判定対象文字列
	 * @return 空文字かnullだったらtrue、そうでない場合はfalse.
	 */
	public static boolean nullOrEmplty(String str) {

		return str == null || str.isEmpty();
	}

	/**
	 * プロジェクトにJSLintプラグインネーチャーが含まれているかどうかをチェックする.
	 * 
	 * @param project プロジェクト.
	 * @return JSlintプラグインネーチャーが含まれているかどうか
	 * @throws CoreException 例外<br>
	 *             プロジェクトが存在しない、又はプロジェクトが閉じているとき.
	 * 
	 */
	public static boolean isIncludeJslintNature(IProject project) throws CoreException {

		String[] ids = project.getDescription().getNatureIds();
		for (String id : ids) {
			if (JSLintPluginNature.NATURE_ID.equals(id)) {
				return true;
			}
		}
		return false;
	}

}
