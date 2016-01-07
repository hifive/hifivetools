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
package com.htmlhifive.tools.jslint.event;

import java.util.EventListener;

/**
 * 
 * FilterBeanが変更された時のイベントを受け取るリスナインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface FilterBeanListChangeListener extends EventListener {

	/**
	 * イベント通知メソッド.
	 * 
	 * @param event イベント.
	 */
	void modify(FilterBeanListChangeEvent event);

}
