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
package com.htmlhifive.tools.codeassist.core.logger;

import java.lang.reflect.InvocationTargetException;

import com.htmlhifive.tools.codeassist.core.logger.impl.DefaultLoggerImpl;
import com.htmlhifive.tools.codeassist.core.messages.Messages;


/**
 * Hi5コードアシストプラグインのロガーファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class H5CodeAssistPluginLoggerFactory {

	/**
	 * ロガー実装クラス.
	 */
	private static Class<?> impl = DefaultLoggerImpl.class;

	/**
	 * コンストラクタ.
	 */
	private H5CodeAssistPluginLoggerFactory() {

	}

	/**
	 * 
	 * ロガーを取得する.
	 * 
	 * @param clazz クラス.
	 * @return ロガー
	 */
	public static H5CodeAssistPluginLogger getLogger(Class<?> clazz) {

		return getLogger(clazz.getName());
	}

	/**
	 * 
	 * ロガーを取得する.
	 * 
	 * @param name クラス名.
	 * @return ロガー
	 */
	public static H5CodeAssistPluginLogger getLogger(String name) {

		try {
			return (H5CodeAssistPluginLogger) impl.getConstructor(String.class).newInstance(name);
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(Messages.EM0004.getText());
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(Messages.EM0004.getText());
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(Messages.EM0004.getText());
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(Messages.EM0004.getText());
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
