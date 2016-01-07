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

public enum JSTag {
	/** ROOT */
	ROOT("root"),
	/** ANNOTATION */
	PARAM("param"),
	/** AUGMENTS */
	AUGMENTS("augments"),
	/** AUTHOR */
	AUTHOR("author"),
	/** AUGURMENT */
	ARGUMENT("argument"),
	/** BORROWS */
	BORROWS("borrows"),
	/** CLASS */
	CLASS("class"),
	/** CONSTANT */
	CONSTANT("constant"),
	/** CONSTRUCTOR */
	CONSTRUCTOR("constructor"),
	/** CONSTRUCTS */
	CONSTRUCTS("constructs"),
	/** DEFAULT */
	DEFAULT("default"),
	/** DEPRECATED */
	DEPRECATED("deprecated"),
	/** DESCRIPTION */
	DESCRIPTION("description"),
	/** EVENT */
	EVENT("event"),
	/** EXAMPLE */
	EXAMPLE("example"),
	/** EXPORTS */
	EXPORTS("exports"),
	/** EXTENDS */
	EXTENDS("extends"),
	/** FIELD */
	FIELD("field"),
	/** FIELDOF */
	FIELDOF("fieldOf"),
	/** FILEOVERVIES */
	FILEOVERVIEW("fileOverview"),
	/** FUNCTION */
	FUNCTION("function"),
	/** IGNORE */
	IGNORE("ignore"),
	/** INNER */
	INNER("inner"),
	/** LENDS */
	LENDS("lends"),
	/** LINK */
	LINK("link"),
	/** MEMBEROF */
	MEMBEROF("memberOf"),
	/** METHODOF */
	METHODOF("methodOf"),
	/** NAME */
	NAME("name"),
	/** NAMESPACE */
	NAMESPACE("namespace"),
	/** PRIVATE */
	PRIVATE("private"),
	/** PROPERTY */
	PROPERTY("property"),
	/** PUBLID */
	PUBLIC("public"),
	/** REQUIRES */
	REQUIRES("requires"),
	/** RETURNS */
	RETURNS("returns"),
	/** SEE */
	SEE("see"),
	/** SINCE */
	SINCE("since"),
	/** STATIC */
	STATIC("static"),
	/** THROWS */
	THROWS("throws"),
	/** TYPE */
	TYPE("type"),
	/** VERSION */
	VERSION("version");

	private final String tagname;

	private JSTag(String tagName) {

		this.tagname = tagName;
	}

	public String getTagname() {

		return tagname;
	}
}
