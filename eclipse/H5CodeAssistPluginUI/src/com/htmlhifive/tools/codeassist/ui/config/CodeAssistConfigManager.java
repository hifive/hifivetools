/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
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
package com.htmlhifive.tools.codeassist.ui.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * コード補完の設定情報を管理するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class CodeAssistConfigManager {
	/**
	 * プロジェクトと設定ファイルのマップ.
	 */
	private static Map<IProject, CodeAssistConfig> configMap = new HashMap<IProject, CodeAssistConfig>();

	/**
	 * コンストラクタ.
	 */
	private CodeAssistConfigManager() {

		// nocreate
	}

	/**
	 * プロジェクトに紐づく設定ファイルを取得する.<br>
	 * 存在しない場合でも空のインスタンスを取得する.
	 * 
	 * @param project 対象のプロジェクト.
	 * @param reload 設定ファイルを再ロードするかどうか.
	 * @return コードアシスト設定
	 */
	public static CodeAssistConfig getConfig(IProject project, boolean reload) {

		IFile codeassistConf = project.getFile(".codeassist");
		CodeAssistConfig config = configMap.get(project);
		if (!codeassistConf.exists() || config == null) {
			configMap.put(project, new CodeAssistConfig(codeassistConf));
		} else if (config != null && reload) {
			config.load();
		}
		return configMap.get(project);
	}

	/**
	 * コードアシストの設定をセーブする.
	 * 
	 * @param project 対象プロジェクト.
	 * @return 保存が成功したらtrue、そうでない場合はfalse
	 */
	public static boolean saveConfig(IProject project) {

		return configMap.get(project).saveConfig();
	}

}
