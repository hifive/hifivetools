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
package jp.co.nssol.h5.tool.jslint.logger.impl;

import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger;
import jp.co.nssol.h5.tool.jslint.logger.LogLevel;
import jp.co.nssol.h5.tool.jslint.messages.MessagesBase.Message;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * JSLintのデフォルトロガー.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSLintPluginLoggerImpl implements JSLintPluginLogger {
	/**
	 * ロガー実体.
	 */
	private Logger logger;

	/**
	 * コンストラクタ.
	 * 
	 * @param outputClazz 出力クラス.
	 */
	public JSLintPluginLoggerImpl(Class<?> outputClazz) {

		logger = Logger.getLogger(outputClazz);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger#put(jp.co.nssol
	 * .hi5.tool.jslint.messages.MessagesBase.Message, java.lang.Throwable,
	 * java.lang.Object[])
	 */
	@Override
	public void put(Message message, Throwable e, Object... params) {

		if (isEnableFor(message)) {
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
	 * @see jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger#put(jp.co.nssol
	 * .hi5.tool.jslint.messages.MessagesBase.Message, java.lang.Object[])
	 */
	@Override
	public void put(Message message, Object... params) {

		put(message, null, params);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger#enableFor(jp.co
	 * .nssol.hi5.tool.jslint.messages.MessagesBase.Message)
	 */
	@Override
	public boolean isEnableFor(Message message) {

		return logger.isEnabledFor(convertInnerLoglevel(message.getLevel()));
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger#debug(java.lang
	 * .Object)
	 */
	@Override
	public void debug(Object message) {

		logger.debug(message);

	}

}
