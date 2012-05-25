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
 package jp.co.nssol.h5.tool.jslint.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import jp.co.nssol.h5.tool.jslint.logger.LogLevel;

import org.apache.commons.lang.StringUtils;

/**
 * Messagesのベースクラス.
 * 
 * @author NS Solutions Corporation
 */
public abstract class MessagesBase {
	/**
	 * デフォルトのバンドルリソース.
	 */
	private static final List<ResourceBundle> RESOURCE_BUNDLES = Collections
			.synchronizedList(new ArrayList<ResourceBundle>());

	/**
	 * デフォルトコンストラクタ.
	 */
	protected MessagesBase() {

		// no action
	}

	/**
	 * サブシステム用のメッセージを追加する.
	 * 
	 * @param bundleName バンドル名.
	 */
	protected static void addResourceBundleString(String bundleName) {

		synchronized (RESOURCE_BUNDLES) {
			ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
			RESOURCE_BUNDLES.add(bundle);
		}
	}

	/**
	 * サブシステム用のメッセージを追加する.
	 * 
	 * @param bundle バンドル.
	 */
	protected static void addResourceBundle(ResourceBundle bundle) {

		synchronized (RESOURCE_BUNDLES) {
			RESOURCE_BUNDLES.add(bundle);
		}
	}

	/**
	 * メッセージオブジェクトを作成する.
	 * 
	 * @param key メッセージのキー
	 * @return メッセージオブジェクト
	 */
	protected static Message createMessage(String key) {

		return new Message(key);
	}

	/**
	 * パスに応じて必要なリソースバンドルを取得する.
	 * 
	 * @param key メッセージのキー
	 * @return リソースバンドルより取得した文字列
	 */
	private static String getResourceBundleString(String key) {

		synchronized (RESOURCE_BUNDLES) {
			for (ResourceBundle resourceBundle : RESOURCE_BUNDLES) {
				if (resourceBundle.containsKey(key)) {
					return resourceBundle.getString(key);
				}
			}
			return null;
		}
	}

	/**
	 * <H3>メッセージを表すクラス.</H3>
	 * 
	 * @author MessageGenerator
	 */
	public static final class Message {

		/**
		 * メッセージキー.
		 */
		private final String key;

		/**
		 * メッセージ内容.
		 */
		private final String text;

		/**
		 * ログレベル.
		 */
		private final LogLevel level;

		/**
		 * コンストラクタ.
		 * 
		 * @param key メッセージキー
		 */
		private Message(String key) {

			this.key = key;
			String rawStr = getResourceBundleString(key);
			String[] strArray = StringUtils.split(rawStr, ",", 2);
			this.level = LogLevel.valueOf(strArray[0]);
			this.text = strArray[1];

		}

		/**
		 * メッセージキーを取得する.
		 * 
		 * @return メッセージキー
		 */
		public String getKey() {

			return key;
		}

		/**
		 * フォーマットされていないメッセージを返す.
		 * 
		 * @return フォーマットされていないメッセージ.
		 */
		public String getText() {

			return text;
		}

		/**
		 * ログレベルを返す.
		 * 
		 * @return ログレベル.
		 */
		public LogLevel getLevel() {

			return level;
		}

		/**
		 * フォーマットされたメッセージを返す.
		 * 
		 * @param params パラメータ.
		 * @return フォーマットされたメッセージ.
		 */
		public String format(Object... params) {

			return MessageFormat.format(text, params);
		}
	}

}
