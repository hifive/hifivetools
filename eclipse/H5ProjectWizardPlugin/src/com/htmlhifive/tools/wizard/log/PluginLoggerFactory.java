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
package com.htmlhifive.tools.wizard.log;

import java.lang.reflect.InvocationTargetException;

import com.htmlhifive.tools.wizard.log.messages.Messages;

/**
 * Hi5コードアシストプラグインのロガーファクトリクラス.
 * 
 * @author fkubo
 */
public final class PluginLoggerFactory {

	/**
	 * ロガー実装クラス.
	 */
	private static Class<?> impl = Log4JLoggerImpl.class;

	/**
	 * コンストラクタ.
	 */
	private PluginLoggerFactory() {

	}

	/**
	 * ロガーを取得する.
	 * 
	 * @param clazz クラス.
	 * @return ロガー
	 */
	public static PluginLogger getLogger(Class<?> clazz) {

		return getLogger(clazz.getName());
	}

	/**
	 * ロガーを取得する.
	 * 
	 * @param name クラス名.
	 * @return ロガー
	 */
	public static PluginLogger getLogger(String name) {

		try {
			return (PluginLogger) impl.getConstructor(String.class).newInstance(name);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(Messages.SE0001.format(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(Messages.SE0001.format(), e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(Messages.SE0001.format(), e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(Messages.SE0001.format(), e);
		}
	}

	/**
	 * ロガー実装クラスを設定する.
	 * 
	 * @param implClass ロガー実装クラス
	 */
	public static void setImpl(Class<?> implClass) {

		impl = implClass;
	}

}
