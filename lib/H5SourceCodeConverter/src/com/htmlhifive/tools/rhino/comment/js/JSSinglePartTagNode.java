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

package com.htmlhifive.tools.rhino.comment.js;


/**
 * 一つのパートを持つタグ.<br>
 * "@tagname part"
 */
public class JSSinglePartTagNode extends JSNoPartTagNode implements JSTagNode {

	/** value */
	private String value;

	public JSSinglePartTagNode(JSTag tag) {

		super(tag);
	}

	/**
	 * valueを取得する.
	 *
	 * @return value
	 */
	public String getValue() {

		return value;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("SinglePartTagNode [getValue()=");
		builder.append(getValue());
		builder.append(", getTag()=");
		builder.append(getTag());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * valueを設定する.
	 *
	 * @param value value
	 */
	public void setValue(String value) {

		this.value = value;
	}

}
