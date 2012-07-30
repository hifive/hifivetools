package com.htmlhifive.h5.tools.codeassist.core.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.WorkingCopyOwner;
import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.internal.codeassist.CompletionEngine;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.core.SearchableEnvironment;
import org.eclipse.wst.jsdt.ui.text.java.CompletionProposalCollector;

import com.htmlhifive.h5.tools.codeassist.core.test.util.TestUtilAndConst;
import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;

@SuppressWarnings("restriction")
public abstract class AbstractVisitorTemplate {
	private int invocationOffset;
	private CompletionParser parser;
	private IJavaScriptUnit unit;
	private static IJavaScriptProject project;
	private static CompilerOptions compilerOptions;

	static {

		try {
			project = TestUtilAndConst.createTestProject();
			compilerOptions = new CompilerOptions(project.getOptions(true));
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

	}

	public AbstractVisitorTemplate(String unitPath, int invocationOffset) throws JavaScriptModelException {

		this.invocationOffset = invocationOffset;
		unit = TestUtilAndConst.createJavaScriptUnit(project);
		setContent(unitPath);
		CompletionProposalCollector requestor = new CompletionProposalCollector(unit);
		SearchableEnvironment environment = newSearchableNameEnvironment(unit, new WorkingCopyOwner() {
		});
		CompletionEngine engine = new CompletionEngine(environment, requestor, project.getOptions(true), project);
		parser = (CompletionParser) engine.getParser();
	}

	public ASTVisitor runVisitor() {

		CompilationUnitDeclaration declaration = parse();
		return traverse(declaration);
	}

	protected abstract ASTVisitor traverse(CompilationUnitDeclaration declaration);

	private SearchableEnvironment newSearchableNameEnvironment(IJavaScriptUnit iJavaScriptUnit, WorkingCopyOwner primary)
			throws JavaScriptModelException {

		return iJavaScriptUnit.getParent() != null ? iJavaScriptUnit.getParent().newSearchableNameEnvironment(primary)
				: iJavaScriptUnit.getJavaScriptProject().newSearchableNameEnvironment(primary);
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

	private CompilationUnitDeclaration parse() {

		CompilationResult compilationResult = new CompilationResult((ICompilationUnit) unit, 1, 1,
				compilerOptions.maxProblemsPerUnit);
		return parser.dietParse((ICompilationUnit) unit, compilationResult, invocationOffset - 1);
	}
}
