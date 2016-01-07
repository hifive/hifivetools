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

package com.htmlhifive.tools.rhino.comment;

import com.htmlhifive.tools.rhino.Constants;
import com.htmlhifive.tools.rhino.comment.RelationNodeType;
import com.htmlhifive.tools.rhino.comment.js.JSDocCommentNodeParser;
import com.htmlhifive.tools.rhino.comment.js.JSDocRoot;
import com.htmlhifive.tools.rhino.comment.vs.VSDocRoot;
import com.htmlhifive.tools.rhino.comment.vs.VSFieldNode;
import com.htmlhifive.tools.rhino.comment.vs.VSParamNode;
import com.htmlhifive.tools.rhino.comment.vs.VSReturnNode;
import com.htmlhifive.tools.rhino.comment.vs.VSSummaryNode;
import com.htmlhifive.tools.rhino.comment.vs.VSVarNode;

/**
 *
 */
public final class TestUtil {

	public static String createAllTagComment() {

		StringBuilder sb = new StringBuilder();
		addLine(sb, "/**");
		addLine(sb, " * これは一行目の関数の説明です。<br>");
		addLine(sb, " * これは二行目の関数の説明です。<br>");
		addLine(sb, " * <pre>");
		addLine(sb, " * var test = new function(){}<br>");
		addLine(sb, " * </pre>");
		addLine(sb, " * @author authorの説明です");
		addLine(sb, " * @borrows other");
		addLine(sb, " * @class classの説明です");
		addLine(sb, " * @constant");
		addLine(sb, " * @constructor");
		addLine(sb, " * @constructs");
		addLine(sb, " * @default defaultの説明です");
		addLine(sb, " * @deprecated deprecatedの説明です");
		addLine(sb, " * @description descriptionの説明です");
		addLine(sb, " * @event eventの説明です");
		addLine(sb, " * @example exampleの説明です");
		addLine(sb, " * @exports other");
		addLine(sb, " * @field");
		addLine(sb, " * @fileOverview fileOverviewの説明です");
		addLine(sb, " * @function");
		addLine(sb, " * @ignore");
		addLine(sb, " * @inner");
		addLine(sb, " * @lends lendsの説明です");
		addLine(sb, " * @memberOf memberOfの説明です");
		addLine(sb, " * @name nameの説明です");
		addLine(sb, " * @namespace namespaceの説明です");
		addLine(sb, " * @param {paramType} paramName paramの説明です");
		addLine(sb, " * @private");
		addLine(sb, " * @property {propertyType} propertyName propertyの説明です");
		addLine(sb, " * @public");
		addLine(sb, " * @requires requiresの説明です");
		addLine(sb, " * @returns {returnsType} returnsの説明です");
		addLine(sb, " * @see seeの説明です");
		addLine(sb, " * @since sinceの説明です");
		addLine(sb, " * @static");
		addLine(sb, " * @throws {throwsType} throwsの説明です");
		addLine(sb, " * @type typeの説明です");
		addLine(sb, " * @version versionの説明です");
		addLine(sb, " *");
		addLine(sb, " */");
		return sb.toString();
	}

	public static void addLine(StringBuilder sb, String str) {

		sb.append(str);
		sb.append(Constants.LINE_SEPARATOR);
	}

	public static JSDocRoot createFunctionJsDocRoot() {

		JSDocCommentNodeParser commentNodeParser = new JSDocCommentNodeParser(RelationNodeType.FUNCTION);
		String comment = createAllTagComment();
		JSDocRoot root = commentNodeParser.parse(comment);
		return root;
	}

	public static JSDocRoot createFieldJsDocRoot() {

		JSDocCommentNodeParser commentNodeParser = new JSDocCommentNodeParser(RelationNodeType.FIELD);
		String comment = createAllTagComment();
		JSDocRoot root = commentNodeParser.parse(comment);
		return root;
	}

	public static JSDocRoot createVarJsDocRoot() {

		JSDocCommentNodeParser commentNodeParser = new JSDocCommentNodeParser(RelationNodeType.VAR);
		String comment = createAllTagComment();
		JSDocRoot root = commentNodeParser.parse(comment);
		return root;
	}

	public static String expectAllTagDescription() {

		StringBuilder sb = new StringBuilder();
		addLine(sb, "これは一行目の関数の説明です。<br>");
		addLine(sb, "これは二行目の関数の説明です。<br>");
		addLine(sb, "<pre>");
		addLine(sb, "var test = new function(){}<br>");
		addLine(sb, "</pre>");
		return sb.toString();
	}

	public static String expectVSDocParam() {

		StringBuilder sb = new StringBuilder();
		addLine(sb, "/// <summary>");
		addLine(sb, "///  これは一行目の関数の説明です。");
		addLine(sb, "///  これは二行目の関数の説明です。");
		addLine(sb, "///  ");
		addLine(sb, "///  var test = new function(){}");
		addLine(sb, "///  ");
		addLine(sb, "/// </summary>");
		addLine(sb, "/// <param  name = \"param0\" type = \"String\" >");
		addLine(sb, "///  0番目の引数です.");
		addLine(sb, "/// </param>");
		addLine(sb, "/// <param  name = \"param1\" type = \"String\" >");
		addLine(sb, "///  1番目の引数です.");
		addLine(sb, "/// </param>");
		addLine(sb, "/// <param  name = \"param2\" type = \"String\" >");
		addLine(sb, "///  2番目の引数です.");
		addLine(sb, "/// </param>");
		addLine(sb, "/// <param  name = \"param3\" type = \"Object\" >");
		addLine(sb, "///  3番目の引数です.");
		addLine(sb, "/// </param>");
		addLine(sb, "/// <param  name = \"param4\" type = \"Object\" >");
		addLine(sb, "///  4番目の引数です.");
		addLine(sb, "/// </param>");
		addLine(sb, "/// <returns  type = \"String\" >");
		addLine(sb, "///  戻り値の説明です.");
		addLine(sb, "/// </returns>");
		return sb.toString();
	}

	private TestUtil() {

	}

	public static VSDocRoot createFunctionVSDocRoot() {

		VSDocRoot root = new VSDocRoot(RelationNodeType.FUNCTION);
		VSSummaryNode summaryNode = new VSSummaryNode();
		summaryNode.setSummary(expectAllTagDescription());
		root.addVSDocNode(summaryNode);
		for (int i = 0; i < 5; i++) {
			VSParamNode node = new VSParamNode();
			node.setParamDescription(i + "番目の引数です.");
			node.setParamName("param" + i);
			if (i < 3) {
				node.setParamType("String");
			} else {
				node.setParamType("Object");
			}
			root.addVSDocNode(node);
		}
		VSReturnNode returnNode = new VSReturnNode();
		returnNode.setDescription("戻り値の説明です.");
		returnNode.setReturnType("String");
		root.addVSDocNode(returnNode);
		return root;
	}

	public static VSDocRoot createFieldVSDocRoot() {

		VSDocRoot root = new VSDocRoot(RelationNodeType.FIELD);
		VSFieldNode node = new VSFieldNode();
		node.setFieldType("FieldType");
		node.setFieldName("fieldName");
		node.setDescription("fieldの説明です.");
		root.addVSDocNode(node);
		return root;
	}

	public static VSDocRoot createVarVSDocRoot() {

		VSDocRoot root = new VSDocRoot(RelationNodeType.VAR);
		VSVarNode node = new VSVarNode();
		node.setVarType("VarType");
		node.setDescription("varの説明です.");
		root.addVSDocNode(node);
		return root;
	}
}
