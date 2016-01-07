package com.htmlhifive.h5.tools.codeassist.core.test.xml;

import java.io.FileInputStream;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.htmlhifive.tools.codeassist.core.config.ConfigFileParser;
import com.htmlhifive.tools.codeassist.core.config.ConfigFileParserFactory;
import com.htmlhifive.tools.codeassist.core.config.bean.AllBean;
import com.htmlhifive.tools.codeassist.core.config.bean.ControllerBean;
import com.htmlhifive.tools.codeassist.core.config.bean.EventContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.FunctionBean;
import com.htmlhifive.tools.codeassist.core.config.bean.InitializationContextBean;
import com.htmlhifive.tools.codeassist.core.config.bean.LogicBean;
import com.htmlhifive.tools.codeassist.core.config.bean.RootChildrenElem;
import com.htmlhifive.tools.codeassist.core.config.bean.VarReferenceBean;
import com.htmlhifive.tools.codeassist.core.config.bean.VariableBean;
import com.htmlhifive.tools.codeassist.core.exception.ParseException;

public class XmlConfigCodeAssistParserTest {
	private static String DIRECTORY = "./testPlugin/testjs/configTest/";
	private static String CONTROLLER = DIRECTORY + "h5-code-assist-controller.xml";
	private static String LOGIC = DIRECTORY + "h5-code-assist-logic.xml";
	private static String EVENTCONTEXT = DIRECTORY + "h5-code-assist-eventContext.xml";
	private static String INITIALCONTEXT = DIRECTORY + "h5-code-assist-initContext.xml";
	private static String ALL = DIRECTORY + "h5-code-assist-all.xml";
	private static ConfigFileParser parserController;
	private static ConfigFileParser parserLogic;
	private static ConfigFileParser parserEventContext;
	private static ConfigFileParser parserInitContext;
	private static ConfigFileParser parserAll;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		parserController = ConfigFileParserFactory.createParser(new FileInputStream(CONTROLLER), "xml");
		parserLogic = ConfigFileParserFactory.createParser(new FileInputStream(LOGIC), "xml");
		parserEventContext = ConfigFileParserFactory.createParser(new FileInputStream(EVENTCONTEXT), "xml");
		parserInitContext = ConfigFileParserFactory.createParser(new FileInputStream(INITIALCONTEXT), "xml");
		parserAll = ConfigFileParserFactory.createParser(new FileInputStream(ALL), "xml");

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
	public void testController() throws ParseException {

		AllBean allbean = parserController.getCodeAssistBean();
		RootChildrenElem[] elems = allbean.getElemList();
		Assert.assertEquals(1, elems.length);
		ControllerBean bean = (ControllerBean) elems[0];
		rootControllerBeanCheck(bean);
	}

	@Test
	public void testLogic() throws ParseException {

		AllBean allbean = parserLogic.getCodeAssistBean();
		RootChildrenElem[] elems = allbean.getElemList();
		Assert.assertEquals(1, elems.length);
		LogicBean bean = (LogicBean) elems[0];
		rootLogicBeanCheck(bean);
	}

	@Test
	public void testEventContext() throws ParseException {

		AllBean allbean = parserEventContext.getCodeAssistBean();
		RootChildrenElem[] elems = allbean.getElemList();
		Assert.assertEquals(1, elems.length);
		EventContextBean bean = (EventContextBean) elems[0];
		rootEventContextBeanCheck(bean);

	}

	@Test
	public void testInitContext() throws ParseException {

		AllBean allbean = parserInitContext.getCodeAssistBean();
		RootChildrenElem[] elems = allbean.getElemList();
		Assert.assertEquals(1, elems.length);
		InitializationContextBean bean = (InitializationContextBean) elems[0];
		rootInitContextBeanCheck(bean);

	}

	@Test
	public void testAll() throws ParseException {

		AllBean allbean = parserAll.getCodeAssistBean();
		RootChildrenElem[] elems = allbean.getElemList();
		Assert.assertEquals(4, elems.length);
		for (RootChildrenElem elem : elems) {
			rootChildrenCheck(elem);
		}
	}

	private void rootInitContextBeanCheck(InitializationContextBean bean) {

		Assert.assertEquals(".*Controller", bean.getRegExPattern().pattern());
		objectLiteralCheck(bean.getFunctions(), bean.getVarRefs());

	}

	private void rootEventContextBeanCheck(EventContextBean bean) {

		Assert.assertEquals(".*Controller", bean.getRegExPattern().pattern());
		objectLiteralCheck(bean.getFunctions(), bean.getVarRefs());
	}

	private void rootControllerBeanCheck(ControllerBean bean) {

		Assert.assertEquals(".*Controller", bean.getRegExPattern().pattern());
		objectLiteralCheck(bean.getFunctions(), bean.getVarRefs());
	}

	private void objectLiteralCheck(FunctionBean[] functions, VarReferenceBean[] varRefs) {

		varRefCheck(varRefs);
		test1FunctionCheck(functions);

	}

	private void rootLogicBeanCheck(LogicBean bean) {

		Assert.assertEquals(".*Logic", bean.getRegExPattern().pattern());
		objectLiteralCheck(bean.getFunctions(), bean.getVarRefs());
	}

	private void rootChildrenCheck(RootChildrenElem elem) {

		if (elem instanceof LogicBean) {
			rootLogicBeanCheck((LogicBean) elem);
		}
		if (elem instanceof ControllerBean) {
			rootControllerBeanCheck((ControllerBean) elem);
		}
		if (elem instanceof EventContextBean) {
			rootEventContextBeanCheck((EventContextBean) elem);
		}
		if (elem instanceof InitializationContextBean) {
			rootInitContextBeanCheck((InitializationContextBean) elem);
		}
	}

	private void varRefCheck(VarReferenceBean[] varRefs) {

		Assert.assertEquals(1, varRefs.length);
		Assert.assertEquals("varRef", varRefs[0].getKey());
		Assert.assertEquals("String", varRefs[0].getClassName());

	}

	private void test1FunctionCheck(FunctionBean[] functions) {

		Assert.assertEquals(1, functions.length);
		FunctionBean bean = functions[0];
		functionCheck(bean, "test1", "arg1", "arg2", "ReturnType1", "<b>テスト1テスト1</b>");
	}

	private void functionCheck(FunctionBean bean, String name, String arg1Name, String arg2Name, String returnType,
			String helpDoc) {

		Assert.assertEquals(name, bean.getName());
		VariableBean[] args = bean.getArgments();
		Assert.assertEquals(2, args.length);
		Assert.assertEquals(arg1Name, args[0].getName());
		Assert.assertEquals(arg2Name, args[1].getName());
		Assert.assertEquals("Object", args[0].getType());
		Assert.assertEquals("Object", args[1].getType());
		Assert.assertEquals(helpDoc, bean.getDescription());
		Assert.assertEquals(returnType, bean.getReturnType());
	}
}
