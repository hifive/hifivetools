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
 * オプションのenumクラス. TODO jslint対応時
 * 
 * @author NS Solutions Corporation
 * 
 */
public enum JSLintDefaultOptions {
	/**
	 * adsafe.
	 */
	ADSAFE("adsafe", Boolean.class, Messages.DES_ADSAFE.getText(), ""),
	/**
	 * bitwise.
	 */
	BITWISE("bitwise", Boolean.class, Messages.DES_BITWISE.getText(), ""),
	/**
	 * browser.
	 */
	BROWSER("browser", Boolean.class, Messages.DES_BROUSER.getText(), ""),
	/**
	 * cap.
	 */
	CAP("cap", Boolean.class, Messages.DES_CAP.getText(), ""),
	/**
	 * confusion.
	 */
	CONFUSION("confusion", Boolean.class, Messages.DES_CONFUSION.getText(), ""),
	/**
	 * 'continue'.
	 */
	CONTINUE("'continue'", Boolean.class, Messages.DES_CONTINUE.getText(), ""),
	/**
	 * css.
	 */
	CSS("css", Boolean.class, Messages.DES_CSS.getText(), ""),
	/**
	 * debug.
	 */
	DEBUG("debug", Boolean.class, Messages.DES_DEBUG.getText(), ""),
	/**
	 * devel.
	 */
	DEVEL("devel", Boolean.class, Messages.DES_DEVEL.getText(), ""),
	/**
	 * eqeq.
	 */
	EQEQ("eqeq", Boolean.class, Messages.DES_EQEQ.getText(), ""),
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
	 * fragment.
	 */
	FRAGMENT("fragment", Boolean.class, Messages.DES_FRAGMENT.getText(), ""),
	/**
	 * indent.
	 */
	INDENT("indent", Integer.class, Messages.DES_INDENT.getText(), ""),
	/**
	 * maxerr.
	 */
	MAXERR("maxerr", Integer.class, Messages.DES_MAXERR.getText(), ""),
	/**
	 * maxlen.
	 */
	MAXLEN("maxlen", Integer.class, Messages.DES_MAXLEN.getText(), ""),
	/**
	 * newcap.
	 */
	NEWCAP("newcap", Boolean.class, Messages.DES_NEWCAP.getText(), Messages.DET_NEWCAP.getText()),
	/**
	 * node.
	 */
	NODE("node", Boolean.class, Messages.DES_NODE.getText(), ""),
	/**
	 * nomen.
	 */
	NOMEN("nomen", Boolean.class, Messages.DES_NOMEN.getText(), ""),
	/**
	 * on.
	 */
	ON("on", Boolean.class, Messages.DES_ON.getText(), ""),
	/**
	 * passfail.
	 */
	PASSFAIL("passfail", Boolean.class, Messages.DES_PASSFAIL.getText(), ""),
	/**
	 * plusplus.
	 */
	PLUSPLUS("plusplus", Boolean.class, Messages.DES_PLUSPLUS.getText(), ""),
	/**
	 * properties.
	 */
	PROPERTIES("properties", Boolean.class, Messages.DES_PROPERTIES.getText(), ""),
	/**
	 * regexp.
	 */
	REGEXP("regexp", Boolean.class, Messages.DES_REGEXP.getText(), ""),
	/**
	 * rhino.
	 */
	RHINO("rhino", Boolean.class, Messages.DES_RHINO.getText(), ""),
	/**
	 * safe.
	 */
	SAFE("safe", Boolean.class, Messages.DES_SAFE.getText(), ""),
	/**
	 * sloppy.
	 */
	SLOPPY("sloppy", Boolean.class, Messages.DES_SLOPPY.getText(), ""),
	/**
	 * sub.
	 */
	SUB("sub", Boolean.class, Messages.DES_SUB.getText(), ""),
	/**
	 * undef.
	 */
	UNDEF("undef", Boolean.class, Messages.DES_UNDEF.getText(), ""),
	/**
	 * unparam.
	 */
	UNPARAM("unparam", Boolean.class, Messages.DES_UNPARAM.getText(), ""),
	/**
	 * vars.
	 */
	VARS("vars", Boolean.class, Messages.DES_VARS.getText(), ""),
	/**
	 * white.
	 */
	WHITE("white", Boolean.class, Messages.DES_WHITE.getText(), ""),
	/**
	 * widget.
	 */
	WIDGET("widget", Boolean.class, Messages.DES_WIDGET.getText(), ""),

	/**
	 * windows.
	 */
	WINDOWS("windows", Boolean.class, Messages.DES_WINDOWS.getText(), "");

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
