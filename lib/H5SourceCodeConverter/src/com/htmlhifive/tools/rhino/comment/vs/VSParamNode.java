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

public class VSParamNode extends AbstractVSDocNode implements VSDocNode {

	/** paramType */
	private String paramType;
	/** paramName */
	private String paramName;
	/** pramDescription */
	private String paramDescription;

	public VSParamNode() {

		super(VSTag.PARAM);
	}

	/**
	 * paramTypeを取得する.
	 *
	 * @return paramType
	 */
	public String getParamType() {

		return paramType;
	}

	/**
	 * paramTypeを設定する.
	 *
	 * @param paramType paramType
	 */
	public void setParamType(String paramType) {

		this.paramType = paramType;
	}

	/**
	 * paramNameを取得する.
	 *
	 * @return paramName
	 */
	public String getParamName() {

		return paramName;
	}

	/**
	 * paramNameを設定する.
	 *
	 * @param paramName paramName
	 */
	public void setParamName(String paramName) {

		this.paramName = paramName;
	}

	/**
	 * pramDescriptionを取得する.
	 *
	 * @return pramDescription
	 */
	public String getParamDescription() {

		return paramDescription;
	}

	/**
	 * pramDescriptionを設定する.
	 *
	 * @param pramDescription pramDescription
	 */
	public void setParamDescription(String pramDescription) {

		this.paramDescription = pramDescription;
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
		result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
		result = prime * result + ((paramType == null) ? 0 : paramType.hashCode());
		result = prime * result + ((paramDescription == null) ? 0 : paramDescription.hashCode());
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
		if (!(obj instanceof VSParamNode)) {
			return false;
		}
		VSParamNode other = (VSParamNode) obj;
		if (paramName == null) {
			if (other.paramName != null) {
				return false;
			}
		} else if (!paramName.equals(other.paramName)) {
			return false;
		}
		if (paramType == null) {
			if (other.paramType != null) {
				return false;
			}
		} else if (!paramType.equals(other.paramType)) {
			return false;
		}
		if (paramDescription == null) {
			if (other.paramDescription != null) {
				return false;
			}
		} else if (!paramDescription.equals(other.paramDescription)) {
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
		builder.append("ParamNode [paramType=");
		builder.append(paramType);
		builder.append(", paramName=");
		builder.append(paramName);
		builder.append(", pramDescription=");
		builder.append(paramDescription);
		builder.append("]");
		return builder.toString();
	}

}
