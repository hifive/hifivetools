package com.htmlhifive.h5.tools.codeassist.core.test;


import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;

import com.htmlhifive.tools.codeassist.core.proposal.ProposalContext;

/**
 * アダプタクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ProposalContextAdapter implements ProposalContext {

	@Override
	public IDocument getDocument() {

		return null;
	}

	@Override
	public IJavaScriptProject getProject() {

		return null;
	}

	@Override
	public int getInvocationOffset() {

		return 0;
	}

	@Override
	public IJavaScriptUnit getCompilationUnit() {

		return null;
	}

}
