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
package com.htmlhifive.tools.jslint.engine.option;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.htmlhifive.tools.jslint.JSLintPluginConstant;

/**
 * 
 * jsHint,jsLintのオプションプロパティファイルのラッパークラス.<br>
 * プロパティファイルは<br>
 * キー："jshint,jslintのオプションのキー"<br>
 * 値："有効かどうか","値","値の型","説明","説明詳細"<br>
 * という形式で保持される.<br>
 * このクラスのキーは全てjshintやjslintオプションのキーとなる。<br>
 * (プロパティのキーではない。)<br>
 * ex.<br>
 * browser = true,true,Boolean,ブラウザー標準を想定するか,ブラウザであらかじめ定義されているオブジェクトは以下の通り....
 * TODO このクラスはJSHint用
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CheckOptionPropertyWrapper implements CheckOptionFileWrapper {

	/**
	 * プロパティファイルのキープレフィックス.
	 */
	private String prefix;
	/**
	 * オプションファイル.
	 */
	private Properties optionProp;

	/**
	 * コンストラクタ.オプションのキーのプレフィックスを設定できる.
	 * 
	 * @param optionProp オプションファイル.
	 */
	public CheckOptionPropertyWrapper(Properties optionProp) {

		this.optionProp = optionProp;
		this.prefix = "";
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.engine.option.CheckOptionWrapper#getOption
	 * (java.lang.String)
	 */
	@Override
	public CheckOption getOption(String key, String engine) {

		String rawValue = optionProp.getProperty(getPrefix() + key);
		String[] valueClass = StringUtils.splitPreserveAllTokens(rawValue, JSLintPluginConstant.OPTION_SEPARATOR);
		Class<?> clazz = null;
		if (StringUtils.equals(valueClass[2], "Boolean")) {
			clazz = Boolean.class;
		} else if (StringUtils.equals(valueClass[2], "Integer")) {
			clazz = Integer.class;
		}
		// TODO プロパティファイルの方
		CheckOption option = new CheckOption(key, "", clazz, valueClass[3], valueClass[4]);

		option.setValue(valueClass[1]);
		option.setEnable(Boolean.valueOf(valueClass[0]));
		return option;
	}

	/**
	 * jslint(jshint)オプションのキーの使用するオプションファイルのプレフィックスを取得する.
	 * 
	 * @return オプションファイルのプレフィックス.
	 */
	protected String getPrefix() {

		return prefix;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.engine.option.CheckOptionWrapper#getOptions()
	 */
	@Override
	public CheckOption[] getOptions(Engine engine) {

		return getOptions(false);
	}

	@Override
	public CheckOption[] getEnableOptions(Engine engine) {

		return getOptions(true);
	}

	/**
	 * オプションを取得する.
	 * 
	 * @param enable 有効状態のみ取得するかどうか.
	 * @return チェックオプション.
	 */
	private CheckOption[] getOptions(boolean enable) {

		Set<Object> keySet = optionProp.keySet();
		List<CheckOption> optionList = new ArrayList<CheckOption>();
		for (Object obj : keySet) {
			String key = (String) obj;
			if (StringUtils.startsWith(key, getPrefix())) {
				String optionKey = StringUtils.remove(key, getPrefix());
				CheckOption option = getOption(optionKey, "engine");
				if (!enable) {
					optionList.add(option);
				} else if (option.isEnable()) {
					optionList.add(option);
				}

			}

		}
		return (CheckOption[]) optionList.toArray(new CheckOption[optionList.size()]);
	}

	@Override
	public void saveOption() {

		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void addOption(CheckOption option) {

		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void updateOption(CheckOption option) {

		// TODO 自動生成されたメソッド・スタブ

	}

}
