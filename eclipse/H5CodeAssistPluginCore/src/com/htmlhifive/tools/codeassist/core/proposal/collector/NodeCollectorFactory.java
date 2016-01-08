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
package com.htmlhifive.tools.codeassist.core.proposal.collector;

import com.htmlhifive.tools.codeassist.core.config.bean.ControllerBean;
import com.htmlhifive.tools.codeassist.core.config.bean.EventContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.InitializationContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.LogicBean;
import com.htmlhifive.tools.codeassist.core.config.bean.RootChildrenElem;

/**
 * ノードコレクターのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class NodeCollectorFactory {

	/**
	 * コンストラクタ.
	 */
	private NodeCollectorFactory() {

		// nocreate
	}

	/**
	 * ノードコレクターを生成する.<br>
	 * 適切なコレクターがない場合はnullを返却.
	 * 
	 * @param elem 設定ファイルのルート直下要素.
	 * @return ノードコレクター.
	 */
	public static NodeCollector createNodeCollector(RootChildrenElem elem) {

		if (elem instanceof ControllerBean) {
			return new ControllerObjectCollector(((ControllerBean) elem).getRegExPattern());
		} else if (elem instanceof LogicBean) {
			LogicBean logicBean = (LogicBean) elem;
			return new LogicObjectCollector(logicBean.getRegExPattern(), logicBean.getRegExControllerPattern());
		} else if (elem instanceof EventContextBean) {
			return new EventHandlerCollector(((EventContextBean) elem).getRegExPattern());
		} else if (elem instanceof InitializationContextBean) {
			return new InitializationHandlerCollector(((InitializationContextBean) elem).getRegExPattern());
		}
		return null;
	}
}
