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
package com.htmlhifive.tools.jslint.engine.download;

/**
 * エンジンファイル情報.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class EngineInfo {

	/**
	 * ソース情報.
	 */
	private String mainSource;

	/**
	 * ライセンス情報.
	 */
	private String licenseStr;

	/**
	 * ソース情報.を取得する.
	 * 
	 * @return ソース情報.
	 */
	public String getMainSource() {
		return mainSource;
	}

	/**
	 * ソース情報.を設定する.
	 * 
	 * @param mainSource ソース情報.
	 */
	public void setMainSource(String mainSource) {
		this.mainSource = mainSource;
	}

	/**
	 * ライセンス情報.を取得する.
	 * 
	 * @return ライセンス情報.
	 */
	public String getLicenseStr() {
		return licenseStr;
	}

	/**
	 * ライセンス情報.を設定する.
	 * 
	 * @param licenseStr ライセンス情報.
	 */
	public void setLicenseStr(String licenseStr) {
		this.licenseStr = licenseStr;
	}

}
