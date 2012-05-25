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
 package jp.co.nssol.h5.tools.codeassist.ui.view.bean;

import java.util.EventListener;

/**
 * 
 * コンポジットに紐づくビーンが変更されたことを通知するリスナインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface BeanChangedListner extends EventListener {

	/**
	 * ビーンの変更通知メソッド.
	 * 
	 * @param event ビーン変更イベント.
	 */
	void beanChanged(BeanChangeEvent event);

}
