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
 package jp.co.nssol.h5.tool.jslint.engine;

import java.io.IOException;
import java.io.Reader;

import javax.script.ScriptException;

/**
 * jsLint実行用インターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface JSChecker {

	/**
	 * JSLintを実行する.
	 * 
	 * @param source ソースコード文字列.
	 * @return エラー配列、例外時はnull.
	 * @throws ScriptException 解析例外
	 */
	public JSCheckerResult lint(String source) throws ScriptException;

	/**
	 * JSLintを実行する.
	 * 
	 * @param reader ソースコード.
	 * @return エラー配列、例外時はnull.
	 * @throws ScriptException 解析例外
	 * @throws IOException 入出力例外
	 */
	public JSCheckerResult lint(Reader reader) throws ScriptException, IOException;

}