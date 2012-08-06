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

import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.tools.rhino.comment.RelationNodeType;


public class VSDocRoot {

	private RelationNodeType commentType;

	public VSDocRoot(RelationNodeType commentType) {

		this.commentType = commentType;
	}

	private List<VSDocNode> docNodeList = new ArrayList<VSDocNode>();

	public boolean addVSDocNode(VSDocNode docNode) {

		return docNodeList.add(docNode);
	}

	public boolean removeVSDocNode(VSDocNode docNode) {

		return docNodeList.remove(docNode);
	}

	public VSDocNode[] getAllNode() {

		return (VSDocNode[]) docNodeList.toArray(new VSDocNode[docNodeList.size()]);
	}

	public VSDocNode[] getNodeFrom(VSTag tag) {

		List<VSDocNode> targetList = new ArrayList<VSDocNode>();
		for (VSDocNode vsDocNode : docNodeList) {
			if (vsDocNode != null && vsDocNode.getTagType() == tag) {
				targetList.add(vsDocNode);
			}
		}
		return (VSDocNode[]) targetList.toArray(new VSDocNode[targetList.size()]);
	}

	public boolean existNodeFrom(VSTag tag) {

		return getNodeFrom(tag) != null && getNodeFrom(tag).length > 0;
	}

	/**
	 * commentTypeを取得する.
	 * @return commentType
	 */
	public RelationNodeType getCommentType() {
	    return commentType;
	}

}
