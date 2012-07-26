package com.htmlhifive.tools.jslint.engine.download;

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
