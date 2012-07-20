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
package jp.co.nssol.h5.tool.jslint.parse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.co.nssol.h5.tool.jslint.Activator;
import jp.co.nssol.h5.tool.jslint.JSLintPluginConstant;
import jp.co.nssol.h5.tool.jslint.library.LibraryManager;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLoggerFactory;
import jp.co.nssol.h5.tool.jslint.messages.Messages;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.jsdt.core.JavaScriptCore;

/**
 * JavaScriptプロジェクト用のパーサー.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JsProjectParser extends JsParser {
	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JsProjectParser.class);

	/**
	 * ライブラリマネージャ.
	 */
	private LibraryManager libManager;

	/**
	 * コンストラクタ.
	 * 
	 * @param resource 対象リソース.
	 */
	public JsProjectParser(IResource resource) {

		super(resource);
		libManager = LibraryManager.getInstance(JavaScriptCore.create(getResource().getProject()));
	}

	@Override
	protected IFile[] getJsFile() throws CoreException {

		if (getResource() instanceof IContainer) {
			// プロジェクト内のjsファイルを取得
			return libManager.getSourceFiles();
		}
		return filterFiles(new IFile[] { (IFile) getResource() });
		// return new IFile[] { (IFile) getResource() };
	}

	/**
	 * 引数のファイル群をインクルードパスと比較し検査されるべきファイル群にフィルタリングする.
	 * 
	 * @param iFiles 検査対象ファイル群
	 * @return 検査されるべきファイル群.
	 */
	private IFile[] filterFiles(IFile[] iFiles) {

		List<IFile> targetFileList = new ArrayList<IFile>();

		for (IFile iFile : iFiles) {
			if (libManager.isTargetFile(iFile)) {
				targetFileList.add(iFile);
			}
		}
		return (IFile[]) targetFileList.toArray(new IFile[targetFileList.size()]);
	}

	@Override
	protected JsFileInfo getLibrary() throws CoreException {

		try {
			// LibraryManager manager = LibraryManager.getInstance();
			File[] externalFiles = libManager.getExternalLibFiles();
			IFile[] internalFiles = libManager.getInternalLibPaths();
			JsFileInfo result = new JsFileInfo();
			for (File file : externalFiles) {
				logger.debug("appendExternalFile : " + file.getAbsolutePath());
				result.append(file);
				logger.debug("lib char count : " + result.getSourceStr().toCharArray().length);
			}
			for (IFile iFile : internalFiles) {
				logger.debug("appendInternalFile : " + iFile.getFullPath());
				result.append(iFile);
			}
			return result;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.EM0100.getText(), e));
		}

	}

	@Override
	void beforeCheck() throws CoreException {

		// チェック前に除外されたファイルのマーカーを除去.
		IFile[] excludeFiles = libManager.getExcludeSourceFiles();
		for (IFile iFile : excludeFiles) {
			iFile.deleteMarkers(JSLintPluginConstant.JS_TYPE_MARKER, true, IResource.DEPTH_INFINITE);
		}
	}
}
