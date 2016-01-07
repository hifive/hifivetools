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

package com.htmlhifive.tools.rhino.comment.js;

import java.util.ArrayList;
import java.util.List;

import com.htmlhifive.tools.rhino.comment.RelationNodeType;


public class JSDocRoot extends JSNoPartTagNode implements JSTagNode {

	/** jsDocType */
	private final RelationNodeType jsDocType;

	/** 説明 */
	private String description;

	/** タグノードのリスト */
	private List<JSTagNode> tagNodeList = new ArrayList<JSTagNode>();

	public JSDocRoot(RelationNodeType jsDocType) {

		super(JSTag.ROOT);
		this.jsDocType = jsDocType;
	}

	/**
	 * タグノードを追加する.
	 *
	 * @param tagNodeList タグノードのリスト
	 * @return 追加したかどうか.
	 */
	public boolean addTagNode(JSTagNode tagNode) {

		return this.tagNodeList.add(tagNode);
	}

	/**
	 * 指定したタグがJSDocコメントに存在するかどうか.
	 *
	 * @param tagType 存在チェックするタグ
	 */
	public boolean existTag(JSTag tagType) {

		return getTagNode(tagType) != null;
	}

	/**
	 * jsDocTypeを取得する.
	 *
	 * @return jsDocType
	 */
	public RelationNodeType getCommentType() {

		return jsDocType;
	}

	/**
	 * 説明を取得する.
	 *
	 * @return 説明
	 */
	public String getDescription() {

		return description;
	}

	/**
	 *
	 * 指定したタグノードを取得する.
	 *
	 * @param tagType 取得するタグタイプ.
	 * @return 指定したタグタイプのノード.
	 */
	public JSTagNode[] getTagNode(JSTag tagType) {

		List<JSTagNode> list = new ArrayList<JSTagNode>();
		for (JSTagNode tagNode : tagNodeList) {
			if (tagNode == null) {
				continue;
			}
			if (tagNode.getTag() == tagType) {
				list.add(tagNode);
			}
		}
		return (JSTagNode[]) list.toArray(new JSTagNode[list.size()]);
	}

	/**
	 * タグノードを取得する.
	 *
	 * @return タグノードのリスト
	 */
	public JSTagNode[] getTagNodes() {

		return (JSTagNode[]) tagNodeList.toArray(new JSNoPartTagNode[tagNodeList.size()]);
	}

	/**
	 *
	 * タグノードを削除する.
	 *
	 * @param tagNode 削除するタグ
	 */
	public boolean removeTagNode(JSTagNode tagNode) {

		return this.tagNodeList.remove(tagNode);
	}

	/**
	 * 説明を設定する.
	 *
	 * @param description 説明
	 */
	public void setDescription(String description) {

		this.description = description;
	}
}
