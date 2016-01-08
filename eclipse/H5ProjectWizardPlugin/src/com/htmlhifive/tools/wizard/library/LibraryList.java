/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
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
package com.htmlhifive.tools.wizard.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IPackageFragmentRoot;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;

import com.htmlhifive.tools.wizard.library.xml.BaseProject;
import com.htmlhifive.tools.wizard.library.xml.Category;
import com.htmlhifive.tools.wizard.library.xml.Info;
import com.htmlhifive.tools.wizard.library.xml.Libraries;
import com.htmlhifive.tools.wizard.library.xml.Library;
import com.htmlhifive.tools.wizard.library.xml.LibraryRef;
import com.htmlhifive.tools.wizard.library.xml.Site;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.page.tree.CategoryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.RootNode;
import com.htmlhifive.tools.wizard.utils.H5IOUtils;
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

	/** source. */
	private String source = "";

	/** lastModified. */
	private Date lastModified = null;

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
				if (info.getLang() == null || defaultInfo == null) {
					defaultInfo = info;
				}
				if (Locale.getDefault().getLanguage().equals(info.getLang())) {
					targetInfo = info;
				}
			}
			if (targetInfo == null) {
				targetInfo = defaultInfo;
			}
			if (targetInfo != null){
				infoMap.put(targetInfo.getTitle(), targetInfo);
				infoBaseProjectMap.put(targetInfo.getTitle(), baseProject);
			}
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
				IIncludePathEntry noIncudeEntry = null;
				for (IIncludePathEntry entry : jsProject.getResolvedIncludepath(true)) {
					if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE) {
						// Includeの指定がないものを優先的にインストール先として利用する
						if (entry.getInclusionPatterns().length == 0) {
							noIncudeEntry = entry;
							break;
						}
						noIncudeEntry = entry;
					}
				}
				if (noIncudeEntry != null) {
					return ResourcesPlugin.getWorkspace().getRoot().getFolder(noIncudeEntry.getPath());
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
	 * @param jsProject JSプロジェクト
	 * @param projectName プロジェクト名
	 * @param defaultJsLibPath JSライブラリ
	 * @param rootNode ルートノード
	 * @return ライブラリ保存場所.
	 */
	public IContainer[] checkLibrary(IJavaScriptProject jsProject, String projectName, String defaultJsLibPath,
			RootNode rootNode) {

		// TODO:要リファクタ
		//		IContainer defaultInstallContainer = null;
		//		if (StringUtils.isNotEmpty(defaultJsLibPath)) {
		//			// 存在していないので、単なる文字列としておく.
		//			defaultInstallContainer =
		//					ResourcesPlugin.getWorkspace().getRoot().getProject(projectName)
		//					.getFolder(Path.fromOSString(defaultJsLibPath));
		//		}

		IContainer[] checkContainers = getCheckContainers(jsProject);

		rootNode.setDefaultInstallPath(defaultJsLibPath);
		rootNode.setDefaultProjectPath(jsProject != null ? jsProject.getProject() : ResourcesPlugin.getWorkspace()
				.getRoot().getProject(projectName));

		if (rootNode.getChildren() == null) {
			return checkContainers;
		}
		for (TreeNode node : rootNode.getChildren()) {
			CategoryNode categoryNode = (CategoryNode) node;

			categoryNode.setParentPath(null);

			// LibraryNode単位ここから
			for (TreeNode node2 : node.getChildren()) {
				LibraryNode libraryNode = (LibraryNode) node2;

				// 推奨チェック.
				libraryNode.setRecommended(libraryRefMap.containsValue(libraryNode.getValue()));

				// 存在チェック.
				//				if (jsProject != null) {
				libraryNode.setExists(false);
				libraryNode.setState(LibraryState.DEFAULT);

				boolean libraryFullExists = false;
				boolean libraryExists = false;
				IContainer lastContainer = null;
				List<String> lastExistsFileList = null;
				List<String> lastAllFileList = null;
				//List<String> lastNoExistsFileList = null;

				if (checkContainers.length == 0) {
					// 全て存在していない場合(名前だけ設定しておく).
					List<String> noExistsFileList = new ArrayList<String>();
					for (Site site : libraryNode.getValue().getSite()) {
						checkSite(site, null, categoryNode.getInstallSubPath(), null, noExistsFileList);
					}

					libraryNode.setFileList(noExistsFileList.toArray(new String[0]));
					libraryNode.setExists(false); // 一応falseにしておく
					libraryNode.setIncomplete(false);

					continue;
				}

				// Container単位ここから
				for (IContainer container : checkContainers) {
					IContainer folder = container;

					List<String> existsFileList = new ArrayList<String>();
					List<String> noExistsFileList = new ArrayList<String>();
					List<String> allFileList = new ArrayList<String>();

					if (folder.exists()) {//if (folder.getRawLocation() != null) {projectが通らないので.
						// Site単位ここから
						boolean allExists = true;
						for (Site site : libraryNode.getValue().getSite()) {
							// ここで一部不足しているかどうか判る
							if (checkSite(site, folder, categoryNode.getInstallSubPath(), existsFileList,
									noExistsFileList)) {
								// 存在している.
								libraryExists = true;
								lastExistsFileList = existsFileList;
								//lastNoExistsFileList = noExistsFileList;
								lastContainer = container;
							} else {
								// 存在していない.
								allExists = false;
							}
						}
						allFileList.addAll(existsFileList);
						allFileList.addAll(noExistsFileList);
						lastAllFileList = allFileList;

						// Site単位ここまで
						if (allExists) {
							// 全て存在しているので終了.
							libraryFullExists = true;
							lastContainer = container;
							break;
						}
					}
				}
				// Container単位ここまで

				if (libraryFullExists) {
					// 全て存在している場合.
					categoryNode.setParentPath(lastContainer);
					libraryNode.setFileList(lastExistsFileList.toArray(new String[0]));
					libraryNode.setExists(true);
					libraryNode.setIncomplete(false);
					libraryNode.setState(LibraryState.EXISTS);
				} else if (libraryExists) {
					// 一部存在している場合.
					categoryNode.setParentPath(lastContainer);
					libraryNode.setFileList(lastExistsFileList.toArray(new String[0]));
					libraryNode.setExists(false); // 一応falseにしておく
					libraryNode.setIncomplete(true);
					// 存在していないファイルログ
					//for (String name : lastNoExistsFileList) {
					//	H5LogUtils.putLog(null, Messages.SE0081, name);
					//}
				} else {
					// 全て存在していない場合(名前だけ設定しておく).
					libraryNode.setFileList(lastAllFileList.toArray(new String[0]));
					libraryNode.setExists(false); // 一応falseにしておく
					libraryNode.setIncomplete(false);
				}
				//				}
			}
			// LibraryNode単位ここまで
		}
		return checkContainers;
	}

	/**
	 * ライブラリの存在チェックを行うルートフォルダを検索する.
	 * 
	 * @param jsProject JavaScriptプロジェクト
	 * @return ライブラリの存在チェックを行うルートフォルダ
	 */
	private IContainer[] getCheckContainers(IJavaScriptProject jsProject) {

		IContainer[] checkContainers = new IContainer[0];
		if (jsProject != null) {
			//			defaultInstallContainer = getDefaultInstallPath(jsProject);
			checkContainers = new IContainer[] { jsProject.getProject() }; // プロジェクト直下.

			try {
				// JavaScriptのライブラリを検索する.
				Set<IContainer> set = new TreeSet<IContainer>(new Comparator<IContainer>() {
					@Override
					public int compare(IContainer o1, IContainer o2) {
						if (o1 == o2) {
							return 0;
						}
						if (o1 == null) {
							return 1;
						}
						if (o2 == null) {
							return -1;
						}
						return o1.getFullPath().toString().compareToIgnoreCase(o2.getFullPath().toString());
					}
				});
				for (IIncludePathEntry entry : jsProject.getResolvedIncludepath(true)) {
					if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE) {
						// Includeの指定がないものを優先的にインストール先として利用する
						//						if (defaultInstallContainer == null && entry.getInclusionPatterns().length == 0) {
						//							defaultInstallContainer =
						//									ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath());
						//						}
						//x: ResourcesPlugin.getWorkspace().getRoot().getFolder(entry.getPath());
						//o: ResourcesPlugin.getWorkspace().getRoot().getProject(entry.getPath().toString());
						//n: ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(entry.getPath());

						try {
							if (ResourcesPlugin.getWorkspace().getRoot().exists(entry.getPath())) { // 存在チェック.
								IContainer parentContainer = (IContainer) ResourcesPlugin.getWorkspace().getRoot()
										.findMember(entry.getPath());
								set.add(parentContainer);
								if (parentContainer.exists()) {
									addChildFolder(parentContainer, set); // 子階層のフォルダも追加.
								}
							}
						} catch (CoreException ignore) {
							// フォルダの検出だけなので、例外は無視.
							ignore.printStackTrace();
						}

					}
				}
				set.add(jsProject.getProject()); // 一応追加しておく.

				if (!set.isEmpty()) {
					checkContainers = set.toArray(new IContainer[set.size()]);
					//					if (defaultInstallContainer == null) {
					//						defaultInstallContainer = set.iterator().next();
					//					}
				}
			} catch (JavaScriptModelException e) {
				H5LogUtils.putLog(e, Messages.SE0022);
			}
		}
		return checkContainers;
	}

	/**
	 * 子階層のフォルダを追加する.
	 * 
	 * @param parentContainer 親フォルダ
	 * @param set Set
	 * @throws CoreException コア例外
	 */
	private static void addChildFolder(IContainer parentContainer, Set<IContainer> set) throws CoreException {
		for (IResource res : parentContainer.members()) {
			if (res.getType() == IResource.FOLDER) {
				set.add((IContainer) res); // 子階層のフォルダも追加.
				addChildFolder((IContainer) res, set);
			}
		}
	}

	/**
	 * サイトごとのファイル存在チェックを行う.
	 * 
	 * @param site サイト
	 * @param folder フォルダ
	 * @param existsFileList 存在しているファイルリスト
	 * @param noExistsFileList 存在していないファイルリスト
	 * @return 存在していればtrueを返す
	 */
	private boolean checkSite(Site site, IContainer folder, String installSubPath, List<String> existsFileList,
			List<String> noExistsFileList) {

		String siteUrl = site.getUrl();
		String path = H5IOUtils.getURLPath(siteUrl);
		if (path == null) {
			return false;
		}

		IContainer savedFolder = folder;
		if (StringUtils.isNotEmpty(installSubPath) && savedFolder != null) {
			savedFolder = savedFolder.getFolder(Path.fromOSString(installSubPath));
		}

		if (StringUtils.isNotEmpty(site.getExtractPath()) && savedFolder != null) {
			savedFolder = savedFolder.getFolder(Path.fromOSString(site.getExtractPath()));// .getRawLocation().toFile();
		}

		String[] fileList = null;

		String wildCardStr = site.getFilePattern();
		if (path.endsWith(".zip") || path.endsWith(".jar") || wildCardStr != null) {

			// ZIPとか用
			String wildCardPath = "";
			if (wildCardStr != null && wildCardStr.contains("/")) {
				// パス以外を取得.
				wildCardStr = StringUtils.substringAfterLast(site.getFilePattern(), "/");

				// wildCardPathに*が含まれる場合はそこからパスを取得する.
				if (!wildCardStr.contains("*")) { // *の時はパスを除去するので
					wildCardPath = StringUtils.substringBeforeLast(site.getFilePattern(), "/");
					// folder = new File(folder, wildCardPath);
					if (folder != null) {
						savedFolder = folder.getFolder(Path.fromOSString(wildCardPath));
					}
				}
			}

			if (savedFolder != null) {
				if (site.getReplaceFileName() != null) {
					fileList = savedFolder.getRawLocation().toFile()
							.list(new WildcardFileFilter(site.getReplaceFileName()));
				} else {
					if (wildCardStr != null) {
						fileList = savedFolder.getRawLocation().toFile().list(new WildcardFileFilter(wildCardStr));
					} else {
						fileList = savedFolder.getRawLocation().toFile().list();
					}
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
			} else if (savedFolder != null) {
				//noExistsFileList.add(savedFolder.getFullPath().toString() + "/" + wildCardStr);
				noExistsFileList.add(savedFolder.getProjectRelativePath().toString() + "/" + wildCardStr);
				if ((savedFolder.getFullPath().toString() + "/" + wildCardStr).startsWith(savedFolder.getFullPath()
						.toString() + "/")) {
					noExistsFileList.add((savedFolder.getFullPath().toString() + "/" + wildCardStr).substring(folder
							.getFullPath().toString().length() + 1));
				} else {
					noExistsFileList.add(savedFolder.getFullPath().toString() + "/" + wildCardStr);
				}
				//folder
			} else {
				// 存在しないが展開時のファイルとして存在しないリストに追加しておく.
				noExistsFileList.add(wildCardStr);
			}

		} else {
			// zip以外.
			IFile file = null;

			if (savedFolder != null) {
				if (site.getReplaceFileName() != null) {
					file = savedFolder.getFile(Path.fromOSString(site.getReplaceFileName()));
				} else {
					file = savedFolder.getFile(Path.fromOSString(StringUtils.substringAfterLast(path, "/")));
				}
				if (file.exists()) {
					fileList = new String[] { file.getName() };
					existsFileList.add(file.getName());
				} else {
					// 存在しない場合
					if (file.getFullPath().toString().startsWith(folder.getFullPath().toString() + "/")) {
						noExistsFileList.add(file.getFullPath().toString()
								.substring(folder.getFullPath().toString().length() + 1));
					} else {
						noExistsFileList.add(file.getFullPath().toString());
					}
				}
			} else {
				// 存在しないが展開時のファイルとして存在しないリストに追加しておく.
				if (site.getReplaceFileName() != null) {
					noExistsFileList.add(site.getReplaceFileName());
				} else {
					noExistsFileList.add(StringUtils.substringAfterLast(path, "/"));
				}
			}
		}
		// ここで一部不足しているかどうか判る
		if (fileList != null && fileList.length > 0) {
			return true;
		}
		return false;
	}

	/**
	 * ライブラリの情報を取得する.
	 * 
	 * @return ライブラリの情報.
	 */
	public String getInfo() {

		if (getSource() == null) {
			return Messages.PI0152.format(getLastModified());
		}
		return Messages.PI0153.format(getLastModified());
	}

	/**
	 * source.を取得します.
	 * 
	 * @return source.
	 */
	public String getSource() {

		return source;
	}

	/**
	 * source.を設定します.
	 * 
	 * @param source source.
	 */
	public void setSource(String source) {

		this.source = source;
	}

	/**
	 * lastModified.を取得します.
	 * 
	 * @return lastModified.
	 */
	public Date getLastModified() {

		return lastModified;
	}

	/**
	 * lastModified.を設定します.
	 * 
	 * @param lastModified lastModified.
	 */
	public void setLastModified(Date lastModified) {

		this.lastModified = lastModified;
	}
}
