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
 package jp.co.nssol.h5.tool.jslint.configure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.nssol.h5.tool.jslint.engine.option.CheckOption;

/**
 * JSLintプラグインの設定保存クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ConfigBean implements Cloneable {

	/**
	 * 検査をするJSLintファイルパス.
	 */
	private String jsLintPath = "";
	/**
	 * オプションを設定したプロパティファイルパス.
	 */
	private String optionFilePath = "";

	/**
	 * 他プロジェクトの設定を使用するかどうか.
	 */
	private boolean useOtherProject = false;
	/**
	 * 他プロジェクトのパス.
	 */
	private String otherProjectPath = "";

	/**
	 * フィルタビーンリスト.
	 */
	private List<FilterBean> filterList = new ArrayList<FilterBean>();

	/**
	 * jsLintのオプション.
	 */
	private Map<String, CheckOption> jsLintOptionList = new HashMap<String, CheckOption>();

	/**
	 * jsHintのオプション.
	 */
	private Map<String, CheckOption> jsHintOptionList = new HashMap<String, CheckOption>();

	/**
	 * ライブラリパスリスト.
	 */
	private List<String> libList;

	/**
	 * コンストラクタ.
	 */
	public ConfigBean() {

		libList = new ArrayList<String>();
	}

	/**
	 * 検査をするJSLintファイルパス.を取得する.
	 * 
	 * @return 検査をするJSLintファイルパス.
	 */
	public String getJsLintPath() {

		return jsLintPath;
	}

	/**
	 * 検査をするJSLintファイルパス.を設定する.
	 * 
	 * @param jsLintPath 検査をするJSLintファイルパス.
	 */
	public void setJsLintPath(String jsLintPath) {

		this.jsLintPath = jsLintPath;
	}

	@Override
	public ConfigBean clone() {

		try {
			ConfigBean clone = (ConfigBean) super.clone();
			clone.setJsLintPath(jsLintPath);
			clone.setOptionFilePath(optionFilePath);
			clone.setOtherProjectPath(otherProjectPath);
			clone.setUseOtherProject(useOtherProject);
			clone.setLibList(getLibList().clone());
			clone.filterList = new ArrayList<FilterBean>();
			FilterBean[] filterBeans = getFilterBeans().clone();
			for (FilterBean filterBean : filterBeans) {
				clone.addFilterBean((FilterBean) filterBean.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

	/**
	 * オプションを設定したプロパティファイルパス.を取得する.
	 * 
	 * @return オプションを設定したプロパティファイルパス.
	 */
	public String getOptionFilePath() {

		return optionFilePath;
	}

	/**
	 * オプションを設定したプロパティファイルパス.を設定する.
	 * 
	 * @param optionFilePath オプションを設定したプロパティファイルパス.
	 */
	public void setOptionFilePath(String optionFilePath) {

		this.optionFilePath = optionFilePath;
	}

	/**
	 * 他プロジェクトの設定を使用するかどうか.を取得する.
	 * 
	 * @return 他プロジェクトの設定を使用するかどうか.
	 */
	public boolean isUseOtherProject() {

		return useOtherProject;
	}

	/**
	 * 他プロジェクトの設定を使用するかどうか.を設定する.
	 * 
	 * @param useOtherProject 他プロジェクトの設定を使用するかどうか.
	 */
	public void setUseOtherProject(boolean useOtherProject) {

		this.useOtherProject = useOtherProject;
	}

	/**
	 * 他プロジェクトのパス.を取得する.
	 * 
	 * @return 他プロジェクトのパス.
	 */
	public String getOtherProjectPath() {

		return otherProjectPath;
	}

	/**
	 * 他プロジェクトのパス.を設定する.
	 * 
	 * @param otherProjectPath 他プロジェクトのパス.
	 */
	public void setOtherProjectPath(String otherProjectPath) {

		this.otherProjectPath = otherProjectPath;
	}

	/**
	 * フィルタビーンリスト.を取得する.
	 * 
	 * @return フィルタビーンリスト.
	 */
	public FilterBean[] getFilterBeans() {

		return (FilterBean[]) filterList.toArray(new FilterBean[filterList.size()]);
	}

	/**
	 * フィルタビーンリストを追加する.
	 * 
	 * @param filterBean フィルタビーン.
	 */
	public void addFilterBean(FilterBean filterBean) {

		if (!this.filterList.contains(filterBean)) {
			this.filterList.add(filterBean);
		}

	}

	/**
	 * フィルタビーンを追加する.
	 * 
	 * @param filterBeans フィルタビーン.
	 */
	public void addFilterBeans(FilterBean[] filterBeans) {

		for (FilterBean filterBean : filterBeans) {
			addFilterBean(filterBean);
		}
	}

	/**
	 * jsLintのオプションを取得する.
	 * 
	 * @return jsLintのオプション
	 */
	public CheckOption[] getJsLintOptionList() {

		return (CheckOption[]) jsLintOptionList.values().toArray(new CheckOption[jsLintOptionList.size()]);
	}

	/**
	 * jsLintのオプションを設定する.
	 * 
	 * @param option jsLintのオプション
	 * @param overwrite 同キーがすでにあった場合上書きするかどうか.
	 */
	public void addJsLintOptionList(CheckOption option, boolean overwrite) {

		if (!jsLintOptionList.containsKey(option.getKey())) {
			jsLintOptionList.put(option.getKey(), option);
			return;
		}
		if (overwrite) {
			jsLintOptionList.put(option.getKey(), option);
		}
	}

	/**
	 * jsHintのオプションを取得する.
	 * 
	 * @return jsHintのオプション
	 */
	public CheckOption[] getJsHintOptionList() {

		return (CheckOption[]) jsHintOptionList.values().toArray(new CheckOption[jsHintOptionList.size()]);
	}

	/**
	 * jsHintのオプションを設定する.
	 * 
	 * @param option jsHintのオプション
	 * @param overwrite 同キーがすでにあった場合上書きするかどうか.
	 */
	public void addJsHintOptionList(CheckOption option, boolean overwrite) {

		if (!jsHintOptionList.containsKey(option.getKey())) {
			jsHintOptionList.put(option.getKey(), option);
			return;
		}
		if (overwrite) {
			jsHintOptionList.put(option.getKey(), option);
		}
	}

	/**
	 * JsHintOptionListをクリアする.
	 */
	public void clearJsHintOptionList() {

		jsHintOptionList.clear();
	}

	/**
	 * JsLintOptionListをクリアする.
	 */
	public void clearJsLintOptionList() {

		jsLintOptionList.clear();
	}

	/**
	 * ライブラリパスリストを追加する.
	 * 
	 * @param lib ライブラリパス
	 */
	public void addLibList(String lib) {

		this.libList.add(lib);
	}

	/**
	 * ライブラリパスリストを追加する.
	 * 
	 * @param libs ライブラリパス
	 */
	public void setLibList(String[] libs) {

		for (String string : libs) {
			this.libList.add(string);
		}

	}

	/**
	 * ライブラリパスリスト.を取得する.
	 * 
	 * @return ライブラリパスリスト.
	 */
	public String[] getLibList() {

		return (String[]) libList.toArray(new String[libList.size()]);
	}

	/**
	 * ライブラリパスリスト.を設定する.
	 * 
	 * @param libList ライブラリパスリスト.
	 */
	public void setLibList(List<String> libList) {

		this.libList = libList;
	}

	/**
	 * 保持しているフィルタービーンを引数に置き換える.
	 * 
	 * @param filterBeans 置き換えるフィルタビーン.
	 */
	public void replaceFilterBeans(FilterBean[] filterBeans) {

		if (filterBeans == null) {
			return;
		}
		this.filterList.clear();
		for (FilterBean filterBean : filterBeans) {
			this.filterList.add(filterBean);
		}

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((filterList == null) ? 0 : filterList.hashCode());
		result = prime * result + ((jsHintOptionList == null) ? 0 : jsHintOptionList.hashCode());
		result = prime * result + ((jsLintOptionList == null) ? 0 : jsLintOptionList.hashCode());
		result = prime * result + ((jsLintPath == null) ? 0 : jsLintPath.hashCode());
		result = prime * result + ((libList == null) ? 0 : libList.hashCode());
		result = prime * result + ((optionFilePath == null) ? 0 : optionFilePath.hashCode());
		result = prime * result + ((otherProjectPath == null) ? 0 : otherProjectPath.hashCode());
		result = prime * result + (useOtherProject ? 1231 : 1237);
		return result;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ConfigBean)) {
			return false;
		}
		ConfigBean other = (ConfigBean) obj;
		if (filterList == null) {
			if (other.filterList != null) {
				return false;
			}
		} else if (!filterList.equals(other.filterList)) {
			return false;
		}
		if (jsHintOptionList == null) {
			if (other.jsHintOptionList != null) {
				return false;
			}
		} else if (!jsHintOptionList.equals(other.jsHintOptionList)) {
			return false;
		}
		if (jsLintOptionList == null) {
			if (other.jsLintOptionList != null) {
				return false;
			}
		} else if (!jsLintOptionList.equals(other.jsLintOptionList)) {
			return false;
		}
		if (jsLintPath == null) {
			if (other.jsLintPath != null) {
				return false;
			}
		} else if (!jsLintPath.equals(other.jsLintPath)) {
			return false;
		}
		if (libList == null) {
			if (other.libList != null) {
				return false;
			}
		} else if (!libList.equals(other.libList)) {
			return false;
		}
		if (optionFilePath == null) {
			if (other.optionFilePath != null) {
				return false;
			}
		} else if (!optionFilePath.equals(other.optionFilePath)) {
			return false;
		}
		if (otherProjectPath == null) {
			if (other.otherProjectPath != null) {
				return false;
			}
		} else if (!otherProjectPath.equals(other.otherProjectPath)) {
			return false;
		}
		if (useOtherProject != other.useOtherProject) {
			return false;
		}
		return true;
	}

}
