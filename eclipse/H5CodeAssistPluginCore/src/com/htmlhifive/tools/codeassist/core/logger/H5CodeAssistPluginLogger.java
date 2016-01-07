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

import com.htmlhifive.tools.codeassist.core.messages.MessagesBase.Message;

/**
 * Hi5コードアシストプラグインのロガーインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface H5CodeAssistPluginLogger {

	/**
	 * ログ出力をする.
	 * 
	 * @param message ログメッセージ.
	 * @param params パラメータ.
	 */
	public abstract void log(Message message, Object... params);

	/**
	 * ログ出力をする.
	 * 
	 * @param message ログメッセージ.
	 * @param t 例外
	 * @param params パラメータ.
	 */
	public abstract void log(Message message, Throwable t, Object... params);

	/**
	 * 指定したログレベルが出力対象かどうかを取得する.
	 * 
	 * @param level ログレベル.
	 * @return 指定したログレベルが出力対象かどうか.
	 */
	public abstract boolean isEnabledFor(LogLevel level);

	/**
	 * ログレベルをセットする.
	 * 
	 * @param level ログレベル.
	 */
	public abstract void setLevel(LogLevel level);

	/**
	 * ログレベルを取得する.
	 * 
	 * @return ログレベル.
	 */
	public abstract LogLevel getLevel();

	/**
	 * デバッグ出力をする.
	 * 
	 * @param obj 出力オブジェクト.
	 */
	public abstract void debug(Object obj);

}
