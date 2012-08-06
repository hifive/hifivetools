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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.htmlhifive.tools.rhino.comment.JSDocVSDocConverter;
import com.htmlhifive.tools.rhino.comment.RelationNodeType;
import com.htmlhifive.tools.rhino.comment.vs.VSDocNode;
import com.htmlhifive.tools.rhino.comment.vs.VSDocRoot;
import com.htmlhifive.tools.rhino.comment.vs.VSFieldNode;
import com.htmlhifive.tools.rhino.comment.vs.VSParamNode;
import com.htmlhifive.tools.rhino.comment.vs.VSReturnNode;
import com.htmlhifive.tools.rhino.comment.vs.VSSummaryNode;
import com.htmlhifive.tools.rhino.comment.vs.VSTag;
import com.htmlhifive.tools.rhino.comment.vs.VSVarNode;

public class JSDocVSDocConverterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testConvert() {

		JSDocVSDocConverter converter = new JSDocVSDocConverter();
		VSDocRoot docRoot = converter.convert(TestUtil.createFunctionJsDocRoot());
		VSDocNode[] nodes = docRoot.getAllNode();
		assertEquals(3, nodes.length);
		assertEquals(RelationNodeType.FUNCTION, docRoot.getCommentType());
	}

	@Test
	public void testParam() {

		JSDocVSDocConverter converter = new JSDocVSDocConverter();
		VSDocRoot docRoot = converter.convert(TestUtil.createFunctionJsDocRoot());
		VSDocNode[] nodes = docRoot.getNodeFrom(VSTag.PARAM);
		assertEquals(1, nodes.length);
		VSParamNode node = (VSParamNode) nodes[0];
		assertEquals("paramType", node.getParamType());
		assertEquals("paramName", node.getParamName());
		assertEquals("paramの説明です", node.getParamDescription());
	}

	@Test
	public void testSummary() {

		JSDocVSDocConverter converter = new JSDocVSDocConverter();
		VSDocRoot docRoot = converter.convert(TestUtil.createFunctionJsDocRoot());
		VSDocNode[] nodes = docRoot.getNodeFrom(VSTag.SUMMARY);
		assertEquals(1, nodes.length);
		VSSummaryNode node = (VSSummaryNode) nodes[0];
		assertEquals(TestUtil.expectAllTagDescription(), node.getSummary());
	}

	@Test
	public void testReturn() {

		JSDocVSDocConverter converter = new JSDocVSDocConverter();
		VSDocRoot docRoot = converter.convert(TestUtil.createFunctionJsDocRoot());
		VSDocNode[] nodes = docRoot.getNodeFrom(VSTag.RETURNS);
		assertEquals(1, nodes.length);
		VSReturnNode node = (VSReturnNode) nodes[0];
		assertEquals("returnsType", node.getReturnType());
		assertEquals("returnsの説明です", node.getDescription());
	}

	@Test
	public void testVar() {

		JSDocVSDocConverter converter = new JSDocVSDocConverter();
		VSDocRoot docRoot = converter.convert(TestUtil.createVarJsDocRoot());
		assertEquals(RelationNodeType.VAR, docRoot.getCommentType());
		VSVarNode node = (VSVarNode) docRoot.getNodeFrom(VSTag.VAR)[0];
		assertEquals(TestUtil.expectAllTagDescription(), node.getDescription());
		assertEquals("typeの説明です", node.getVarType());
	}

	@Test
	public void testField() {

		JSDocVSDocConverter converter = new JSDocVSDocConverter();
		VSDocRoot docRoot = converter.convert(TestUtil.createFieldJsDocRoot());
		assertEquals(RelationNodeType.FIELD, docRoot.getCommentType());
		VSFieldNode node = (VSFieldNode) docRoot.getNodeFrom(VSTag.FIELD)[0];
		assertEquals(TestUtil.expectAllTagDescription(), node.getDescription());
		assertEquals("typeの説明です", node.getFieldType());
	}

}
