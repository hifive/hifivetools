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
package com.htmlhifive.tools.jslint.parse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * 
 * Jsファイルにマーキングするサポートクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JsMarkingSupport {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JsMarkingSupport.class);

	/**
	 * マーカー.
	 */
	private final IMarker marker;

	/**
	 * 属性マップ.
	 */
	private final Map<String, Object> attributeMap;

	/**
	 * コンストラクタ.
	 * 
	 * @param marker マーカー.
	 */
	public JsMarkingSupport(IMarker marker) {
		attributeMap = new HashMap<String, Object>();
		this.marker = marker;
	}

	/**
	 * 重大度を設定する.
	 * 
	 * @see {@link IMarker}
	 * @param severity 重大度.
	 */
	public void putSeverityAttribute(int severity) {

		attributeMap.put(IMarker.SEVERITY, severity);
	}

	/**
	 * ロケーションを設定する.
	 * 
	 * 
	 * @see {@link IMarker}
	 * @param location ロケーション.
	 */
	public void putLocationAttribute(String location) {

		attributeMap.put(IMarker.LOCATION, location);
	}

	/**
	 * 行数を設定する.
	 * 
	 * @See {@link IMarker}
	 * @param lineNum 行数.
	 */
	public void putLineNumAttribute(int lineNum) {

		attributeMap.put(IMarker.LINE_NUMBER, lineNum);
	}

	/**
	 * メッセージを設定する.
	 * 
	 * @See {@link IMarker}
	 * @param message メッセージ.
	 */
	public void putMessageAttribute(String message) {

		attributeMap.put(IMarker.MESSAGE, message);
	}

	/**
	 * 設定された内容でマークする.
	 * 
	 * @return 成功したかどうか.
	 */
	public boolean marking() {
		try {
			marker.setAttributes(attributeMap);
			attributeMap.clear();
			return true;
		} catch (CoreException e) {
			// TODO エラーメッセージ.
			logger.put(Messages.EM0001, e);
			return false;
		}
	}

}
