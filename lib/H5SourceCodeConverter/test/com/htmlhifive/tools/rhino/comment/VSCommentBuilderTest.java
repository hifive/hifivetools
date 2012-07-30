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


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.htmlhifive.tools.rhino.comment.vs.VSCommentBuilder;
import com.htmlhifive.tools.rhino.comment.vs.VSDocRoot;

public class VSCommentBuilderTest {

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
	public void testFunctionBuilder() {

		VSDocRoot root = TestUtil.createFunctionVSDocRoot();
		VSCommentBuilder builder = new VSCommentBuilder(root);
		String str = builder.build();
		Assert.assertEquals(TestUtil.expectVSDocParam(), str);
		System.out.println(str);
	}

	@Test
	public void testVarBuilder() {

		VSDocRoot root = TestUtil.createVarVSDocRoot();
		VSCommentBuilder builder = new VSCommentBuilder(root);
		String str = builder.build();
		System.out.println(str);
	}

	@Test
	public void testFieldBuilder() {

		VSDocRoot root = TestUtil.createFieldVSDocRoot();
		VSCommentBuilder builder = new VSCommentBuilder(root);
		String str = builder.build();
		System.out.println(str);
	}

}
