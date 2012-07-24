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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * 設定ファイルを管理するクラス.設定ファイルの操作はこのクラスを通して行う.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class JSLintConfigManager {

	/**
	 * プロジェクトと設定ファイルのマップ.
	 */
	private static Map<IProject, JSLintConfig> configMap = new HashMap<IProject, JSLintConfig>();

	/**
	 * コンストラクタ.
	 */
	private JSLintConfigManager() {

	}

	/**
	 * プロジェクトの設定ファイルオブジェクトを返す.
	 * 
	 * @param project プロジェクトに紐づいた設定ファイル.
	 * @return 設定ファイルオブジェクト
	 */
	public static ConfigBean getConfigBean(IProject project) {

		return getConfig(project).getConfigBean();
	}

	/**
	 * プロジェクトに紐づく設定ファイルを取得する.<br>
	 * 存在しない場合でも空のインスタンスを取得する.
	 * 
	 * @param project 対象のプロジェクト.
	 * @return JSLint設定
	 */
	public static JSLintConfig getConfig(IProject project) {

		IFile jslintConf = project.getFile(".jslint");
		JSLintConfig config = configMap.get(project);
		if (config != null) {
			return config;
		}
		if (!jslintConf.exists()) {
			configMap.put(project, new JSLintConfig(jslintConf));
		} else if (!configMap.containsKey(project)) {
			configMap.put(project, new JSLintConfig(jslintConf));
		}
		return configMap.get(project);
	}

	/**
	 * プロジェクトと設定ファイルを設定する.
	 * 
	 * @param project プロジェクト
	 * @param config 設定ファイル
	 */
	public static void putConfig(IProject project, JSLintConfig config) {

		configMap.put(project, config);

	}

	/**
	 * 設定ファイルをロードする.
	 * 
	 * @param project 対象プロジェクト
	 */
	public static void loadConfig(IProject project) {

		getConfig(project).load();
	}

	/**
	 * 設定ファイルを保存する.
	 * 
	 * @param project 対象プロジェクト
	 */
	public static void saveConfig(IProject project) {

		getConfig(project).store();
	}

	/**
	 * デフォルトのコンフィグビーンを取得する.
	 * 
	 * @param project 対象プロジェクト.
	 * @return デフォルトコンフィグビーン
	 */
	public static ConfigBean getDefaultConfigBean(IProject project) {

		return getConfig(project).getDefaultConfigBean();
	}

}
