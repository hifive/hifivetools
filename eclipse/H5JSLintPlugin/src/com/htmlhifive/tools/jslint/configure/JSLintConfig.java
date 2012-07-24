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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.htmlhifive.tools.jslint.configure.FilterBean.FilterRevel;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * JSLintの設定ファイルを取得、設定するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSLintConfig {

	/**
	 * 設定情報のキープレフィックス.
	 */
	private static final String CONFIG_KEY_PREFIX = "hi5.tool.jslint.";

	/**
	 * jslint.jsファイルパス.
	 */
	private static final String KEY_JSLINT_PATH = CONFIG_KEY_PREFIX + "jslint";

	/**
	 * オプションファイルパス.
	 */
	private static final String KEY_OPTION_PATH = CONFIG_KEY_PREFIX + "option";

	/**
	 * 他プロジェクトの設定を利用するかどうか.
	 */
	private static final String KEY_USE_OTHER_PROJECT = CONFIG_KEY_PREFIX + "use.other.project";

	/**
	 * 他プロジェクトのパス.
	 */
	private static final String KEY_OTHER_PROJECT_PATH = CONFIG_KEY_PREFIX + "other.project";

	/**
	 * ライブラリファイルのパスリスト.
	 */
	private static final String KEY_LIBRARY_LIST = CONFIG_KEY_PREFIX + "lib.list";

	/**
	 * フィルタの正規表現.
	 */
	private static final String KEY_FILTER_REGEX = CONFIG_KEY_PREFIX + "filter.regex";

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JSLintConfig.class);

	/**
	 * 設定プロパティファイル.
	 */
	private IFile configProp;

	/**
	 * 設定ファイルビーン.
	 */
	private final ConfigBean configBean;

	/**
	 * デフォルトのコンフィグビーン.
	 */
	private ConfigBean defaultConfigBean;

	/**
	 * コンストラクタ.
	 * 
	 * @param configProp 設定プロパティファイル.
	 */
	JSLintConfig(IFile configProp) {

		this.configProp = configProp;
		configBean = new ConfigBean();
		load();

	}

	/**
	 * 設定情報ファイルからbeanをロードする.
	 * 
	 * @return
	 */
	void load() {

		try {
			configProp.refreshLocal(IResource.DEPTH_ZERO, null);
			if (configProp.exists()) {
				Properties properties = new Properties();
				properties.load(configProp.getContents());
				configBean.setJsLintPath(properties.getProperty(KEY_JSLINT_PATH, ""));
				configBean.setOptionFilePath(properties.getProperty(KEY_OPTION_PATH, ""));
				configBean.setUseOtherProject(Boolean.parseBoolean(properties.getProperty(KEY_USE_OTHER_PROJECT,
						"false")));
				configBean.setOtherProjectPath(properties.getProperty(KEY_OTHER_PROJECT_PATH, ""));
				configBean.setLibList(StringUtils.split(properties.getProperty(KEY_LIBRARY_LIST, ""), ","));
				int i = 0;
				String filterStr = null;
				while ((filterStr = properties.getProperty(KEY_FILTER_REGEX + i)) != null) {
					configBean.addFilterBean(convertToFilterBean(filterStr));
					i++;
				}

			}
			defaultConfigBean = configBean.clone();
		} catch (CoreException e) {
			logger.put(Messages.EM0100, e);
		} catch (IOException e) {
			logger.put(Messages.EM0100, e);
		}

	}

	/**
	 * 文字列から、フィルタービーンに変換する.
	 * 
	 * @param property 変換前文字列
	 * @return フィルタービーン.
	 */
	private FilterBean convertToFilterBean(String property) {

		FilterBean bean = new FilterBean();
		String[] strs = StringUtils.split(property, ",", 3);
		bean.setState(Boolean.valueOf(strs[0]));
		bean.setRevel(FilterRevel.valueOf(strs[1]));
		if (strs.length == 3) {
			bean.setRegex(strs[2]);
		} else {
			bean.setRegex("");
		}

		return bean;
	}

	/**
	 * 設定された情報を.jslintに保存する.
	 */
	void store() {

		Properties properties = new Properties();
		properties.setProperty(KEY_JSLINT_PATH, configBean.getJsLintPath());
		properties.setProperty(KEY_OPTION_PATH, configBean.getOptionFilePath());
		properties.setProperty(KEY_USE_OTHER_PROJECT, Boolean.toString(configBean.isUseOtherProject()));
		properties.setProperty(KEY_OTHER_PROJECT_PATH, configBean.getOtherProjectPath());
		properties.setProperty(KEY_LIBRARY_LIST, StringUtils.join(configBean.getLibList(), ","));
		// properties.setProperty(KEY_USE_FILTER,
		// Boolean.toString(configBean.isUseFilter()));
		FilterBean[] beans = configBean.getFilterBeans();
		for (int i = 0; i < beans.length; i++) {
			properties.setProperty(KEY_FILTER_REGEX + i, beans[i].toString());
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		InputStream input = null;
		try {
			properties.store(output, "");
			output.flush();
			input = new ByteArrayInputStream(output.toByteArray());
			configProp.refreshLocal(IResource.DEPTH_ZERO, null);

			if (configProp.exists()) {
				configProp.setContents(input, false, false, null);
			} else {
				configProp.create(input, false, null);
			}
		} catch (CoreException e) {
			logger.put(Messages.EM0100, e);
		} catch (IOException e) {
			logger.put(Messages.EM0100, e);
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
		}
	}

	/**
	 * 設定ファイルビーンを取得する.
	 * 
	 * @return 設定ファイルビーン.
	 */
	ConfigBean getConfigBean() {

		return configBean;
	}

	/**
	 * デフォルトコンフィグビーンを取得する.
	 * 
	 * @return デフォルトコンフィグビーン
	 */
	ConfigBean getDefaultConfigBean() {

		return defaultConfigBean;
	}

}
