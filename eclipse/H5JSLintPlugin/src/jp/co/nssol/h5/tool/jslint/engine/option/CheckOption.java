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
 package jp.co.nssol.h5.tool.jslint.engine.option;

import jp.co.nssol.h5.tool.jslint.JSLintPluginConstant;

/**
 * jslint,jshintのオプション.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CheckOption {

	/**
	 * オプションの指定クラス.
	 */
	private final Class<?> clazz;

	/**
	 * チェッカエンジン.
	 */
	private final String engine;

	/**
	 * オプションのキー値.
	 */
	private final String key;

	/**
	 * オプションの値.
	 */
	private String value;

	/**
	 * 有効かどうか.
	 */
	private boolean enable;

	/**
	 * 説明.
	 */
	private final String description;

	/**
	 * 説明詳細.
	 */
	private final String detail;

	/**
	 * コンストラクタ.
	 * 
	 * @param key オプションのキー値.
	 * @param engine エンジン名.
	 * @param clazz オプションの指定クラス.
	 * @param description 説明
	 * @param detail 説明詳細.
	 */
	public CheckOption(String key, String engine, Class<?> clazz, String description, String detail) {

		this.key = key;
		this.engine = engine;
		this.clazz = clazz;
		this.description = description;
		this.detail = detail;
	}

	/**
	 * オプションの指定クラスを取得する.
	 * 
	 * @return オプションの指定クラス
	 */
	public Class<?> getClazz() {

		return clazz;
	}

	/**
	 * チェッカエンジンを取得する.
	 * 
	 * @return チェッカエンジン
	 */
	public String getEngine() {

		return engine;
	}

	/**
	 * オプションのキー値を取得する.
	 * 
	 * @return オプションのキー値
	 */
	public String getKey() {

		return key;
	}

	/**
	 * オプションの値を取得する.
	 * 
	 * @return オプションの値
	 */
	public String getValue() {

		return value;
	}

	/**
	 * オプションの値.を設定する.
	 * 
	 * @param value オプションの値.
	 */
	public void setValue(String value) {

		this.value = value;
	}

	/**
	 * 有効かどうかを取得する.
	 * 
	 * @return 有効かどうか
	 */
	public boolean isEnable() {

		return enable;
	}

	/**
	 * 有効かどうかを設定する.
	 * 
	 * @param enable 有効かどうか
	 */
	public void setEnable(boolean enable) {

		this.enable = enable;
	}

	/**
	 * 説明を取得する.
	 * 
	 * @return 説明
	 */
	public String getDescription() {

		return description;
	}

	/**
	 * 説明詳細を取得する.
	 * 
	 * @return 説明詳細
	 */
	public String getDetail() {

		return detail;
	}

	@Override
	public String toString() {

		return toStringFromStrings(key, Boolean.toString(enable), value, clazz.getSimpleName(), description,
				detail == null ? "" : detail);
	}

	/**
	 * キーを除いた文字列表現を返す.
	 * 
	 * @return キーを除いた文字列表現.
	 */
	public String toStringExcludeKey() {

		return toStringFromStrings(Boolean.toString(enable), value, clazz.getSimpleName(), description,
				detail == null ? "" : detail);
	}

	/**
	 * 複数文字列を連結し、最後の区切り文字(,)から最後を削除したものを返す.
	 * 
	 * @param strings 連結する複数文字列.
	 * @return 処理後の文字列.
	 */
	private String toStringFromStrings(String... strings) {

		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			sb.append(string);
			sb.append(JSLintPluginConstant.OPTION_SEPARATOR);
		}
		sb.deleteCharAt(sb.lastIndexOf(JSLintPluginConstant.OPTION_SEPARATOR));
		return sb.toString();
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
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((detail == null) ? 0 : detail.hashCode());
		result = prime * result + (enable ? 1231 : 1237);
		result = prime * result + ((engine == null) ? 0 : engine.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof CheckOption)) {
			return false;
		}
		CheckOption other = (CheckOption) obj;
		if (clazz == null) {
			if (other.clazz != null) {
				return false;
			}
		} else if (!clazz.equals(other.clazz)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (detail == null) {
			if (other.detail != null) {
				return false;
			}
		} else if (!detail.equals(other.detail)) {
			return false;
		}
		if (enable != other.enable) {
			return false;
		}
		if (engine == null) {
			if (other.engine != null) {
				return false;
			}
		} else if (!engine.equals(other.engine)) {
			return false;
		}
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		if (clazz != other.getClazz()) {
			return false;
		}
		return true;
	}

}
