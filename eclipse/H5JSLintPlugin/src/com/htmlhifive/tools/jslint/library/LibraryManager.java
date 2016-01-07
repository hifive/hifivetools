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
package com.htmlhifive.tools.jslint.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;

import com.htmlhifive.tools.jslint.JSLintPluginConstant;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * 
 * プロジェクトのライブラリを管理するクラス.<br>
 * JavaScriptプロジェクトに設定されたインクルード情報の読み込みをする.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class LibraryManager {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(LibraryManager.class);

	/**
	 * プロジェクトとそのプロジェクトに紐づいたクラスパス群.
	 */
	private static Map<IJavaScriptProject, LibraryManager> projectLib = new HashMap<IJavaScriptProject, LibraryManager>();

	/**
	 * javaScriptプロジェクト.
	 */
	private IJavaScriptProject project;

	/**
	 * ライブラリに考慮しないライブラリのID.
	 */
	private Set<String> ignoreRawIdSet;

	/**
	 * コンストラクタ.
	 * 
	 * @param project javaScriptプロジェクト.
	 */
	private LibraryManager(IJavaScriptProject project) {

		this.project = project;
		projectLib.put(project, this);
		ignoreRawIdSet = new HashSet<String>();
		ignoreRawIdSet.add("system.js");
		ignoreRawIdSet.add("dom5.js");
		ignoreRawIdSet.add("browserWindow.js");
		ignoreRawIdSet.add("baseBrowserLibrary.js");
		ignoreRawIdSet.add("xhr.js");
	}

	/**
	 * インスタンスを取得する.
	 * 
	 * @param project jsプロジェクト.
	 * @return jsプロジェクトのライブラリマネージャインスタンス
	 */
	public static LibraryManager getInstance(IJavaScriptProject project) {

		LibraryManager manager = projectLib.get(project);
		if (manager == null) {
			manager = new LibraryManager(project);
		}
		return manager;
	}

	/**
	 * インクルードパスエントリを取得する.
	 * 
	 * @return クラスパスエントリのパス群.
	 */
	public IncludePathEntryWrapper[] getIncludePathEntries() {

		return getIncludePathEntries(project, false);

	}

	/**
	 * インクルードパスエントリを取得する.
	 * 
	 * @return クラスパスエントリのパス群.
	 */
	public IncludePathEntryWrapper[] getRawIncludePathEntries() {

		return getIncludePathEntries(project, true);

	}

	/**
	 * インクルードパスエントリを取得する.
	 * 
	 * @param project jsプロジェクト.
	 * @param raw パスを取得するか.登録されたそのままのものを取得するか.
	 * @return インクルードパスエントリ.
	 */
	private IncludePathEntryWrapper[] getIncludePathEntries(IJavaScriptProject project, boolean raw) {

		try {

			List<IncludePathEntryWrapper> entryList;
			if (raw) {
				entryList = new ArrayList<IncludePathEntryWrapper>(Arrays.asList(IncludePathEntryWrapperFactory
						.getEntryWrappers(project.getRawIncludepath())));
			} else {
				Set<IncludePathEntryWrapper> removeIndex = getRemoveList();
				entryList = new ArrayList<IncludePathEntryWrapper>(Arrays.asList(IncludePathEntryWrapperFactory
						.getEntryWrappers(project.getResolvedIncludepath(true))));
				entryList.removeAll(removeIndex);
			}

			return (IncludePathEntryWrapper[]) entryList.toArray(new IncludePathEntryWrapper[entryList.size()]);
		} catch (JavaScriptModelException e) {
			logger.put(Messages.EM0100, e);
			return null;
		}
	}

	/**
	 * ライブラリマネージャに紐づいたエントリで、無視対象のエントリを取得する.
	 * 
	 * @return 無視対象のエントリ
	 * @throws JavaScriptModelException エントリの取得失敗.
	 */
	private Set<IncludePathEntryWrapper> getRemoveList() throws JavaScriptModelException {

		Set<IncludePathEntryWrapper> removeSet = new HashSet<IncludePathEntryWrapper>();
		IncludePathEntryWrapper[] entrys = IncludePathEntryWrapperFactory.getEntryWrappers(project
				.getResolvedIncludepath(true));
		for (int i = 0; i < entrys.length; i++) {
			IncludePathEntryWrapper entry = entrys[i];
			if (StringUtils.endsWithAny(entry.getPath().toString(),
					(String[]) ignoreRawIdSet.toArray(new String[ignoreRawIdSet.size()]))) {
				removeSet.add(entry);
			}
		}
		return removeSet;

	}

	/**
	 * プロジェクトに紐づいたJavaScriptライブラリのパスを取得する.
	 * 
	 * @return ワークスペース内のパス.
	 */
	public IFile[] getInternalLibPaths() {

		List<IPath> pathList = new ArrayList<IPath>();
		addLibPaths(pathList, project);
		List<IFile> fileList = new ArrayList<IFile>();
		for (IPath iPath : pathList) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(iPath);
			if (resource != null) {
				fileList.add((IFile) resource);
			}
		}
		return (IFile[]) fileList.toArray(new IFile[fileList.size()]);

	}

	/**
	 * 外部指定されているライブラリファイルを取得する.
	 * 
	 * @param 対象プロジェクト
	 * @return ワークスペース外のライブラリ.jsファイル
	 */
	public File[] getExternalLibFiles() {

		List<IPath> pathList = new ArrayList<IPath>();
		addLibPaths(pathList, project);
		List<File> fileList = new ArrayList<File>();
		for (IPath iPath : pathList) {
			if (!(ResourcesPlugin.getWorkspace().getRoot().exists(iPath))) {
				fileList.add(iPath.toFile());
			}
		}
		return (File[]) fileList.toArray(new File[fileList.size()]);
	}

	/**
	 * 引数のリストに、プロジェクトに紐づいたインクルードパス（ライブラリ）を追加する.
	 * 
	 * @param pathList 追加されるパスリスト.
	 * @param project pathListに追加するパスを保持したプロジェクト.
	 */
	private void addLibPaths(final List<IPath> pathList, IJavaScriptProject project) {

		IncludePathEntryWrapper[] entries = getIncludePathEntries(project, false);
		for (IncludePathEntryWrapper iEntries : entries) {
			if (IncludePathEntryWrapper.CPE_PROJECT == iEntries.getEntryKind()) {
				addLibPaths(
						pathList,
						JavaScriptCore.create((IProject) ResourcesPlugin.getWorkspace().getRoot()
								.findMember(iEntries.getPath())));
			} else if (IncludePathEntryWrapper.CPE_LIBRARY == iEntries.getEntryKind()) {
				// リソースが.jsファイルかまたはフォルダの場合
				IPath path = iEntries.getPath();
				if (StringUtils.equals(path.getFileExtension(), JSLintPluginConstant.EXTENTION_JS)) {
					// ライブラリが.jsファイル
					pathList.add(path);
				} else {
					// ライブラリがフォルダ.
					IContainer libFolder = (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(path);
					try {
						libFolder.accept(new IResourceVisitor() {

							@Override
							public boolean visit(IResource resource) throws CoreException {

								if (StringUtils.equals(resource.getFileExtension(), //
										JSLintPluginConstant.EXTENTION_JS)) {
									// ライブラリが.jsファイル
									pathList.add(resource.getFullPath());
								}
								return true;
							}
						});
					} catch (CoreException e) {
						logger.put(Messages.EM0100, e);
					}
				}
			}
		}

	}

	/**
	 * 対象ソースファイルを取得する.
	 * 
	 * @return ソースファイル群.
	 */
	public IFile[] getSourceFiles() {

		IncludePathEntryWrapper[] entries = getIncludePathEntries(project, false);
		final List<IFile> fileList = new ArrayList<IFile>();
		for (IncludePathEntryWrapper iEntry : entries) {
			if (IncludePathEntryWrapper.CPE_SOURCE == iEntry.getEntryKind()) {
				IResource srcDir = ResourcesPlugin.getWorkspace().getRoot().findMember(iEntry.getPath());
				if (null != srcDir) {

					final IPath[] includePaths = iEntry.getFullInclusionPatterns();
					final IPath[] excludePaths = iEntry.getFullExclusionPatterns();

					try {
						srcDir.accept(new IResourceVisitor() {

							@Override
							public boolean visit(IResource resource) throws CoreException {

								if (resource instanceof IFile) {
									if (!StringUtils.equals(resource.getFileExtension(),
											JSLintPluginConstant.EXTENTION_JS)) {
										return true;
									}
									if (null == includePaths || includePaths.length == 0) {
										fileList.add((IFile) resource);
									} else {
										includeCheck: for (IPath iPath : includePaths) {
											char[] include = iPath.toString().toCharArray();
											if (CharOperation.pathMatch(include, resource.getFullPath().toString()
													.toCharArray(), true, IPath.SEPARATOR)) {
												fileList.add((IFile) resource);
												break includeCheck;
											}
										}
									}
									if (!(null == excludePaths || excludePaths.length == 0)) {

										excludeCheck: for (IPath iPath : excludePaths) {
											char[] exclude = iPath.toString().toCharArray();
											if (CharOperation.pathMatch(exclude, resource.getFullPath().toString()
													.toCharArray(), true, IPath.SEPARATOR)) {
												fileList.remove((IFile) resource);
												break excludeCheck;
											}
										}
									}
								}
								return true;
							}
						});
					} catch (CoreException e) {
						logger.put(Messages.EM0100, e);
					}
				}

			}
		}

		return (IFile[]) fileList.toArray(new IFile[fileList.size()]);
	}

	/**
	 * 無視するライブラリのIDをプロジェクトに追加する.
	 * 
	 * @param fileName 無視するライブラリファイル名.
	 */
	public void addIgnoreName(String fileName) {

		ignoreRawIdSet.add(fileName);
	}

	/**
	 * ソースフォルダ内でチェック対象外のファイルを取得する.
	 * 
	 * @return チェック対象外のファイル.
	 */
	public IFile[] getExcludeSourceFiles() {

		IncludePathEntryWrapper[] entries = getIncludePathEntries(project, false);
		final List<IFile> excludeFileList = new ArrayList<IFile>();
		for (IncludePathEntryWrapper iEntry : entries) {
			if (IncludePathEntryWrapper.CPE_SOURCE == iEntry.getEntryKind()) {
				IResource srcDir = ResourcesPlugin.getWorkspace().getRoot().findMember(iEntry.getPath());
				if (null != srcDir) {

					final IPath[] includePaths = iEntry.getFullInclusionPatterns();
					final IPath[] excludePaths = iEntry.getFullExclusionPatterns();

					try {
						srcDir.accept(new IResourceVisitor() {

							@Override
							public boolean visit(IResource resource) throws CoreException {

								if (resource instanceof IFile) {
									if (!StringUtils.equals(resource.getFileExtension(),
											JSLintPluginConstant.EXTENTION_JS)) {
										return true;
									}
									if (!(null == includePaths || includePaths.length == 0)) {
										for (IPath iPath : includePaths) {
											char[] include = iPath.toString().toCharArray();
											if (!CharOperation.pathMatch(include, resource.getFullPath().toString()
													.toCharArray(), true, IPath.SEPARATOR)) {
												excludeFileList.add((IFile) resource);
												return true;
											}
										}
									}
									if (!(null == excludePaths || excludePaths.length == 0)) {

										for (IPath iPath : excludePaths) {
											char[] exclude = iPath.toString().toCharArray();
											if (CharOperation.pathMatch(exclude, //
													resource.getFullPath().toString().toCharArray(), //
													true, IPath.SEPARATOR)) {
												excludeFileList.add((IFile) resource);
												return true;
											}
										}
									}
								}
								return true;
							}
						});
					} catch (CoreException e) {
						logger.put(Messages.EM0100, e);
					}
				}

			}
		}

		return (IFile[]) excludeFileList.toArray(new IFile[excludeFileList.size()]);
	}

	/**
	 * 引数のファイルが検査対象であるかどうかをチェックする.
	 * 
	 * @param iFile 比較ファイル
	 * @return ファイルが検査対象であればtrue,そうでない場合はfalse
	 */
	public boolean isTargetFile(IFile iFile) {

		return ArrayUtils.contains(getSourceFiles(), iFile);
	}
}
