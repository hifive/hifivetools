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
package com.htmlhifive.tools.jslint.view.model;

import java.util.Set;

/**
 * 
 * チェック対象ファイルとライブラリを確認するコンポジット.
 * 
 * @author NS Solutions Corporation
 */
public class TargetStructureCompositeViewModel extends AbstractModelObject {

	/**
	 * ライブラリビューでチェックされているエレメント.
	 */
	private Set<String> checkedInternalLibElement;

	/**
	 * 外部参照ライブラリビューでチェックされているエレメント.
	 */
	private Set<String> checkedExternalLibElement;

	/**
	 * ライブラリビューでチェックされているエレメントを取得する.
	 * 
	 * @return ライブラリビューでチェックされているエレメント
	 */
	public Set<String> getCheckedInternalLibElement() {
		return checkedInternalLibElement;
	}

	/**
	 * ライブラリビューでチェックされているエレメントを設定する.
	 * 
	 * @param checkedInternalLibElement ライブラリビューでチェックされているエレメント
	 */
	public void setCheckedInternalLibElement(Set<String> checkedInternalLibElement) {
		this.checkedInternalLibElement = checkedInternalLibElement;
		firePropertyChange("checkedInternalLibElement", null, this.checkedInternalLibElement);
	}

	/**
	 * 外部参照ライブラリビューでチェックされているエレメントを取得する.
	 * 
	 * @return 外部参照ライブラリビューでチェックされているエレメント
	 */
	public Set<String> getCheckedExternalLibElement() {
		return checkedExternalLibElement;
	}

	/**
	 * 外部参照ライブラリビューでチェックされているエレメントを設定する.
	 * 
	 * @param checkedExternalLibElement 外部参照ライブラリビューでチェックされているエレメント
	 */
	public void setCheckedExternalLibElement(Set<String> checkedExternalLibElement) {
		this.checkedExternalLibElement = checkedExternalLibElement;
		firePropertyChange("checkedExternalLibElement", null, this.checkedExternalLibElement);
	}

}
