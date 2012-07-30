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
package com.htmlhifive.tools.jslint.logger;

/**
 * JSLintプラグインのログレベル.
 * 
 * @author NS Solutions Corporation
 * 
 */
public enum LogLevel {
	/**
	 * FATAL.
	 */
	FATAL(500),
	/**
	 * ERROR.
	 */
	ERROR(400),
	/**
	 * WARN.
	 */
	WARN(300),
	/**
	 * INFO.
	 */
	INFO(200),
	/**
	 * DEBUG.
	 */
	DEBUG(100);

	/**
	 * 重大度.
	 */
	private final int priority;

	/**
	 * コンストラクタ.
	 * 
	 * @param priority 重大度.
	 */
	private LogLevel(int priority) {

		this.priority = priority;
	}

	/**
	 * 重大度を取得する.
	 * 
	 * @return 重大度.
	 */
	public int getPriority() {

		return priority;
	}

	/**
	 * 引数のログレベルとどちらが重大か（重大度が大きいか）比較する.
	 * 
	 * @param level 比較レベル.
	 * @return 自身の重大度の方が高い場合はtrue,そうでない場合はfalse;
	 */
	public boolean isPriorThan(LogLevel level) {

		return priority >= level.getPriority();
	}

}
