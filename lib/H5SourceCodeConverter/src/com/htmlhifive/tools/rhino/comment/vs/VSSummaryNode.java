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

public class VSSummaryNode extends AbstractVSDocNode implements VSDocNode {

	/** summary */
	private String summary;

	public VSSummaryNode() {

		super(VSTag.SUMMARY);
	}

	/**
	 * summaryを取得する.
	 *
	 * @return summary
	 */
	public String getSummary() {

		return summary;
	}

	/**
	 * summaryを設定する.
	 *
	 * @param summary summary
	 */
	public void setSummary(String summary) {

		this.summary = summary;
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
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
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
		if (!(obj instanceof VSSummaryNode)) {
			return false;
		}
		VSSummaryNode other = (VSSummaryNode) obj;
		if (summary == null) {
			if (other.summary != null) {
				return false;
			}
		} else if (!summary.equals(other.summary)) {
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
		builder.append("SummaryNode [summary=");
		builder.append(summary);
		builder.append("]");
		return builder.toString();
	}

}
