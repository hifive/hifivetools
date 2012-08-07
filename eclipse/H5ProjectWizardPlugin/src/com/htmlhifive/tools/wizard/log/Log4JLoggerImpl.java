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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.htmlhifive.tools.wizard.log.messages.MessagesBase.Message;

/**
 * Log4Jロガー実装.
 * 
 * @author fkubo
 */
public class Log4JLoggerImpl implements PluginLogger {
	/** callerFQCN. */
	private static final String callerFQCN = Log4JLoggerImpl.class.getName();
	/**
	 * ロガー実体.
	 */
	private final Logger logger;

	/**
	 * コンストラクタ.
	 * 
	 * @param className 出力クラス名.
	 */
	public Log4JLoggerImpl(String className) {

		logger = Logger.getLogger(className);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param outputClazz 出力クラス.
	 */
	public Log4JLoggerImpl(Class<?> outputClazz) {

		this(outputClazz.getName());
	}

	/*
	 * (非 Javadoc)
	 * @see com.htmlhifive.tools.wizard.log.PluginLogger#log(com.htmlhifive.tools .wizard.messages.MessagesBase.Message,
	 * java.lang.Throwable, java.lang.Object[])
	 */
	@Override
	public void log(Message message, Throwable e, Object... params) {

		if (isEnabledFor(message.getLevel())) {
			//logger.log(convertInnerLoglevel(message.getLevel()), message.format(params), e);
			logger.log(callerFQCN, convertInnerLoglevel(message.getLevel()), message.format(params), e);
		}

	}

	/**
	 * クラス内のログレベルに変換する.
	 * 
	 * @param level ログレベル.
	 * @return このクラスで利用されているログレベル.
	 */
	private Level convertInnerLoglevel(LogLevel level) {

		return Level.toLevel(level.name());
	}

	/*
	 * (非 Javadoc)
	 * @see com.htmlhifive.tools.wizard.log.PluginLogger#log(com.htmlhifive.tools .wizard.messages.MessagesBase.Message,
	 * java.lang.Object[])
	 */
	@Override
	public void log(Message message, Object... params) {

		log(message, null, params);
	}

	/*
	 * (非 Javadoc)
	 * @see com.htmlhifive.tools.wizard.log.PluginLogger#debug(java.lang.Object)
	 */
	@Override
	public void debug(Object message) {

		logger.debug(message);

	}

	/*
	 * (非 Javadoc)
	 * @see com.htmlhifive.tools.wizard.log.PluginLogger#isEnabledFor(jp.co.nssol .h5.tools.wizard.log.LogLevel)
	 */
	@Override
	public boolean isEnabledFor(LogLevel level) {

		return logger.isEnabledFor(convertInnerLoglevel(level));
	}

	/*
	 * (非 Javadoc)
	 * @see com.htmlhifive.tools.wizard.log.PluginLogger#setLevel(jp.co.nssol.h5. tools.wizard.log.LogLevel)
	 */
	@Override
	public void setLevel(LogLevel level) {

		logger.setLevel(convertInnerLoglevel(level));
	}

	/*
	 * (非 Javadoc)
	 * @see com.htmlhifive.tools.wizard.log.PluginLogger#getLevel()
	 */
	@Override
	public LogLevel getLevel() {

		return LogLevel.valueOf(logger.getLevel().toString());
	}
}
