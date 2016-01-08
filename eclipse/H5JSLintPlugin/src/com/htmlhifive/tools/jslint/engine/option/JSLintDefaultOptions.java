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
package com.htmlhifive.tools.jslint.engine.option;

import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * 
 * オプションのenumクラス. TODO jslint対応時
 * 
 * @author NS Solutions Corporation
 * 
 */
public enum JSLintDefaultOptions {
	/**
	 * bitwise.
	 */
	BITWISE("bitwise", Boolean.class, Messages.DES_BITWISE.getText(), ""),
	/**
	 * browser.
	 */
	BROWSER("browser", Boolean.class, Messages.DES_BROUSER.getText(), ""),
	/**
	 * couch.
	 */
	COUCH("couch", Boolean.class, Messages.DES_COUCH.getText(), ""),
	/**
	 * devel.
	 */
	DEVEL("devel", Boolean.class, Messages.DES_DEVEL.getText(), ""),
	/**
	 * es6.
	 */
	ES5("es6", Boolean.class, Messages.DES_ES6.getText(), ""),
	/**
	 * eval.
	 */
	EVAL("eval", Boolean.class, Messages.DES_EVAL.getText(), ""),
	/**
	 * for.
	 */
	FOR("for", Boolean.class, Messages.DES_FOR.getText(), ""),
	/**
	 * fudge.
	 */
	FUDGE("fudge", Boolean.class, Messages.DES_FUDGE.getText(), Messages.DET_FUDGE.getText()),
	/**
	 * maxerr.
	 */
	MAXERR("maxerr", Integer.class, Messages.DES_MAXERR.getText(), ""),
	/**
	 * maxlen.
	 */
	MAXLEN("maxlen", Integer.class, Messages.DES_MAXLEN.getText(), ""),
	/**
	 * node.
	 */
	NODE("node", Boolean.class, Messages.DES_NODE.getText(), ""),
	/**
	 * this.
	 */
	THIS("this", Boolean.class, Messages.DES_THIS.getText(), ""),
	/**
	 * white.
	 */
	WHITE("white", Boolean.class, Messages.DES_WHITE.getText(), "");

	/**
	 * 設定クラス.
	 */
	private final Class<?> clazz;

	/**
	 * 説明.
	 */
	private final String description;

	/**
	 * 詳細.
	 */
	private final String detail;

	/**
	 * オプションファイルのキー.
	 */
	private String key;

	/**
	 * コンストラクタ.
	 * 
	 * @param key オプションファイルのキー.
	 * @param clazz 設定クラス.
	 * @param description 説明.
	 * @param detail 詳細.
	 */
	private JSLintDefaultOptions(String key, Class<?> clazz, String description, String detail) {

		this.key = key;
		this.clazz = clazz;
		this.description = description;
		this.detail = detail;
	}

	/**
	 * Optionに変換する.<br>
	 * enableおよびvalueはnull
	 * 
	 * @return Option型
	 */
	public CheckOption convertToOption() {

		return new CheckOption(this.getKey(), Engine.JSLINT.getKey(), this.getClazz(), this.getDescription(),
				this.getDetail());
	}

	/**
	 * 設定クラスを取得する.
	 * 
	 * @return 設定クラス.
	 */
	public Class<?> getClazz() {

		return clazz;
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
	 * 詳細を取得する.
	 * 
	 * @return 詳細
	 */
	public String getDetail() {

		return detail;
	}

	/**
	 * オプションファイルのキーを取得する.
	 * 
	 * @return オプションファイルのキー
	 */
	public String getKey() {

		return key;
	}
}
