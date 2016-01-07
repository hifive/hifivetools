package com.htmlhifive.h5.tools.codeassist.core.test.util;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;

public class TestUtil {
	public static String createXmlString() {

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<h5-code-assist xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"h5-code-assist.xsd\">");
		sb.append("<codeassists suffix=\"Controller\">");
		sb.append("<codeassist method=\"test1\" returnType=\"ReturnType1\">");
		sb.append("<helpdoc><![CDATA[<b>テストテスト</b>]]></helpdoc>");
		sb.append("<argument name=\"arg1\" type=\"Object\"/>");
		sb.append("<argument name=\"arg2\" type=\"Object\"/>");
		sb.append("</codeassist>");
		sb.append("</codeassists>");
		sb.append("<codeassists suffix=\"View\">");
		sb.append("<codeassist returnType=\"ReturnType2\" method=\"Test2\">");
		sb.append("<helpdoc>ああああああ</helpdoc>");
		sb.append("<argument name=\"arg\" type=\"String\"/>");
		sb.append("</codeassist>");
		sb.append("</codeassists>");
		sb.append("</h5-code-assist>");
		return sb.toString();
	}

	public static String createRealCode() {

		StringBuilder sb = new StringBuilder();
		sb.append("var ReturnType1 = function(){};");
		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		sb.append("var arg1,arg2;");
		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		sb.append("ReturnType1.prototype.returnString = function(){ return new ReturnType1();}");
		sb.append(H5CodeAssistCorePluginConst.SEPARATOR_LINE);
		return sb.toString();
	}

	public static IJavaScriptProject createTestProject() throws CoreException {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject("TestProject");
		project.delete(true, null);
		project.create(null);
		project.open(null);
		return JavaScriptCore.create(project);
	}

	public static IJavaScriptUnit createJavaScriptUnit(IJavaScriptProject project) throws JavaScriptModelException {

		return project.getPackageFragments()[0].getJavaScriptUnit("sample.js").getWorkingCopy(null);
	}

}
