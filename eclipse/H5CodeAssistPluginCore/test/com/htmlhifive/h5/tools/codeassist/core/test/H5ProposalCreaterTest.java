package com.htmlhifive.h5.tools.codeassist.core.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.htmlhifive.h5.tools.codeassist.core.test.util.TestFilesAndInvocationOffsets;
import com.htmlhifive.h5.tools.codeassist.core.test.util.TestUtilAndConst;
import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;
import com.htmlhifive.tools.codeassist.core.config.ConfigFileParser;
import com.htmlhifive.tools.codeassist.core.config.ConfigFileParserFactory;
import com.htmlhifive.tools.codeassist.core.config.bean.AllBean;
import com.htmlhifive.tools.codeassist.core.exception.ParseException;
import com.htmlhifive.tools.codeassist.core.exception.ProposalCreateException;
import com.htmlhifive.tools.codeassist.core.proposal.H5ProposalCreater;
import com.htmlhifive.tools.codeassist.core.proposal.ProposalContext;

public class H5ProposalCreaterTest {

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
	public void testCreateProposal1() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT1);

	}

	@Test
	public void testCreateProposal2() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT2);
	}

	@Test
	public void testCreateProposal3() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT3);
	}

	@Test
	public void testCreateProposal4() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT4);
	}

	@Test
	public void testCreateProposal5() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT5);
	}

	@Test
	public void testCreateProposal6() throws ParseException, CoreException, ProposalCreateException {

		returnPropTest(TestFilesAndInvocationOffsets.RETURNCHECK6);
	}

	@Test
	public void testCreateProposal7() throws ParseException, CoreException, ProposalCreateException {

		returnPropTest(TestFilesAndInvocationOffsets.RETURNCHECK7);
	}

	@Test
	public void testCreateProposal8() throws ParseException, CoreException, ProposalCreateException {

		returnPropTest(TestFilesAndInvocationOffsets.RETURNCHECK8);
	}

	@Test
	public void testCreateProposal9() throws ParseException, CoreException, ProposalCreateException {

		returnPropTest(TestFilesAndInvocationOffsets.RETURNCHECK9);
	}

	@Test
	public void testCreateProposal10() throws ParseException, CoreException, ProposalCreateException {

		returnPropTest(TestFilesAndInvocationOffsets.RETURNCHECK10);
	}

	@Test
	public void testCreateProposal11() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT11);
	}

	@Test
	public void testCreateProposal12() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT12);
	}

	@Test
	public void testCreateProposalSample() throws ParseException, CoreException, ProposalCreateException {

		noExistPropTest(TestFilesAndInvocationOffsets.SAMPLE_CONTROLLER);
	}

	@Test
	public void testCreateProposal14() throws ParseException, CoreException, ProposalCreateException {

		existPropTest(TestFilesAndInvocationOffsets.UNIT14);
	}

	@Test
	public void testCreateProposal15() throws ParseException, CoreException, ProposalCreateException {

		noExistPropTest(TestFilesAndInvocationOffsets.UNIT15);
	}

	@Test
	public void testEventContextProposal1() throws ParseException, CoreException, ProposalCreateException {

		// existPropTest(TestFilesAndInvocationOffsets.EVENTCONTEXT1);
	}

	@Test
	public void testEventContextProposal2() throws ParseException, CoreException, ProposalCreateException {

		// existPropTest(TestFilesAndInvocationOffsets.EVENTCONTEXT2);
	}

	private void returnPropTest(TestFilesAndInvocationOffsets info) throws ParseException, CoreException,
			ProposalCreateException {

		List<ICompletionProposal> resultList = createProposal(info);
		Assert.assertTrue("Actual size is " + resultList.size(), resultList.size() > 0);
	}

	private void existPropTest(TestFilesAndInvocationOffsets info) throws ParseException, CoreException,
			ProposalCreateException {

		List<ICompletionProposal> resultList = createProposal(info);
		Assert.assertTrue(resultList.size() >= 1);
		for (ICompletionProposal prop : resultList) {
			System.out.println(info.getUnitPath());
			Assert.assertEquals("test1(arg1, arg2) - Object", prop.getDisplayString());
		}
	}

	private List<ICompletionProposal> createProposal(TestFilesAndInvocationOffsets info) throws ParseException,
			CoreException, ProposalCreateException {

		ConfigFileParser parser = ConfigFileParserFactory.createParser(TestUtilAndConst.getTestConfigStream(), "xml");
		AllBean allBean = parser.getCodeAssistBean();
		List<ICompletionProposal> resultList = new ArrayList<ICompletionProposal>();
		new ArrayList<ICompletionProposal>();
		H5ProposalCreater creater = new H5ProposalCreater(new InternalProposalContext(info.getUnitPath(),
				info.getInvocationOffset()), allBean);
		resultList.addAll(creater.createProposal());
		return resultList;
	}

	private void noExistPropTest(TestFilesAndInvocationOffsets info) throws ParseException, CoreException,
			ProposalCreateException {

		List<ICompletionProposal> resultList = createProposal(info);
		Assert.assertEquals(0, resultList.size());

	}

	private static class InternalProposalContext implements ProposalContext {

		private int invocationOffset;
		private IJavaScriptProject project;
		private IJavaScriptUnit unit;

		public InternalProposalContext(String unitPath, int invocationOffset) throws CoreException {

			this.invocationOffset = invocationOffset;
			project = TestUtilAndConst.createTestProject();
			unit = TestUtilAndConst.createJavaScriptUnit(project);
			setContent(unitPath);
		}

		private void setContent(String unitPath) {

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(new File(unitPath)));
				StringBuilder sb = new StringBuilder();
				String readline = null;
				// コード補完までの行数.
				int invocationLine = 0;
				// 文字数.
				int charCount = 0;
				while ((readline = reader.readLine()) != null) {
					sb.append(readline);
					sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
					charCount += readline.length();
					if (charCount <= invocationOffset) {
						invocationLine++;
					}
				}
				invocationOffset += invocationLine * H5CodeAssistCorePluginConst.SEPARATOR_LINE.length();
				unit.getBuffer().setContents(sb.toString());
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (JavaScriptModelException e) {
				throw new RuntimeException(e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		@Override
		public IJavaScriptProject getProject() {

			return project;
		}

		@Override
		public int getInvocationOffset() {

			return invocationOffset;
		}

		@Override
		public IJavaScriptUnit getCompilationUnit() {

			return unit;
		}

		@Override
		public IDocument getDocument() {

			// TODO Auto-generated method stub
			return null;
		}

	}
}
