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

import java.io.IOException;


import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.ast.AstRoot;

import com.htmlhifive.tools.rhino.Main;
import com.htmlhifive.tools.rhino.SourceMaker;
import com.htmlhifive.tools.rhino.Main.DocType;

public class AddVSDocCommentNodeTest {

	private static final String TEST_JS = "/test.js";

	private static final String EXPECT_VS = "/expectVS.js";

	private static final String EXPECT_JS = "/expectJS.js";

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
	public void testAddVSDocCommentNode() throws IOException {

		Main main = new Main();
		AstRoot vsAstRoot =
				main.parse(IOUtils.toString(this.getClass().getResourceAsStream(TEST_JS), "UTF-8"), "", DocType.VSDOC);
		Assert.assertEquals(IOUtils.toString(this.getClass().getResourceAsStream(EXPECT_VS), "UTF-8"),
				SourceMaker.toSource(vsAstRoot));
	}

	@Test
	public void testAddJSDocCommentNode() throws IOException {

		Main main = new Main();
		AstRoot jsAstRoot = main.parse(IOUtils.toString(this.getClass().getResourceAsStream(TEST_JS), "UTF-8"), "");
		Assert.assertEquals(IOUtils.toString(this.getClass().getResourceAsStream(EXPECT_JS), "UTF-8"),
				SourceMaker.toSource(jsAstRoot));
	}
}
