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

package com.htmlhifive.tools.rhino.comment.vs;

public enum VSTag {
	/** FIELD */
	FIELD("field"),
	/** SUMMARY */
	SUMMARY("summary"),
	/** LOC */
	LOC("loc"),
	/** PARAM */
	PARAM("param"),
	/** RETURNS */
	RETURNS("returns"),
	/** SIGNATURE */
	SIGNATURE("signature"),
	/** VALUE */
	VALUE("value"),
	/** VAR */
	VAR("var");

	private String tagName;

	private VSTag(String tagName) {

		this.tagName = tagName;
	}

	public String getTagName() {

		return tagName;
	}
}
