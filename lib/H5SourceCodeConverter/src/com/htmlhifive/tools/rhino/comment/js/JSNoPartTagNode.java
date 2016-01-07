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
 * パートを持たないタグ.<br>
 * "@tagname"
 */
public class JSNoPartTagNode implements JSTagNode {

	private final JSTag tag;

	public JSNoPartTagNode(JSTag tag) {

		this.tag = tag;
	}

	@Override
	public JSTag getTag() {

		return tag;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("NoPartTagNode [getTag()=");
		builder.append(getTag());
		builder.append("]");
		return builder.toString();
	}

}
