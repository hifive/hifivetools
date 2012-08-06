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
package com.htmlhifive.tools.codeassist.ui.logger;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePlugin;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.LogLevel;
import com.htmlhifive.tools.codeassist.core.messages.MessagesBase.Message;

/**
 * ロガー実装.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class H5CodeAssistPluginLoggerImpl implements H5CodeAssistPluginLogger {

	/**
	 * 表示するログレベル.
	 */
	private LogLevel enableLevel = LogLevel.INFO;

	/**
	 * ロガー実体.
	 */
	private ILog log;

	/**
	 * コンストラクタ.
	 * 
	 * @param className クラス名
	 */
	public H5CodeAssistPluginLoggerImpl(String className) {

		log = H5CodeAssistCorePlugin.getDefault().getLog();
	}

	@Override
	public void log(Message message, Object... params) {

		log(message, null, params);
	}

	/**
	 * eclipseのロガーレベルに変換する.
	 * 
	 * @param level メッセージのレベル.
	 * @return eclipseのロガーレベル.
	 */
	private int convertToInternalLevel(LogLevel level) {

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
		return 0;
	}

	@Override
	public void log(Message message, Throwable t, Object... params) {

		if (isEnabledFor(message.getLevel())) {
			IStatus status = new Status(convertToInternalLevel(message.getLevel()), H5CodeAssistCorePlugin.PLUGIN_ID,
					message.format(params), t);
			log.log(status);
		}

	}

	@Override
	public boolean isEnabledFor(LogLevel level) {

		return level.isPriorThan(enableLevel);
	}

	@Override
	public void setLevel(LogLevel level) {

		enableLevel = level;

	}

	@Override
	public LogLevel getLevel() {

		return enableLevel;
	}

	@Override
	public void debug(Object obj) {

		if (isEnabledFor(LogLevel.DEBUG)) {
			IStatus status = new Status(IStatus.INFO, H5CodeAssistCorePlugin.PLUGIN_ID, obj.toString());
			log.log(status);
		}

	}

}
