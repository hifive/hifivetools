package com.htmlhifive.tools.rhino.comment;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.ast.AstRoot;

import com.htmlhifive.tools.rhino.Main;
import com.htmlhifive.tools.rhino.SourceMaker;

public class SuppressLoggerTest {

	private static final String TEST_JS = "/h5.dev.js";
	private static final String EXPECT_JS = "/expect.h5.dev.js";

	@Test
	public void test() throws IOException {
		Main main = new Main();
		AstRoot jsAstRoot = main.parse(IOUtils.toString(this.getClass().getResourceAsStream(TEST_JS), "UTF-8"), "");
		Assert.assertEquals(IOUtils.toString(this.getClass().getResourceAsStream(EXPECT_JS), "UTF-8"),
				SourceMaker.toSource(jsAstRoot));
	}

}
