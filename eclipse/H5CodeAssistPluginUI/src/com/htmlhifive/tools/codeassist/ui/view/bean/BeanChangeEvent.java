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
package com.htmlhifive.tools.codeassist.ui.view.bean;

/**
 * Beanが変更された時のイベント.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class BeanChangeEvent {

	/**
	 * 変更されたビーン.
	 */
	private CompositeBean changedBean;

	/**
	 * コンストラクタ.
	 * 
	 * @param changedBean 変更されたビーン.
	 */
	public BeanChangeEvent(CompositeBean changedBean) {

		this.changedBean = changedBean;
	}

	/**
	 * 変更されたビーンを取得する.
	 * 
	 * @return ビーン
	 */
	public CompositeBean getChangedBean() {

		return changedBean;
	}

	/**
	 * コンポジットの情報ビーンであるということの<br>
	 * マーカーインターフェース.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	public interface CompositeBean {

	}

}
