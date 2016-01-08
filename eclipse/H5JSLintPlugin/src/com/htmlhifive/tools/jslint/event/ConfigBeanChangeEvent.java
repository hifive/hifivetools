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
package com.htmlhifive.tools.jslint.event;

import com.htmlhifive.tools.jslint.configure.ConfigBean;

/**
 * 
 * コンフィグビーンが変更された時のイベント.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ConfigBeanChangeEvent {

	/**
	 * 変更されたコンフィグビーン.
	 */
	private final ConfigBean changedBean;

	/**
	 * コンストラクタ.
	 * 
	 * @param changedBean 変更されたコンフィグビーン.
	 */
	public ConfigBeanChangeEvent(ConfigBean changedBean) {

		this.changedBean = changedBean;

	}

	/**
	 * 変更されたコンフィグビーンを取得する.
	 * 
	 * @return 変更されたコンフィグビーン
	 */
	public ConfigBean getChangedBean() {

		return changedBean;
	}
}
