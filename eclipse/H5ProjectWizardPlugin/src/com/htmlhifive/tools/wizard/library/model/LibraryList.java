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
 */
package com.htmlhifive.tools.wizard.library.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IPackageFragmentRoot;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;

import com.htmlhifive.tools.wizard.library.model.xml.BaseProject;
import com.htmlhifive.tools.wizard.library.model.xml.Category;
import com.htmlhifive.tools.wizard.library.model.xml.Info;
import com.htmlhifive.tools.wizard.library.model.xml.Libraries;
import com.htmlhifive.tools.wizard.library.model.xml.Library;
import com.htmlhifive.tools.wizard.library.model.xml.LibraryRef;
import com.htmlhifive.tools.wizard.library.model.xml.Site;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.page.tree.CategoryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.RootNode;
import com.htmlhifive.tools.wizard.utils.H5LogUtils;
import com.htmlhifive.tools.wizard.utils.H5StringUtils;

/**
 * <H3>ライブラリリスト.</H3>
 * 
 * @author fkubo
 */
public class LibraryList {

	/** libraries. */
	private final Libraries libraries;

	/** libraryRefMap. */
	private final Map<LibraryRef, Library> libraryRefMap = new LinkedHashMap<LibraryRef, Library>();

	/** infoMap. */
	private final Map<String, Info> infoMap = new LinkedHashMap<String, Info>();

	/** infoBaseProjectMap. */
	private final Map<String, BaseProject> infoBaseProjectMap = new LinkedHashMap<String, BaseProject>();

	/** defaultJsLibPath. */
	private String defaultJsLibPath = null;

	/**
	 * コンストラクタ.
	 * 
	 * @param libraries libraries.
	 */
	public LibraryList(Libraries libraries) {

		this.libraries = libraries;
	}

	/**
	 * librariesを取得します.
	 * 
	 * @return libraries
	 */
	public Libraries getLibraries() {

		return libraries;
	}

	/**
	 * infoMapを取得します。
	 * 
	 * @return infoMap
	 */
	public Map<String, Info> getInfoMap() {

		return infoMap;
	}

	/**
	 * infoBaseProjectMapを取得します。
	 * 
	 * @return infoBaseProjectMap
	 */
	public Map<String, BaseProject> getInfoBaseProjectMap() {

		return infoBaseProjectMap;
	}

	/**
	 * 初期化処理.
	 */
	public void init() {

		// Defaultに対する追加処理.
		for (Category category : libraries.getSiteLibraries().getCategory()) {
			// Licenseの加工.
			if (category.getLicense() != null) {
				category.setLicense(StringUtils.chomp(category.getLicense()));
			}

			// Info情報修正.
			for (Info info : category.getInfo()) {
				info.setTitle(H5StringUtils.trim(info.getTitle()));
				info.setDescription(H5StringUtils.trim(info.getDescription()));
			}

			// Libraryの追加.
			for (Library library : category.getLibrary()) {

				// 推奨設定のLibraryData の作成.
				for (LibraryRef libraryRef : libraries.getDefaultLibraries().getLibraryRef()) {
					if (libraryRef.getOrg().equals(category.getOrg())
							&& libraryRef.getName().equals(category.getName())
							&& libraryRef.getVersion().equals(library.getVersion())) {
						libraryRefMap.put(libraryRef, library);
					}
				}

			}
		}

		// プロジェクト一覧を加工.
		for (BaseProject baseProject : libraries.getBaseProjects().getBaseProject()) {
			Info targetInfo = null;
			Info defaultInfo = null;
			for (Info info : baseProject.getInfo()) {
				if (info.getLang() == null) {
					defaultInfo = info;
				}
				if (Locale.getDefault().getLanguage().equals(info.getLang())) {
					targetInfo = info;
				}
			}
			if (targetInfo == null) {
				targetInfo = defaultInfo;
			}
			infoMap.put(targetInfo.getTitle(), targetInfo);
			infoBaseProjectMap.put(targetInfo.getTitle(), baseProject);
		}
	}

	/**
	 * デフォルトのインストール先を取得する.
	 * 
	 * @param jsProject JsProject
	 * @return デフォルトのインストール先
	 */
	public IContainer getDefaultInstallPath(IJavaScriptProject jsProject) {

		if (jsProject != null) {
			try {
				// JavaScriptのライブラリを検索する.
				IPackageFragmentRoot noIncudeEntry = null;
				for (IPackageFragmentRoot entry : jsProject.getPackageFragmentRoots()) {
					if (entry.getResource() instanceof IContainer) {
						// Includeの指定がないものを優先的にインストール先として利用する
						if (entry.getRawIncludepathEntry().getInclusionPatterns().length == 0) {
							noIncudeEntry = entry;
							break;
						}
						noIncudeEntry = entry;
					}
				}
				if (noIncudeEntry != null) {
					return (IContainer) noIncudeEntry.getResource();
				}
			} catch (JavaScriptModelException e) {
				H5LogUtils.putLog(e, Messages.SE0022);
			}
			return jsProject.getProject();
		}
		return null;
	}

