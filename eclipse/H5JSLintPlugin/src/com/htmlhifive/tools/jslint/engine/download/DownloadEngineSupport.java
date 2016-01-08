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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.htmlhifive.tools.jslint.engine.option.Engine;

/**
 * ダウンロード支援I/F.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface DownloadEngineSupport {

	/**
	 * エンジン種別を取得する.
	 * 
	 * @return エンジン種別.
	 */
	Engine getEngine();

	/**
	 * デフォルトのソースを取得する.<br>
	 * ダウンロードが出来なかった場合.
	 * 
	 * @return デフォルトのソースコード.
	 */
	String getDefaultSource();

	/**
	 * エンジンソース取得URLを取得する.
	 * 
	 * @return エンジンソース取得URL
	 */
	String getEngineSourceUrl();

	/**
	 * エンジン情報を取得する.
	 * 
	 * @param monitor モニター.
	 * @return エンジン情報.
	 * @throws IOException 入出力例外.
	 */
	EngineInfo getEngineInfo(IProgressMonitor monitor) throws IOException;

}
