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
package com.htmlhifive.tools.codeassist.core.config.bean;

import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.tools.codeassist.core.config.xml.H5CodeAssist;


/**
 * 設定ファイルから読み込んだすべての補完情報を保持するビーンクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class AllBean {

	/**
	 * 最上位の全要素.
	 */
	private List<RootChildrenElem> elemList;

	/**
	 * コンストラクタ.
	 * 
	 * @param assist 設定ファイルから読み込んだルート要素.
	 */
	public AllBean(H5CodeAssist assist) {

		// 挿入順序は優先度を考慮する必要がある.
		elemList = new ArrayList<RootChildrenElem>();
		if (assist.getEventcontext() != null) {
			elemList.add(new EventContextBean(assist.getEventcontext()));
		}
		if (assist.getInitcontext() != null) {
			elemList.add(new InitializationContextBean(assist.getInitcontext()));
		}

		if (assist.getLogic() != null) {
			elemList.add(new LogicBean(assist.getLogic()));
		}
		if (assist.getController() != null) {
			elemList.add(new ControllerBean(assist.getController()));
		}
	}

	/**
	 * 最上位の全要素を取得する.
	 * 
	 * @return 最上位の全要素
	 */
	public RootChildrenElem[] getElemList() {

		return (RootChildrenElem[]) elemList.toArray(new RootChildrenElem[elemList.size()]);
	}

}
