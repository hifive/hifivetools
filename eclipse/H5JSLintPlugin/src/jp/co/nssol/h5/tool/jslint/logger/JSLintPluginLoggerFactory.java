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
 package jp.co.nssol.h5.tool.jslint.logger;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import jp.co.nssol.h5.tool.jslint.logger.impl.JSLintPluginLoggerImpl;

import org.apache.log4j.PropertyConfigurator;

/**
 * ロガーファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class JSLintPluginLoggerFactory {

	/**
	 * ロガー実装クラス.
	 */
	private static Class<? extends JSLintPluginLogger> loggerImpl = JSLintPluginLoggerImpl.class;

	static {
		URL configURL = JSLintPluginLoggerFactory.class.getResource("/log4j.properties");
		PropertyConfigurator.configure(configURL);
	}

	/**
	 * コンストラクタ.
	 */
	private JSLintPluginLoggerFactory() {

	}

	/**
	 * ロガーを取得する.
	 * 
	 * @param clazz 出力クラス
	 * @return JSLintプラグインロガー
	 */
	public static final JSLintPluginLogger getLogger(Class<?> clazz) {

		try {
			return loggerImpl.getConstructor(Class.class).newInstance(clazz);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return new JSLintPluginLoggerImpl(clazz);

	}

	/**
	 * ロガー実装クラスを設定する.
	 * 
	 * @param clazz 実装ロガークラス.
	 */
	public static final void setLogger(Class<? extends JSLintPluginLogger> clazz) {

		loggerImpl = clazz;
	}

}
