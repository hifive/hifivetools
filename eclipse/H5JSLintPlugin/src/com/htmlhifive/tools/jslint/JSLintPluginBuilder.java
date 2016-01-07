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
package com.htmlhifive.tools.jslint;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.htmlhifive.tools.jslint.parse.JsParserFactory;
import com.htmlhifive.tools.jslint.parse.Parser;

/**
 * JSLintプラグインビルダークラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JSLintPluginBuilder extends IncrementalProjectBuilder {

	/**
	 * ビルダーID.
	 */
	public static final String BUILDER_ID = "com.htmlhifive.tools.jslint.H5JSLintPlugin.jslintbuilder";

	/**
	 * 
	 * インクリメンタルビルド用のビジタークラス.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	private class IncrementalBuildVisitor implements IResourceDeltaVisitor {

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {

			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				checkJs(resource);
				break;

			case IResourceDelta.CHANGED:
				checkJs(resource);
				break;
			default:
				break;
			}

			return true;
		}
	}

	/**
	 * リソースのパースを行う.<br>
	 * jsファイルのみパース.
	 * 
	 * @param resource 対象リソース.
	 * @throws CoreException 解析例外.
	 */
	private void checkJs(IResource resource) throws CoreException {

		if (resource instanceof IFile
				&& StringUtils.equals(resource.getFileExtension(), JSLintPluginConstant.EXTENTION_JS)) {
			Parser parser = JsParserFactory.createParser(resource);
			try {
				parser.parse(new NullProgressMonitor());
			} catch (InterruptedException e) {
				// ignore
			}
		}

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
	 * java.util.Map<java.lang.String,java.lang.String>,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {

		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	/**
	 * インクリメンタルビルドを行う.
	 * 
	 * @param delta 変更デルタ.
	 * @param monitor モニター.
	 * @throws CoreException 解析例外.
	 */
	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {

		delta.accept(new IncrementalBuildVisitor());
	}

	/**
	 * 
	 * フルビルドを行う.
	 * 
	 * @param monitor モニター.
	 * @throws CoreException 解析例外.
	 */
	private void fullBuild(IProgressMonitor monitor) throws CoreException {

		Parser parser = JsParserFactory.createParser(getProject());
		try {
			parser.parse(monitor);
		} catch (InterruptedException e) {
			// ignore
		}

	}

}
