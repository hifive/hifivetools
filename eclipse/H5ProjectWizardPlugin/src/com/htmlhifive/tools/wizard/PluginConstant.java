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
 */
package com.htmlhifive.tools.wizard;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.wst.jsdt.internal.ui.wizards.NewWizardMessages;

import com.htmlhifive.tools.wizard.log.messages.Messages;

/**
 * <H3>プラグイン定数定義.</H3>
 * 
 * @author fkubo
 */
public abstract class PluginConstant {

	/** properties. */
	private static final Properties properties;

	/** resource-name. */
	private static final String resourceName = "/hifive-wizard-plugin.properties";

	// 読み込み処理.
	static {
		properties = new Properties();
		try {
			properties.load(PluginConstant.class.getResourceAsStream(resourceName));
		} catch (IOException e) {
			throw new IllegalStateException(Messages.SE0002.format(resourceName), e);
		}

	}

	/** ライブラリ用のURL. */
	public static final String URL_LIBRARY_LIST = properties.getProperty("url.library.list");

	/** ライブラリ用のURL_MIRROR. */
	public static final String URL_LIBRARY_LIST_MIRROR = properties.getProperty("url.library.list.mirror");

	/** ライブラリリスト取得時のタイムアウト設定. */
	public static final int URL_LIBRARY_LIST_CONNECTION_TIMEOUT = NumberUtils.toInt(
			properties.getProperty("url.library.list.connection.timeout"), 3000);

	/** ライブラリ取得時のタイムアウト設定. */
	public static final int URL_LIBRARY_CONNECTION_TIMEOUT = NumberUtils.toInt(
			properties.getProperty("url.library.connection.timeout"), 5000);

	/** JavaProjectWizardFirstPageName. */
	public static final String JavaProjectWizardFirstPageName = NewWizardMessages.JavaProjectWizardFirstPage_page_pageName;
}