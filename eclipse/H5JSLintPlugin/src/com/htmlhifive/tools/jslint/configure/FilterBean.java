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
package com.htmlhifive.tools.jslint.configure;

import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * フィルタの状態をあらわすビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class FilterBean implements Cloneable {

	/**
	 * 有効かどうか.
	 */
	private boolean state;

	/**
	 * 対象正規表現.
	 */
	private String regex = "";

	/**
	 * フィルタレベル.
	 */
	private FilterLevel revel = FilterLevel.IGNORE;

	/**
	 * 有効かどうかを取得する.
	 * 
	 * @return 有効かどうか
	 */
	public boolean isState() {

		return state;
	}

	/**
	 * 有効かどうかを設定する.
	 * 
	 * @param state 有効かどうか
	 */
	public void setState(boolean state) {

		this.state = state;
	}

	/**
	 * 対象正規表現を取得する.
	 * 
	 * @return 対象正規表現
	 */
	public String getRegex() {

		return regex;
	}

	/**
	 * 対象正規表現を設定する.
	 * 
	 * @param regex 対象正規表現
	 */
	public void setRegex(String regex) {

		this.regex = regex;
	}

	/**
	 * フィルタレベルを取得する.
	 * 
	 * @return フィルタレベル
	 */
	public FilterLevel getRevel() {

		return revel;
	}

	/**
	 * フィルタレベルを設定する.
	 * 
	 * @param revel フィルタレベル
	 */
	public void setLevel(FilterLevel revel) {

		this.revel = revel;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(state);
		sb.append(",");
		sb.append(revel.name());
		sb.append(",");
		sb.append(regex);

		return sb.toString();
	}

	@Override
	public Object clone() {

		try {
			FilterBean bean = (FilterBean) super.clone();
			bean.setRegex(regex);
			bean.setLevel(revel);
			bean.setState(state);
			return bean;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}

	}

	/**
	 * 正規表現にマッチした時のフィルタレベル.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	public enum FilterLevel {
		/**
		 * 無視.
		 */
		IGNORE(Messages.CT0000.getText()),
		/**
		 * エラー.
		 */
		ERROR(Messages.CT0001.getText());
		/**
		 * フィルターレベルのラベル.
		 */
		private static final String[] FILTER_LABELS;
		/**
		 * 表記.
		 */
		private final String label;

		static {
			FilterLevel[] revels = values();
			FILTER_LABELS = new String[revels.length];
			for (int i = 0; i < revels.length; i++) {
				FILTER_LABELS[i] = revels[i].getLabel();
			}
		}

		/**
		 * 
		 * コンストラクタ.
		 * 
		 * @param label 表記.
		 */
		private FilterLevel(String label) {

			this.label = label;
		}

		/**
		 * 表記を取得する.
		 * 
		 * @return 表記.
		 */
		public String getLabel() {

			return this.label;
		}

		/**
		 * 全ての表記を取得する.
		 * 
		 * @return 全ての表記.
		 */
		public static String[] getAllLabels() {

			return FILTER_LABELS.clone();
		}

		/**
		 * ラベルからフィルタレベルを取得する.
		 * 
		 * @param label ラベル.
		 * @return フィルタレベル.
		 */
		public static FilterLevel getRevelFromLabel(String label) {

			FilterLevel[] revels = values();
			for (FilterLevel filterRevel : revels) {
				if (filterRevel.getLabel().equals(label)) {
					return filterRevel;
				}
			}
			throw new IllegalArgumentException();
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
		result = prime * result + ((regex == null) ? 0 : regex.hashCode());
		result = prime * result + ((revel == null) ? 0 : revel.hashCode());
		result = prime * result + (state ? 1231 : 1237);
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
		if (!(obj instanceof FilterBean)) {
			return false;
		}
		FilterBean other = (FilterBean) obj;
		if (regex == null) {
			if (other.regex != null) {
				return false;
			}
		} else if (!regex.equals(other.regex)) {
			return false;
		}
		if (revel != other.revel) {
			return false;
		}
		if (state != other.state) {
			return false;
		}
		return true;
	}
}
