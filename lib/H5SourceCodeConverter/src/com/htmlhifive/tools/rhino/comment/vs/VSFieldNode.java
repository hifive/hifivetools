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

package com.htmlhifive.tools.rhino.comment.vs;

public class VSFieldNode extends AbstractVSDocNode implements VSDocNode {

	/** fieldType */
	private String fieldType;

	/** fieldName */
	private String fieldName;

	/** description */
	private String description;

	public VSFieldNode() {

		super(VSTag.FIELD);
	}

	/**
	 * fieldTypeを取得する.
	 *
	 * @return fieldType
	 */
	public String getFieldType() {

		return fieldType;
	}

	/**
	 * fieldTypeを設定する.
	 *
	 * @param fieldType fieldType
	 */
	public void setFieldType(String fieldType) {

		this.fieldType = fieldType;
	}

	/**
	 * fieldNameを取得する.
	 *
	 * @return fieldName
	 */
	public String getFieldName() {

		return fieldName;
	}

	/**
	 * fieldNameを設定する.
	 *
	 * @param fieldName fieldName
	 */
	public void setFieldName(String fieldName) {

		this.fieldName = fieldName;
	}

	/**
	 * descriptionを取得する.
	 *
	 * @return description
	 */
	public String getDescription() {

		return description;
	}

	/**
	 * descriptionを設定する.
	 *
	 * @param description description
	 */
	public void setDescription(String description) {

		this.description = description;
	}

}
