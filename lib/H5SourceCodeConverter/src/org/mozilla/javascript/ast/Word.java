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

package org.mozilla.javascript.ast;

import com.htmlhifive.tools.rhino.Constants;
import com.htmlhifive.tools.rhino.Util;


/**
 * 文字列を保持するノード.
  */
public class Word extends AstNode {

	/** JavaScript構文で使用される文字列. */
	private String value;

	/**
	 * コンストラクタ.
	 *
	 * @param pos
	 * @param value
	 */
	public Word(int position, String value) {
		super(position, value.length());
		this.value = value;
	}

	@Override
	public String toSource(int depth) {
		return value;
	}

	@Override
	public void visit(NodeVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 *
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 *
	 */
	public String toString() {
		Object depthObj = getProp(Constants.DEPTH);
		StringBuilder sb = new StringBuilder();
		if (null != depthObj) {
			int depth = ((Integer)depthObj).intValue();
	        for (int i = 0; i < depth; i++) {
	            sb.append(Util.makeIndent(1));
	        }
		}
		sb.append(value);
		return sb.toString();
	}
}
