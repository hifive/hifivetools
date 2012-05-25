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

import jp.co.nssol.h5.tool.jslint.messages.Messages;

/**
 * 
 * オプションのenumクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public enum JSHintDefaultOptions {
	/**
	 * asi.
	 */
	ASI("asi", Boolean.class, Messages.DES_ASI.getText(), ""),

	/**
	 * bitwise.
	 */
	BITWISE("bitwise", Boolean.class, Messages.DES_BITWISE.getText(), ""),

	/**
	 * boss.
	 */
	BOSS("boss", Boolean.class, Messages.DES_BOSS.getText(), ""),
	/**
	 * browser.
	 */
	BROWSER("browser", Boolean.class, Messages.DES_BROUSER.getText(), ""),
	/**
	 * CouchDB.
	 */
	COUCH("couch", Boolean.class, Messages.DES_COUCH.getText(), ""),
	/**
	 * curly.
	 */
	CURLY("curly", Boolean.class, Messages.DES_CURLY.getText(), ""),
	/**
	 * debug.
	 */
	DEBUG("debug", Boolean.class, Messages.DES_DEBUG.getText(), ""),

	/**
	 * devel.
	 */
	DEVEL("devel", Boolean.class, Messages.DES_DEVEL.getText(), ""),

	/**
	 * eqeqeq.
	 */
	EQEQEQ("eqeqeq", Boolean.class, Messages.DES_EQEQEQ.getText(), ""),
	/**
	 * eqnull.
	 */
	EQNULL("eqnull", Boolean.class, Messages.DES_EQNULL.getText(), ""),
	/**
	 * es5.
	 */
	ES5("es5", Boolean.class, Messages.DES_ES5.getText(), ""),
	/**
	 * evil.
	 */
	EVIL("evil", Boolean.class, Messages.DES_EVIL.getText(), ""),
	/**
	 * forin.
	 */
	FORIN("forin", Boolean.class, Messages.DES_FORIN.getText(), ""),

	/**
	 * immed.
	 */
	IMMED("immed", Boolean.class, Messages.DES_IMMED.getText(), ""),

	/**
	 * jquery.
	 */
	JQUERY("jquery", Boolean.class, Messages.DES_JQUERY.getText(), ""),

	/**
	 * laxbreak.
	 */
	LAXBREAK("laxbreak", Boolean.class, Messages.DES_LAXBREAK.getText(), ""),

	/**
	 * maxerr.
	 */
	MAXERR("maxerr", Integer.class, Messages.DES_MAXERR.getText(), ""),
	/**
	 * MooTools.
	 */
	MOOTOOLS("mootools", Boolean.class, Messages.DES_MOOTOOLS.getText(), ""),
	/**
	 * newcap.
	 */
	NEWCAP("newcap", Boolean.class, Messages.DES_NEWCAP.getText(), Messages.DET_NEWCAP.getText()),
	/**
	 * noarg.
	 */
	NOARG("noarg", Boolean.class, Messages.DES_NOARG.getText(), ""),
	/**
	 * node.
	 */
	NODE("node", Boolean.class, Messages.DES_NODE.getText(), ""),
	/**
	 * noempty.
	 */
	NOEMPTY("noempty", Boolean.class, Messages.DES_NOEMPTY.getText(), ""),
	/**
	 * nomen.
	 */
	NOMEN("nomen", Boolean.class, Messages.DES_NOMEN.getText(), ""),
	/**
	 * nonew.
	 */
	NONEW("nonew", Boolean.class, Messages.DES_NONEW.getText(), ""),
	/**
	 * onevar.
	 */
	ONEVAR("onevar", Boolean.class, Messages.DES_ONEVAR.getText(), ""),

	/**
	 * passfail.
	 */
	PASSFAIL("passfail", Boolean.class, Messages.DES_PASSFAIL.getText(), ""),
	/**
	 * plusplus.
	 */
	PLUSPLUS("plusplus", Boolean.class, Messages.DES_PLUSPLUS.getText(), ""),
	/**
	 * Prototype.js.
	 */
	PROTOTYPEJS("prototypejs", Boolean.class, Messages.DES_PROTOTYPEJS.getText(), ""),
	/**
	 * regexp.
	 */
	REGEXP("regexp", Boolean.class, Messages.DES_REGEXP.getText(), ""),

	/**
	 * rhino.
	 */
	RHINO("rhino", Boolean.class, Messages.DES_RHINO.getText(), ""),
	/**
	 * strict.
	 */
	STRICT("strict", Boolean.class, Messages.DES_STRICT.getText(), ""),

	/**
	 * sub.
	 */
	SUB("sub", Boolean.class, Messages.DES_SUB.getText(), Messages.DET_SUB.getText()),
	/**
	 * undef.
	 */
	UNDEF("undef", Boolean.class, Messages.DES_UNDEF.getText(), ""),
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
	private final String key;

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param key オプションファイルのキー.
	 * @param clazz 設定クラス.
	 * @param description 説明.
	 * @param detail 詳細.
	 */
	private JSHintDefaultOptions(String key, Class<?> clazz, String description, String detail) {

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

		return new CheckOption(this.getKey(), Engine.JSHINT.getKey(), this.getClazz(), this.getDescription(),
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
