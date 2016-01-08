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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;


import org.apache.commons.lang.StringUtils;

import com.htmlhifive.tools.rhino.Constants;
import com.htmlhifive.tools.rhino.Util;
import com.htmlhifive.tools.rhino.comment.TokenUtil;

public class VSCommentBuilder {

	private VSDocRoot docRoot;

	private VSCommentBuilderHelper helper = new VSCommentBuilderHelper();

	public VSCommentBuilder(VSDocRoot docRoot) {

		this.docRoot = docRoot;
	}

	public String build() {

		StringBuilder builder = new StringBuilder();
		switch (docRoot.getCommentType()) {
			case FUNCTION:
				buildSummary(builder);
				buildParams(builder);
				buildReturns(builder);
				break;
			case FIELD:
				buildField(builder);
				break;
			case VAR:
				buildVar(builder);
				break;
			default:
				break;
		}

		return builder.toString();

	}

	protected void buildField(StringBuilder sb) {

		VSDocNode[] nodes = docRoot.getNodeFrom(VSTag.FIELD);
		for (VSDocNode vsDocNode : nodes) {
			// ///<var type = "varType">description</var>
			VSFieldNode fieldNode = (VSFieldNode) vsDocNode;
			Map<String, String> attributeMap = new HashMap<String, String>();
			attributeMap.put("type", fieldNode.getFieldType());
			attributeMap.put("name", fieldNode.getFieldName());
			helper.appendTag(sb, VSTag.FIELD, attributeMap, fieldNode.getDescription());
		}

	}

	protected void buildParams(StringBuilder sb) {

		VSDocNode[] node = docRoot.getNodeFrom(VSTag.PARAM);
		for (VSDocNode vsDocNode : node) {
			VSParamNode paramNode = (VSParamNode) vsDocNode;
			// ///<param name ="paramname" type = "paramtype">
			Map<String, String> attributeMap = new HashMap<String, String>();
			attributeMap.put("name", paramNode.getParamName());
			attributeMap.put("type", paramNode.getParamType());
			helper.appendTag(sb, VSTag.PARAM, attributeMap, paramNode.getParamDescription());
		}
	}

	protected void buildReturns(StringBuilder sb) {

		VSDocNode[] node = docRoot.getNodeFrom(VSTag.RETURNS);
		for (VSDocNode vsDocNode : node) {
			VSReturnNode returnsNode = (VSReturnNode) vsDocNode;
			// ///<returns type = "paramtype">description<returns>
			Map<String, String> attributeMap = new HashMap<String, String>();
			attributeMap.put("type", returnsNode.getReturnType());
			helper.appendTag(sb, VSTag.RETURNS, attributeMap, returnsNode.getDescription());
		}
	}

	protected void buildSummary(StringBuilder sb) {

		VSDocNode[] node = docRoot.getNodeFrom(VSTag.SUMMARY);
		for (VSDocNode vsDocNode : node) {
			VSSummaryNode summaryNode = (VSSummaryNode) vsDocNode;
			helper.appendTag(sb, VSTag.SUMMARY, null, summaryNode.getSummary());
		}
	}

	protected void buildVar(StringBuilder sb) {

		VSDocNode[] nodes = docRoot.getNodeFrom(VSTag.VAR);
		for (VSDocNode vsDocNode : nodes) {
			// ///<var type = "varType">description</var>
			VSVarNode varNode = (VSVarNode) vsDocNode;
			Map<String, String> attributeMap = new HashMap<String, String>();
			attributeMap.put("type", varNode.getVarType());
			helper.appendTag(sb, VSTag.VAR, attributeMap, varNode.getDescription());
		}
	}

	private class VSCommentBuilderHelper {

		private void appendStartTag(StringBuilder sb, VSTag vsTag, Map<String, String> attributeMap) {

			sb.append("/// <");
			sb.append(vsTag.getTagName());
			if (attributeMap != null) {
				sb.append(Util.makeIndent(1));
				Set<Entry<String, String>> keySet = attributeMap.entrySet();
				for (Entry<String, String> key : keySet) {
					appendAttribute(sb, key.getKey(), key.getValue());
				}
			}
			sb.append(">");
			sb.append(Constants.LINE_SEPARATOR);

		}

		public void appendTag(StringBuilder sb, VSTag vsTag, Map<String, String> attributeMap, String description) {

			helper.appendStartTag(sb, vsTag, attributeMap);
			if (description != null) {
				helper.appendLine(sb, description);
			}
			helper.appendEndTag(sb, vsTag);
			sb.append(Constants.LINE_SEPARATOR);
		}

		private void appendEndTag(StringBuilder sb, VSTag var) {

			sb.append("/// </");
			sb.append(var.getTagName());
			sb.append(">");
		}

		private void appendAttribute(StringBuilder sb, String key, String value) {

			if (StringUtils.isNotEmpty(value)) {
				// key = "value" valueが空またはnullの場合は何も追加しない.
				sb.append(key);
				sb.append(" = \"");
				sb.append(value);
				sb.append("\" ");
			}
		}

		private void appendLine(StringBuilder sb, String value) {

			// 一行ずつ取得.
			StringTokenizer tokenizer = new StringTokenizer(value, Constants.LINE_SEPARATOR);
			while (tokenizer.hasMoreTokens()) {
				sb.append("///");
				sb.append(Util.makeIndent(1));
				sb.append(TokenUtil.escapeHtml(TokenUtil.removeHtmlTag(tokenizer.nextToken())));
				sb.append(Constants.LINE_SEPARATOR);
			}

		}
	}

}
