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
 *
 * 型をもつタグ.<br>
 * "@tagname {type} part"
 *
 */
public class JSTypePartNode extends JSSinglePartTagNode implements JSOtherTagNode {

	private String type;

	public JSTypePartNode(JSTag tag) {

		super(tag);
	}

	/**
	 * typeを取得する.
	 *
	 * @return type
	 */
	public String getType() {

		return type;
	}

	/**
	 * s typeを設定する.
	 *
	 * @param type type
	 */
	public void setType(String type) {

		this.type = type;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("TypePartNode [getType()=");
		builder.append(getType());
		builder.append(", getValue()=");
		builder.append(getValue());
		builder.append(", getTag()=");
		builder.append(getTag());
		builder.append("]");
		return builder.toString();
	}

}
