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
package com.htmlhifive.tools.jslint.logger.impl;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.htmlhifive.tools.jslint.JSLintPlugin;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.LogLevel;
import com.htmlhifive.tools.jslint.messages.MessagesBase.Message;

/**
 * JSLintPluginロガー.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSLintPluginLoggerDefaultImpl implements JSLintPluginLogger {

	/**
	 * 表示ログレベル.
	 */
	private static final LogLevel ENABLE = LogLevel.WARN;

	/**
	 * 出力クラス.
	 */
	private Class<?> outputClass;

	/**
	 * コンストラクタ.
	 * 
	 * @param outputClazz 出力クラス.
	 */
	public JSLintPluginLoggerDefaultImpl(Class<?> outputClazz) {

		this.outputClass = outputClazz;
	}

	@Override
	public void put(Message message, Throwable e, Object... params) {

		ILog log = JSLintPlugin.getDefault().getLog();
		if (isEnableFor(message)) {
			log.log(new Status(convertInnerLogLevel(message.getLevel()), JSLintPlugin.PLUGIN_ID, outputClass.getName()
					+ message.format(params), e));
		}

	}

	/**
	 * ログレベルを内部のログレベルに変換する.
	 * 
	 * @param level ログレベル.
	 * @return 変換後のログレベル.
	 */
	private int convertInnerLogLevel(LogLevel level) {

		switch (level) {
		case FATAL:
			return IStatus.ERROR;
		case ERROR:
			return IStatus.ERROR;
		case WARN:
			return IStatus.WARNING;
		case INFO:
			return IStatus.INFO;
		case DEBUG:
			return IStatus.INFO;
		default:
			break;
		}
		return IStatus.INFO;
	}

	@Override
	public void put(Message message, Object... params) {

		this.put(message, null, params);
	}

	@Override
	public void debug(Object message) {

		ILog log = JSLintPlugin.getDefault().getLog();
		if (LogLevel.INFO.isPriorThan(ENABLE)) {
			log.log(new Status(IStatus.INFO, JSLintPlugin.PLUGIN_ID, message.toString(), null));
		}
	}

	@Override
	public boolean isEnableFor(Message message) {

		return message.getLevel().isPriorThan(ENABLE);
	}

}
