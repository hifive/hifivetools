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

import static com.htmlhifive.tools.rhino.comment.TestUtil.createFunctionJsDocRoot;
import static com.htmlhifive.tools.rhino.comment.TestUtil.expectAllTagDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;


import org.junit.Test;

import com.htmlhifive.tools.rhino.comment.TagType;
import com.htmlhifive.tools.rhino.comment.js.JSDocRoot;
import com.htmlhifive.tools.rhino.comment.js.JSNoPartTagNode;
import com.htmlhifive.tools.rhino.comment.js.JSSinglePartTagNode;
import com.htmlhifive.tools.rhino.comment.js.JSTag;
import com.htmlhifive.tools.rhino.comment.js.JSTagNode;
import com.htmlhifive.tools.rhino.comment.js.JSTypeNamePartNode;
import com.htmlhifive.tools.rhino.comment.js.JSTypePartNode;

public class CommentNodeParserTest {

	@Test
	public void testParse() {

		JSDocRoot root = createFunctionJsDocRoot();
		JSTagNode[] tagNodes = root.getTagNodes();
		assertEquals(33, tagNodes.length);
		existTagCheck(root);
		assertEquals(expectAllTagDescription(), root.getDescription());
	}

	@Test
	public void testNoPartTag() {

		JSDocRoot root = createFunctionJsDocRoot();
		noPartTagCheck(root);

	}

	@Test
	public void testSinglePartTag() {

		JSDocRoot root = createFunctionJsDocRoot();
		singlePartTagCheck(root);

	}

	@Test
	public void testTypePartTag() {

		JSDocRoot root = createFunctionJsDocRoot();
		typePartTagCheck(root);

	}

	@Test
	public void testTypeNamePartTag() {

		JSDocRoot root = createFunctionJsDocRoot();
		typeNamePartNodeCheck(root);

	}

	private void typeNamePartNodeCheck(JSDocRoot root) {

		JSTagNode[] typeNamePartTags = selectTagNodes(root, new JSTag[] { JSTag.PARAM, JSTag.PROPERTY });
		for (JSTagNode tagNode : typeNamePartTags) {
			assertTrue(tagNode.toString(), tagNode instanceof JSTypeNamePartNode);
			JSTypeNamePartNode typeTagNode = (JSTypeNamePartNode) tagNode;
			assertEquals(tagNode.getTag().getTagname() + "の説明です", typeTagNode.getValue());
			assertEquals(tagNode.getTag().getTagname() + "Type", typeTagNode.getType());
			assertEquals(tagNode.getTag().getTagname() + "Name", typeTagNode.getName());
		}

	}

	private void typePartTagCheck(JSDocRoot root) {

		JSTagNode[] typePartTags = selectTagNodes(root, new JSTag[] { JSTag.RETURNS, JSTag.THROWS });
		for (JSTagNode tagNode : typePartTags) {
			assertTrue(tagNode.toString(), tagNode instanceof JSTypePartNode);
			JSTypePartNode typeTagNode = (JSTypePartNode) tagNode;
			assertEquals(tagNode.getTag().getTagname() + "の説明です", typeTagNode.getValue());
			assertEquals(tagNode.getTag().getTagname() + "Type", typeTagNode.getType());
		}

	}

	private void singlePartTagCheck(JSDocRoot root) {

		JSTagNode[] singlePartTags = selectTagNodes(root, TagType.SINGLE_PART_TAG.getJsTag());
		for (JSTagNode tagNode : singlePartTags) {
			assertTrue(tagNode.toString(), tagNode instanceof JSSinglePartTagNode);
			JSSinglePartTagNode singleTagNode = (JSSinglePartTagNode) tagNode;
			String tagname = tagNode.getTag().getTagname();
			assertEquals(tagname + "の説明です", singleTagNode.getValue());
		}

	}

	private void noPartTagCheck(JSDocRoot root) {

		JSTagNode[] noPartTagNodeList = selectTagNodes(root, TagType.NO_PART_TAG.getJsTag());
		for (JSTagNode tagNode : noPartTagNodeList) {
			assertEquals(true, tagNode instanceof JSNoPartTagNode);
		}
	}

	private JSTagNode[] selectTagNodes(JSDocRoot root, JSTag[] tagTypes) {

		List<JSTagNode> tagNodeList = new ArrayList<JSTagNode>();
		for (JSTag nopartTag : tagTypes) {
			JSTagNode[] nodes = root.getTagNode(nopartTag);
			if (nodes != null && nodes.length != 0) {
				for (JSTagNode node : nodes) {
					tagNodeList.add(node);
				}
			}
		}
		return (JSTagNode[]) tagNodeList.toArray(new JSTagNode[tagNodeList.size()]);
	}

	private void existTagCheck(JSDocRoot root) {

		assertEquals(true, root.existTag(JSTag.AUTHOR));
		assertEquals(true, root.existTag(JSTag.BORROWS));
		assertEquals(true, root.existTag(JSTag.CLASS));
		assertEquals(true, root.existTag(JSTag.CONSTANT));
		assertEquals(true, root.existTag(JSTag.CONSTRUCTOR));
		assertEquals(true, root.existTag(JSTag.CONSTRUCTS));
		assertEquals(true, root.existTag(JSTag.DEFAULT));
		assertEquals(true, root.existTag(JSTag.DEPRECATED));
		assertEquals(true, root.existTag(JSTag.DESCRIPTION));
		assertEquals(true, root.existTag(JSTag.EVENT));
		assertEquals(true, root.existTag(JSTag.EXAMPLE));
		assertEquals(true, root.existTag(JSTag.EXPORTS));
		assertEquals(true, root.existTag(JSTag.FIELD));
		assertEquals(true, root.existTag(JSTag.FILEOVERVIEW));
		assertEquals(true, root.existTag(JSTag.FUNCTION));
		assertEquals(true, root.existTag(JSTag.IGNORE));
		assertEquals(true, root.existTag(JSTag.INNER));
		assertEquals(true, root.existTag(JSTag.LENDS));
		assertEquals(true, root.existTag(JSTag.MEMBEROF));
		assertEquals(true, root.existTag(JSTag.NAME));
		assertEquals(true, root.existTag(JSTag.NAMESPACE));
		assertEquals(true, root.existTag(JSTag.PARAM));
		assertEquals(true, root.existTag(JSTag.PRIVATE));
		assertEquals(true, root.existTag(JSTag.PROPERTY));
		assertEquals(true, root.existTag(JSTag.PUBLIC));
		assertEquals(true, root.existTag(JSTag.REQUIRES));
		assertEquals(true, root.existTag(JSTag.RETURNS));
		assertEquals(true, root.existTag(JSTag.SEE));
		assertEquals(true, root.existTag(JSTag.SINCE));
		assertEquals(true, root.existTag(JSTag.STATIC));
		assertEquals(true, root.existTag(JSTag.THROWS));
		assertEquals(true, root.existTag(JSTag.TYPE));
		assertEquals(true, root.existTag(JSTag.VERSION));
	}

}
