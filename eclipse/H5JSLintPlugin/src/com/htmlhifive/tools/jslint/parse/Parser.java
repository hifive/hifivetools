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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * オブジェクト解析のインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface Parser {

	/**
	 * 解析を実行する. 解析対象がない（必要がない）場合はnullを返す.
	 * 
	 * @param monitor プログレスモニター
	 * @throws InterruptedException キャンセル押下時.
	 * @throws CoreException 解析例外
	 */
	void parse(IProgressMonitor monitor) throws InterruptedException, CoreException;

	/**
	 * パースをキャンセルする.
	 * 
	 */
	void cansel();
}
