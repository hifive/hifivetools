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

/**
 * 
 * jsチェッカのエンジンを表すクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public enum Engine {
	/**
	 * JSLint.
	 */
	JSLINT("jslint"),
	/**
	 * JSHint.
	 */
	JSHINT("jshint"),
	/**
	 * 全エンジン.
	 */
	ALL("all");

	/**
	 * キー値.
	 */
	private final String key;

	/**
	 * コンストラクタ.
	 * 
	 * @param key キー値.
	 */
	private Engine(String key) {

		this.key = key;
	}

	/**
	 * キー値を取得する.
	 * 
	 * @return キー値.
	 */
	public String getKey() {

		return key;
	}

	/**
	 * エンジンのファイル名を取得する.
	 * 
	 * @return エンジンファイル名.
	 */
	public String getFileName() {
		return key + ".js";
	}

	/**
	 * パスからエンジンを取得する. 該当しない場合はnullを返す.
	 * 
	 * @param jsLintPath エンジンファイルのパス.
	 * @return 対応したエンジン.
	 */
	public static Engine getEngine(String jsLintPath) {

		Engine[] engines = values();
		for (Engine engine : engines) {
			if (jsLintPath.contains(engine.getKey())) {
				return engine;
			}
		}

		return null;
	}

}
