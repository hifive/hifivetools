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
package com.htmlhifive.tools.codeassist.core.logger.impl;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.LogLevel;
import com.htmlhifive.tools.codeassist.core.messages.MessagesBase.Message;

/**
 * ロガー実装.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class DefaultLoggerImpl implements H5CodeAssistPluginLogger {

	/**
	 * ロガー実体.
	 */
	private Logger logger;

	/**
	 * コンストラクタ.
	 * 
	 * @param className 出力クラス名.
	 */
	public DefaultLoggerImpl(String className) {

		logger = Logger.getLogger(className);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param outputClazz 出力クラス.
	 */
	public DefaultLoggerImpl(Class<?> outputClazz) {

		this(outputClazz.getName());
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * jp.co.nssol.hi5.tool.jslint.logger.JSLintPluginLogger#put(jp.co.nssol
	 * .hi5.tool.jslint.messages.MessagesBase.Message, java.lang.Throwable,
	 * java.lang.Object[])
	 */
	@Override
	public void log(Message message, Throwable e, Object... params) {

		if (isEnabledFor(message.getLevel())) {
			logger.log(convertInnerLoglevel(message.getLevel()), message.format(params), e);
		}

	}

	/**
	 * クラス内のログレベルに変換する.
	 * 
	 * @param level ログレベル.
	 * @return このクラスで利用されているログレベル.
	 */
	private Priority convertInnerLoglevel(LogLevel level) {

		return Level.toLevel(level.name());
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * jp.co.nssol.hi5.tool.jslint.logger.JSLintPluginLogger#put(jp.co.nssol
	 * .hi5.tool.jslint.messages.MessagesBase.Message, java.lang.Object[])
	 */
	@Override
	public void log(Message message, Object... params) {

		log(message, null, params);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * jp.co.nssol.hi5.tool.jslint.logger.JSLintPluginLogger#debug(java.lang
	 * .Object)
	 */
	@Override
	public void debug(Object message) {

		logger.debug(message);

	}

	@Override
	public boolean isEnabledFor(LogLevel level) {

		return logger.isEnabledFor(convertInnerLoglevel(level));
	}

	@Override
	public void setLevel(LogLevel level) {

	}

	@Override
	public LogLevel getLevel() {

		return null;
	}
}
