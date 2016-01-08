/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
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
package com.htmlhifive.tools.jslint.logger;

import com.htmlhifive.tools.jslint.messages.MessagesBase.Message;

/**
 * JSLintロガーインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface JSLintPluginLogger {
	/**
	 * ログを出力する.
	 * 
	 * @param message ログメッセージ
	 * @param e 例外.
	 * @param params ログパラメータ.
	 */
	void put(Message message, Throwable e, Object... params);

	/**
	 * ログを出力する.
	 * 
	 * @param message ログメッセージ
	 * @param params ログパラメータ.
	 */
	void put(Message message, Object... params);

	/**
	 * ログを出力する.
	 * 
	 * @param message 出力メッセージ.
	 */
	void debug(Object message);

	/**
	 * 指定されたメッセージが現在の設定で表示されるかどうかを取得する.
	 * 
	 * @param message メッセージ
	 * @return メッセージが表示されればtrue、されなければfalse
	 */
	boolean isEnableFor(Message message);

}
