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

public class VSReturnNode extends AbstractVSDocNode implements VSDocNode {

	/** returntype */
	private String returnType;

	/** description */
	private String description;

	public VSReturnNode() {

		super(VSTag.RETURNS);
	}

	/**
	 * returntypeを取得する.
	 *
	 * @return returntype
	 */
	public String getReturnType() {

		return returnType;
	}

	/**
	 * returntypeを設定する.
	 *
	 * @param returnType returntype
	 */
	public void setReturnType(String returnType) {

		this.returnType = returnType;
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

	/*
	 * (非 Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		return result;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VSReturnNode)) {
			return false;
		}
		VSReturnNode other = (VSReturnNode) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		} else if (!returnType.equals(other.returnType)) {
			return false;
		}
		return true;
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("ReturnNode [returnType=");
		builder.append(returnType);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}

}
