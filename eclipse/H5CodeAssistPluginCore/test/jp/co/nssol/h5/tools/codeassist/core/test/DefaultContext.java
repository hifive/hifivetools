package jp.co.nssol.h5.tools.codeassist.core.test;

import jp.co.nssol.h5.tools.codeassist.core.test.util.TestUtilAndConst;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;

public class DefaultContext extends ProposalContextAdapter {
	private final String assistStr;
	private IJavaScriptProject project;

	public DefaultContext(String assistStr, IJavaScriptProject project) {

		this.assistStr = assistStr;
		this.project = project;
	}

	@Override
	public int getInvocationOffset() {

		StringBuilder sb = new StringBuilder(TestUtilAndConst.createRealCode());
		sb.append(assistStr);
		return sb.lastIndexOf(assistStr) + assistStr.length();
	}

	@Override
	public IDocument getDocument() {

		return null;
	}

	@Override
	public IJavaScriptProject getProject() {

		return project;
	}

	@Override
	public IJavaScriptUnit getCompilationUnit() {

		StringBuilder sb = new StringBuilder(TestUtilAndConst.createRealCode());
		try {
			IJavaScriptUnit unit = project.getPackageFragments()[0].getJavaScriptUnit("sample.js").getWorkingCopy(null);
			sb.append(assistStr);
			unit.getBuffer().setContents(sb.toString());
			return unit;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
