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

import com.htmlhifive.tools.rhino.Converter;
import com.htmlhifive.tools.rhino.comment.js.JSDocRoot;
import com.htmlhifive.tools.rhino.comment.js.JSSinglePartTagNode;
import com.htmlhifive.tools.rhino.comment.js.JSTag;
import com.htmlhifive.tools.rhino.comment.js.JSTagNode;
import com.htmlhifive.tools.rhino.comment.js.JSTypeNamePartNode;
import com.htmlhifive.tools.rhino.comment.js.JSTypePartNode;
import com.htmlhifive.tools.rhino.comment.vs.VSDocNode;
import com.htmlhifive.tools.rhino.comment.vs.VSDocRoot;
import com.htmlhifive.tools.rhino.comment.vs.VSFieldNode;
import com.htmlhifive.tools.rhino.comment.vs.VSParamNode;
import com.htmlhifive.tools.rhino.comment.vs.VSReturnNode;
import com.htmlhifive.tools.rhino.comment.vs.VSSummaryNode;
import com.htmlhifive.tools.rhino.comment.vs.VSTag;
import com.htmlhifive.tools.rhino.comment.vs.VSVarNode;

public class JSDocVSDocConverter implements Converter<JSDocRoot, VSDocRoot> {

	@Override
	public VSDocRoot convert(JSDocRoot from) {

		VSDocRoot docRoot = new VSDocRoot(from.getCommentType());
		switch (docRoot.getCommentType()) {
			case FUNCTION:
				addVSRootParams(docRoot, from);
				addVSRootReturns(docRoot, from);
				addVSSummary(docRoot, from);
				break;
			case FIELD:
				addVSFieldTypeNodes(docRoot, from);
				break;
			case VAR:
				addVSVarNode(docRoot, from);
				break;
			default:
				break;
		}
		return docRoot;
	}

	private void addVSVarNode(VSDocRoot docRoot, JSDocRoot from) {

		JSTagNode[] types = from.getTagNode(JSTag.TYPE);
		for (JSTagNode jsTagNode : types) {
			JSSinglePartTagNode node = (JSSinglePartTagNode) jsTagNode;
			VSVarNode fieldNode = new VSVarNode();
			fieldNode.setVarType(node.getValue());
			fieldNode.setDescription(from.getDescription());
			docRoot.addVSDocNode(fieldNode);
		}

	}

	private void addVSFieldTypeNodes(VSDocRoot docRoot, JSDocRoot from) {

		JSTagNode[] types = from.getTagNode(JSTag.TYPE);
		for (JSTagNode jsTagNode : types) {
			JSSinglePartTagNode node = (JSSinglePartTagNode) jsTagNode;
			VSFieldNode fieldNode = new VSFieldNode();
			fieldNode.setFieldType(node.getValue());
			fieldNode.setDescription(from.getDescription());
			docRoot.addVSDocNode(fieldNode);
		}
	}

	private void addVSSummary(VSDocRoot docRoot, JSDocRoot from) {

		String summary = from.getDescription();
		VSSummaryNode node = new VSSummaryNode();
		node.setSummary(summary);
		docRoot.addVSDocNode(node);
	}

	private void addVSRootReturns(VSDocRoot docRoot, JSDocRoot from) {

		JSTagNode[] returns = from.getTagNode(JSTag.RETURNS);
		addVSRootNode(docRoot, returns, VSTag.RETURNS);
	}

	private void addVSRootParams(VSDocRoot docRoot, JSDocRoot from) {

		JSTagNode[] params = from.getTagNode(JSTag.PARAM);
		addVSRootNode(docRoot, params, VSTag.PARAM);
	}

	private void addVSRootNode(VSDocRoot docRoot, JSTagNode[] params, VSTag param) {

		for (JSTagNode jsParamNode : params) {
			VSDocNode node = null;
			switch (param) {
				case PARAM:
					if (!(jsParamNode instanceof JSTypeNamePartNode)) {
						throw new IllegalArgumentException();
					}
					JSTypeNamePartNode jsTypeNamePartNode = (JSTypeNamePartNode) jsParamNode;
					VSParamNode paramNode = new VSParamNode();
					paramNode.setParamName(jsTypeNamePartNode.getName());
					paramNode.setParamType(jsTypeNamePartNode.getType());
					paramNode.setParamDescription(jsTypeNamePartNode.getValue());
					node = paramNode;
					break;
				case RETURNS:
					if (!(jsParamNode instanceof JSTypePartNode)) {
						throw new IllegalArgumentException();
					}
					JSTypePartNode jsTypePartNode = (JSTypePartNode) jsParamNode;
					VSReturnNode returnNode = new VSReturnNode();
					returnNode.setReturnType(jsTypePartNode.getType());
					returnNode.setDescription(jsTypePartNode.getValue());
					node = returnNode;
					break;
				default:
					break;
			}
			docRoot.addVSDocNode(node);

		}
	}

}
