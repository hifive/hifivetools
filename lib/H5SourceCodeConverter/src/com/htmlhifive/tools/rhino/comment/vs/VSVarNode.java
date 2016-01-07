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

public class VSVarNode extends AbstractVSDocNode implements VSDocNode {

	/** varType */
	private String varType;

	/** description */
	private String description;

	public VSVarNode() {

		super(VSTag.VAR);
	}

	/**
	 * varTypeを取得する.
	 * @return varType
	 */
	public String getVarType() {
	    return varType;
	}

	/**
	 * varTypeを設定する.
	 * @param varType varType
	 */
	public void setVarType(String varType) {
	    this.varType = varType;
	}

	/**
	 * descriptionを取得する.
	 * @return description
	 */
	public String getDescription() {
	    return description;
	}

	/**
	 * descriptionを設定する.
	 * @param description description
	 */
	public void setDescription(String description) {
	    this.description = description;
	}

}
