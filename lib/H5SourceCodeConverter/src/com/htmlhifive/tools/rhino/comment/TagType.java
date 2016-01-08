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

package com.htmlhifive.tools.rhino.comment;

import com.htmlhifive.tools.rhino.comment.js.JSTag;

public enum TagType {
	/**
	 *
	 */
	NO_PART_TAG(new JSTag[] { JSTag.CONSTANT, JSTag.CONSTRUCTOR, JSTag.CONSTRUCTS, JSTag.EVENT, JSTag.FIELD,
			JSTag.FUNCTION, JSTag.IGNORE, JSTag.INNER, JSTag.PRIVATE, JSTag.PUBLIC, JSTag.STATIC }),
	/**
	 *
	 */
	SINGLE_PART_TAG(new JSTag[] { JSTag.AUGMENTS, JSTag.AUTHOR, JSTag.CLASS, JSTag.DEFAULT, JSTag.DEPRECATED,
			JSTag.NAME, JSTag.NAMESPACE, JSTag.DESCRIPTION, JSTag.EXAMPLE, JSTag.FILEOVERVIEW, JSTag.LENDS,
			JSTag.REQUIRES, JSTag.MEMBEROF, JSTag.SEE, JSTag.SINCE, JSTag.TYPE, JSTag.VERSION }),
	/**
	 *
	 */
	OTHER_PARAM_TAG(new JSTag[] { JSTag.BORROWS, JSTag.EXPORTS, JSTag.LINK, JSTag.PARAM, JSTag.PROPERTY, JSTag.RETURNS,
			JSTag.THROWS });

	private final JSTag[] jsTag;

	private TagType(JSTag[] jstags) {

		this.jsTag = jstags;
	}

	public JSTag[] getJsTag() {

		return jsTag.clone();
	}
}