	/**
	 * ライブラリ存在チェック.<br>
	 * 
	 * @param jsProject 対象JSプロジェクト
	 * @param rootNode ルートノード
	 */
	public void checkLibrary(IJavaScriptProject jsProject, RootNode rootNode) {

		IContainer defaultInstallContainer = null;
		if (defaultJsLibPath != null) {
			defaultInstallContainer =
					ResourcesPlugin.getWorkspace().getRoot().getFolder(Path.fromOSString(defaultJsLibPath));
		}

		IContainer[] checkContainers = new IContainer[0];
		if (jsProject != null) {
			checkContainers = new IContainer[] { jsProject.getProject() }; // プロジェクト直下.

			try {
				// JavaScriptのライブラリを検索する.
				Set<IContainer> set = new LinkedHashSet<IContainer>();
				for (IPackageFragmentRoot entry : jsProject.getPackageFragmentRoots()) {
					if (entry.getResource() instanceof IContainer) {
						// Includeの指定がないものを優先的にインストール先として利用する
						if (defaultInstallContainer == null
								&& entry.getRawIncludepathEntry().getInclusionPatterns().length == 0) {
							defaultInstallContainer = (IContainer) entry.getResource();
						}
						set.add((IContainer) entry.getResource());
					}
				}
				set.add(jsProject.getProject()); // 一応追加しておく.

				if (!set.isEmpty()) {
					checkContainers = set.toArray(new IContainer[0]);
					if (defaultInstallContainer == null) {
						defaultInstallContainer = set.iterator().next();
					}
				}
			} catch (JavaScriptModelException e) {
				H5LogUtils.putLog(e, Messages.SE0022);
			}
		}

		for (TreeNode node : rootNode.getChildren()) {
			CategoryNode categoryNode = (CategoryNode) node;
			if (jsProject != null) {
				categoryNode.setParentPath(defaultInstallContainer, jsProject.getProject());
			} else {
				categoryNode.setParentPath(defaultInstallContainer, null);
			}

			for (TreeNode node2 : node.getChildren()) {
				if (node2 instanceof LibraryNode) {
					LibraryNode libraryNode = (LibraryNode) node2;

					// 推奨チェック.
					libraryNode.setRecommended(libraryRefMap.containsValue(libraryNode.getValue()));

					// 存在チェック.
					if (jsProject != null) {
						libraryNode.setExists(false);
						libraryNode.setState(LibraryState.DEFAULT);

						boolean libraryFullExists = false;
						boolean libraryExists = false;

						IContainer lastContainer = null;
						List<String> lastExistsFileList = null;
						List<String> lastNoExistsFileList = null;
						for (IContainer container : checkContainers) {
							boolean allExists = true;
							libraryExists = false;
							List<String> existsFileList = new ArrayList<String>();
							List<String> noExistsFileList = new ArrayList<String>();

							IContainer entryFolder = container;
							if (StringUtils.isNotEmpty(categoryNode.getInstallSubPath())) {
								entryFolder = container.getFolder(Path.fromOSString(categoryNode.getInstallSubPath()));
							}

							if (entryFolder.getRawLocation() != null) {
								for (Site site : libraryNode.getValue().getSite()) {
									IContainer folder;
									if (site.getExtractPath() != null) {
										folder = entryFolder.getFolder(Path.fromOSString(site.getExtractPath()));// .getRawLocation().toFile();
									} else {
										folder = entryFolder;// .getRawLocation().toFile();
									}

									String[] fileList = null;

									String wildCardStr = site.getFilePattern();
									if (site.getUrl().endsWith(".zip") || site.getUrl().endsWith(".jar")
											|| site.getFilePattern() != null) {
										// ZIPとか用
										String wildCardPath = "";
										if (wildCardStr != null && wildCardStr.contains("/")) {
											// パス以外を取得.
											wildCardStr = StringUtils.substringAfterLast(site.getFilePattern(), "/");

											// パスを取得.
											if (!wildCardStr.contains("*")) { // *の時はパスを除去するので
												wildCardPath =
														StringUtils.substringBeforeLast(site.getFilePattern(), "/");
												// folder = new File(folder, wildCardPath);
												folder = entryFolder.getFolder(Path.fromOSString(wildCardPath));
											}
										}

										if (site.getReplaceFileName() != null) {
											fileList =
													folder.getRawLocation().toFile()
													.list(new WildcardFileFilter(site.getReplaceFileName()));
										} else if (folder != null) {
											if (wildCardStr != null) {
												fileList =
														folder.getRawLocation().toFile()
														.list(new WildcardFileFilter(wildCardStr));
											} else {
												fileList = folder.getRawLocation().toFile().list();
											}
										}
										if (fileList != null && fileList.length > 0) {
											if (!wildCardPath.isEmpty()) {
												for (String fileName : fileList) {
													existsFileList.add(wildCardPath + "/" + fileName);
												}
											} else {
												existsFileList.addAll(Arrays.asList(fileList));
											}
										} else {
											noExistsFileList.add(folder.getFullPath().toString() + "/" + wildCardStr);
										}
									} else {
										// zip以外.
										IFile file = null;
										if (site.getReplaceFileName() != null) {
											file = folder.getFile(Path.fromOSString(site.getReplaceFileName()));
										} else {
											file =
													folder.getFile(Path.fromOSString(StringUtils.substringAfterLast(
															site.getUrl(), "/")));
										}
										if (file.exists()) {
											fileList = new String[] { file.getName() };
											existsFileList.add(file.getName());
										} else {
											noExistsFileList.add(file.getFullPath().toString());
										}
									}

									// ここで一部不足しているかどうか判る
									if (fileList != null && fileList.length > 0) {
										// 1つ以上は存在している.
										libraryExists = true;
										lastExistsFileList = existsFileList;
										lastNoExistsFileList = noExistsFileList;
										lastContainer = container;
									} else {
										allExists = false;
									}
								}
								if (allExists) {
									// 全て存在しているので終了.
									libraryFullExists = true;
									lastContainer = container;
									break;
								}
							}
						}
						if (libraryFullExists) {
							// 全て存在している場合.
							categoryNode.setParentPath(lastContainer, null);
							libraryNode.setFileList(lastExistsFileList.toArray(new String[0]));
							libraryNode.setExists(true);
							libraryNode.setState(LibraryState.EXISTS);
						} else if (libraryExists) {
							// 一部存在している場合.
							categoryNode.setParentPath(lastContainer, null);
							libraryNode.setFileList(lastExistsFileList.toArray(new String[0]));
							libraryNode.setExists(false);
							libraryNode.setState(LibraryState.INCOMPLETE);
							for (String name : lastNoExistsFileList) {
								H5LogUtils.putLog(null, Messages.SE0081, name);
							}
						} else {
							// 存在していない場合.
							// categoryNode.setParentPath(defaultInstallContainer, jsProject.getProject());
							// libraryNode.setExists(false);
							// libraryNode.setState(LibraryState.DEFAULT);
						}

						// try {
						// JSのライブラリでループ.
						// for (IPackageFragmentRoot entry : jsProject.getPackageFragmentRoots()) {
						// // IFolder libFolder = jsProject.getProject().getParent().getFolder(((IPackageFragmentRoot)
						// // entry).getPath()); // TODO:
						//
						// jsProject.
						//
						// entry.getPath().toFile()
						//
						// IFolder libFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath());
						// IFolder entryFolder = libFolder.getFolder(libraryNode.getParent().getValue().getPath()); //
						// if (entryFolder.getRawLocation() != null) {
						// for (Site site : libraryNode.getValue().getSite()) {
						// File file = entryFolder.getRawLocation().toFile();
						// String[] fileList = null;
						// if (file != null) {
						// fileList = file.list(new WildcardFileFilter(site.getFilePattern()));
						// }
						// if (fileList != null && fileList.length > 0) {
						// // if (FilenameUtils.wildcardMatch(entryFolder.getFullPath().toString(),
						// // site.getFilePattern())) {
						// // 存在している.
						// allExists = true;
						// } else {
						// allExists = false;
						// break;
						// }
						// }
						// }
						// if (allExists) {
						// libraryNode.setExists(true);
						// libraryNode.setState(LibraryState.EXISTS);
						// break;
						// }
						// }
						// } catch (JavaScriptModelException e) {
						// // TODO 自動生成された catch ブロック
						// e.printStackTrace();
						// }
					}
				}
			}
		}
	}

	/**
	 * defaultJsLibPath.を取得します.
	 * 
	 * @return defaultJsLibPath.
	 */
	public String getDefaultJsLibPath() {
		return defaultJsLibPath;
	}

	/**
	 * defaultJsLibPath.を設定します.
	 * 
	 * @param defaultJsLibPath defaultJsLibPath.
	 */
	public void setDefaultJsLibPath(String defaultJsLibPath) {
		this.defaultJsLibPath = defaultJsLibPath;
	}
}
