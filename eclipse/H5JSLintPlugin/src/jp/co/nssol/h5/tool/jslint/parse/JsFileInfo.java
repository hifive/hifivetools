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
package jp.co.nssol.h5.tool.jslint.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jp.co.nssol.h5.tool.jslint.JSLintPluginConstant;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * ライブラリ情報.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JsFileInfo implements Cloneable {

	/**
	 * ライブラリの行数.
	 */
	private int lineCount;

	/**
	 * ライブラリの文字列.
	 */
	private String libraryStr;

	/**
	 * コンストラクタ.
	 */
	public JsFileInfo() {

	}

	/**
	 * コンストラクタ.
	 * 
	 * @param jsFile jsファイル
	 * @throws CoreException 解析例外.
	 * @throws IOException 入出力例外.
	 */
	public JsFileInfo(IFile jsFile) throws CoreException, IOException {

		append(jsFile);

	}

	/**
	 * ファイルを追加する.
	 * 
	 * @param jsfile 追加するファイル
	 * @throws IOException 入出力例外
	 */
	public void append(File jsfile) throws IOException {

		if (!StringUtils.endsWith(jsfile.getName(), "." + JSLintPluginConstant.EXTENTION_JS)) {
			throw new IllegalArgumentException();
		}
		BufferedReader reader = new BufferedReader(new FileReader(jsfile));
		append(reader);
	}

	/**
	 * ファイルを追加する.
	 * 
	 * @param reader 追加対象.
	 * @throws IOException 入出力例外.
	 */
	public void append(BufferedReader reader) throws IOException {

		StringBuilder sb = new StringBuilder();
		if (libraryStr != null) {
			sb.append(libraryStr);
		}
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
				lineCount++;

			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		libraryStr = sb.toString();

	}

	/**
	 * ファイルを追加する.
	 * 
	 * @param info 追加ファイル情報
	 */
	public void append(JsFileInfo info) {

		StringBuilder sb = new StringBuilder();
		sb.append(this.libraryStr);
		sb.append(info.libraryStr);
		this.lineCount = this.lineCount + info.lineCount;
		this.libraryStr = sb.toString();
	}

	/**
	 * ファイルを追加する.
	 * 
	 * @param jsFile 追加ファイル.
	 * @throws IOException 入出力例外
	 * @throws CoreException 解析例外
	 */
	public void append(IFile jsFile) throws IOException, CoreException {

		if (!StringUtils.equals(jsFile.getFileExtension(), JSLintPluginConstant.EXTENTION_JS)) {
			throw new IllegalArgumentException();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(jsFile.getContents(), jsFile.getCharset(true)));
		append(reader);

	}

	/**
	 * ライブラリの行数を取得する.
	 * 
	 * @return ライブラリの行数
	 */
	public int getLineCount() {

		return lineCount;
	}

	/**
	 * ライブラリの文字列を取得する.
	 * 
	 * @return ライブラリの文字列
	 */
	public String getSourceStr() {

		return libraryStr;
	}

	@Override
	public JsFileInfo clone() {

		try {
			return (JsFileInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

}
