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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.htmlhifive.tools.jslint.JSLintPluginConstant;
import com.htmlhifive.tools.jslint.configure.ConfigBean;
import com.htmlhifive.tools.jslint.engine.option.CheckOption;
import com.htmlhifive.tools.jslint.engine.option.JSHintDefaultOptions;
import com.htmlhifive.tools.jslint.engine.option.JSLintDefaultOptions;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * コンフィグビーンのユーティリティクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class ConfigBeanUtil {

	/**
	 * コンストラクタ.
	 */
	private ConfigBeanUtil() {

	}

	/**
	 * デフォルトで定義されたオプションから、指定した型のオプションを取得する.<br>
	 * TODO getAllOptionFromDefaultにするか...
	 * 
	 * @return オプション
	 */
	public static CheckOption[] getAllJsHintOptionFromDefault() {

		JSHintDefaultOptions[] jsHintOptions = JSHintDefaultOptions.values();
		JSLintDefaultOptions[] jsLintOptions = JSLintDefaultOptions.values();
		Set<CheckOption> optionSet = new HashSet<CheckOption>();
		for (JSHintDefaultOptions option : jsHintOptions) {
			optionSet.add(option.convertToOption());
		}
		for (JSLintDefaultOptions option : jsLintOptions) {
			optionSet.add(option.convertToOption());
		}
		return (CheckOption[]) optionSet.toArray(new CheckOption[optionSet.size()]);
	}

	/**
	 * デフォルトで定義されたオプションから、指定した型のオプションを取得する.<br>
	 * 
	 * @param clazz 取得するクラス
	 * @return オプション
	 */
	public static CheckOption[] getJsHintOptionFromDefault(Class<?> clazz) {

		JSHintDefaultOptions[] options = JSHintDefaultOptions.values();
		List<CheckOption> optionList = new ArrayList<CheckOption>();
		for (JSHintDefaultOptions option : options) {
			if (option.getClazz() == clazz) {
				optionList.add(option.convertToOption());
			}
		}
		return (CheckOption[]) optionList.toArray(new CheckOption[optionList.size()]);
	}

	/**
	 * デフォルトで定義されたオプションから、指定した型のオプションを取得する.<br>
	 * 
	 * @param clazz 取得するクラス
	 * @return オプション
	 */
	public static CheckOption[] getJsLintOptionFromDefault(Class<?> clazz) {

		JSLintDefaultOptions[] options = JSLintDefaultOptions.values();
		List<CheckOption> optionList = new ArrayList<CheckOption>();
		for (JSLintDefaultOptions option : options) {
			if (option.getClazz() == clazz) {
				optionList.add(option.convertToOption());
			}
		}
		return (CheckOption[]) optionList.toArray(new CheckOption[optionList.size()]);
	}

	/**
	 * ビーンから指定した型のオプションを取得する.
	 * 
	 * @param bean コンフィグビーン.
	 * @param clazz オプションのクラス.
	 * @return ビーンから取得したオプション.
	 */
	public static CheckOption[] getJsHintOptionFromBean(ConfigBean bean, Class<?> clazz) {

		CheckOption[] options = bean.getJsHintOptionList();
		return pickUpOption(options, clazz);
	}

	/**
	 * ビーンから指定した型のオプションを取得する.
	 * 
	 * @param bean コンフィグビーン.
	 * @param clazz オプションのクラス.
	 * @return ビーンから取得したオプション.
	 */
	public static CheckOption[] getJsLintOptionFromBean(ConfigBean bean, Class<?> clazz) {

		CheckOption[] options = bean.getJsLintOptionList();
		return pickUpOption(options, clazz);

	}

	/**
	 * オプションから指定したクラスの型で表現されるものだけ抽出する.
	 * 
	 * @param options 抽出されるオプション
	 * @param clazz 指定クラス.
	 * @return 抽出後のオプション.
	 */
	private static CheckOption[] pickUpOption(CheckOption[] options, Class<?> clazz) {

		List<CheckOption> optionList = new ArrayList<CheckOption>();
		for (CheckOption option : options) {
			if (option.getClazz() == clazz) {
				optionList.add(option);
			}
		}
		return (CheckOption[]) optionList.toArray(new CheckOption[optionList.size()]);
	}

	/**
	 * 基本設定ページの入力チェック.
	 * 
	 * @param changedBean 変更されたコンフィグビーン.
	 * @return エラーメッセージ.
	 */
	public static String[] checkProperty(ConfigBean changedBean) {

		List<String> errorMessageList = new ArrayList<String>();
		if (changedBean.isUseOtherProject() && StringUtils.isEmpty(changedBean.getOtherProjectPath())) {
			errorMessageList.add(Messages.DM0001.format(Messages.DL0000.getText()));
		}
		if (!changedBean.isUseOtherProject()) {
			if (!PluginResourceUtils.isExistFile(changedBean.getJsLintPath())
					|| !StringUtils.endsWith(changedBean.getJsLintPath(), JSLintPluginConstant.EXTENTION_JS)) {
				errorMessageList.add(Messages.EM0000.format(Messages.E0000.getText()));
			}
			if (StringUtils.isNotEmpty(changedBean.getOptionFilePath())
					&& (!PluginResourceUtils.isExistFile(changedBean.getOptionFilePath()) || !StringUtils.endsWith(
							changedBean.getOptionFilePath(), JSLintPluginConstant.EXTENTION_OPTION))) {
				errorMessageList.add(Messages.EM0000.format(Messages.E0001.getText()));
			}
		}
		return (String[]) errorMessageList.toArray(new String[errorMessageList.size()]);
	}

}
