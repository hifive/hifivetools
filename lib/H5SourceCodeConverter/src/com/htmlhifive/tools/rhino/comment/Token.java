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

public class Token {

	/**
	 * トークンのタイプ.
	 */
	private TokenType type;
	/**
	 * トークンの値.
	 */
	private String value;

	/**
	 * 行数.
	 */
	private int lineNum;

	/**
	 * 文字位置.
	 */
	private int indexNum;

	public Token(TokenType type, String value) {

		this.type = type;
		this.value = value;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("[tokenType] : ");
		builder.append(type.name());
		builder.append("\n");
		builder.append("[tokenValue] : ");
		builder.append(value);
		builder.append("\n");
		builder.append("[lineNumber] : ");
		builder.append(lineNum);
		builder.append("\n");
		builder.append("[indexNumber] : ");
		builder.append(indexNum);
		return builder.toString();
	}

	/**
	 * トークンのタイプ.を取得する.
	 *
	 * @return トークンのタイプ.
	 */
	public TokenType getType() {

		return type;
	}

	/**
	 * トークンのタイプ.を設定する.
	 *
	 * @param type トークンのタイプ.
	 */
	public void setType(TokenType type) {

		this.type = type;
	}

	/**
	 * トークンの値.を取得する.
	 *
	 * @return トークンの値.
	 */
	public String getValue() {

		return value;
	}

	/**
	 * トークンの値.を設定する.
	 *
	 * @param value トークンの値.
	 */
	public void setValue(String value) {

		this.value = value;
	}

	/**
	 * 行数.を取得する.
	 *
	 * @return 行数.
	 */
	public int getLineNum() {

		return lineNum;
	}

	/**
	 * 行数.を設定する.
	 *
	 * @param lineNum 行数.
	 */
	public void setLineNum(int lineNum) {

		this.lineNum = lineNum;
	}

	/**
	 * 文字位置.を取得する.
	 *
	 * @return 文字位置.
	 */
	public int getIndexNum() {

		return indexNum;
	}

	/**
	 * 文字位置.を設定する.
	 *
	 * @param indexNum 文字位置.
	 */
	public void setIndexNum(int indexNum) {

		this.indexNum = indexNum;
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
		result = prime * result + indexNum;
		result = prime * result + lineNum;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (!(obj instanceof Token)) {
			return false;
		}
		Token other = (Token) obj;
		if (indexNum != other.indexNum) {
			return false;
		}
		if (lineNum != other.lineNum) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
