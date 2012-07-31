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
package com.htmlhifive.tools.wizard.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeNode;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.library.model.LibraryState;
import com.htmlhifive.tools.wizard.library.model.xml.BaseProject;
import com.htmlhifive.tools.wizard.library.model.xml.Category;
import com.htmlhifive.tools.wizard.library.model.xml.Library;
import com.htmlhifive.tools.wizard.library.model.xml.Site;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.page.tree.CategoryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.utils.H5IOUtils;
import com.htmlhifive.tools.wizard.utils.H5LogUtils;

/**
 * <H3>ライブラリダウンロード用モジュール.</H3>
 * 
 * @author fkubo
 */
public class DownloadModule {

	private final int DEFAULT_BUFFER_SIZE = 4096;

	/** 外部接続用ProxyTracker. */
	private final ServiceTracker<IProxyService, Object> proxyTracker;

	/** defaultOverwriteMode. */
	private int defaultOverwriteMode = 0;

	/** lastDownloadStatus. */
	private boolean lastDownloadStatus = false;

	/**
	 * デフォルトコンストラクタ.
	 * 
	 * @param shell シェル
	 */
	public DownloadModule() {

		this.proxyTracker =
				new ServiceTracker<IProxyService, Object>(FrameworkUtil.getBundle(ProjectCreationWizard.class)
						.getBundleContext(), IProxyService.class, null);
		proxyTracker.open();

	}

	/**
	 * <H3>ファイルコンテンツ出力用ハンドラー.</H3>
	 * 
	 * @author fkubo
	 */
	static abstract class FileContentsHandler {

		abstract InputStream getInputStream() throws IOException;
	}

	/**
	 * 1つのファイルを保存する.
	 * 
	 * @param monitor モニター
	 * @param iFile ファイル
	 * @param fileContentsHandler ファイルコンテンツハンドラ
	 * @return 結果コード
	 * @throws IOException IO例外
	 * @throws URISyntaxException URI例外
	 * @throws CoreException コア例外
	 */
	private int updateFile(IProgressMonitor monitor, ResultStatus logger, IFile iFile,
			FileContentsHandler fileContentsHandler) throws IOException, URISyntaxException, CoreException {

		// PI0112=INFO,[{0}]を更新中...
		monitor.subTask(Messages.PI0112.format(iFile.getFullPath()));

		int ret = 0;
		InputStream is = null;
		try {
			if (iFile.exists()) {
				// 既に存在している.
				if (defaultOverwriteMode != 0) {
					ret = defaultOverwriteMode;
				} else {
					// TODO:メッセージ対応
					MessageDialog dialog =
							new MessageDialog(null, Messages.SE0113.format(),
									Dialog.getImage(Dialog.DLG_IMG_MESSAGE_INFO), Messages.SE0114.format(iFile
											.getRawLocation().toString()), MessageDialog.QUESTION, new String[] {
								UIMessages.Dialog_OVERWRITE, UIMessages.Dialog_ALL_OVERWRITE,
								UIMessages.Dialog_IGNORE, UIMessages.Dialog_ALL_IGNORE }, 0);
					ret = dialog.open();
				}
				switch (ret) {
					case 1:
						defaultOverwriteMode = ret;
					case 0:
						// 上書きする.
						logger.log(Messages.SE0097, iFile.getFullPath());
						is = fileContentsHandler.getInputStream();
						iFile.setContents(is, true, true, monitor);
						logger.log(Messages.SE0098, iFile.getFullPath());
						break;
					case 3:
						defaultOverwriteMode = ret;
					case 2:
						// 無視する.
						break;
				}
			} else {
				if (!iFile.getParent().exists()) {
					// フォルダの作成.
					H5IOUtils.createParentFolder(iFile.getParent(), monitor);
				}

				is = fileContentsHandler.getInputStream();
				logger.log(Messages.SE0091, iFile.getFullPath());
				iFile.create(is, true, monitor);
				logger.log(Messages.SE0092, iFile.getFullPath());
			}
		} finally {
			IOUtils.closeQuietly(is);
		}
		return ret;
	}

	/**
	 * 1つのファイルをダウンロードする.
	 * 
	 * @param monitor モニター
	 * @param file ファイル
	 * @param siteUrl URL
	 * @paramr perSiteWork ここで進ませるworked
	 * @throws IOException IO例外
	 * @throws URISyntaxException URI例外
	 * @throws CoreException コア例外
	 */
	private ZipFile
	download(IProgressMonitor monitor, ResultStatus logger, IFile file, String siteUrl, int perSiteWork)
			throws IOException, URISyntaxException, CoreException {

		// PI0111=INFO,[{0}]をダウンロード中...
		monitor.subTask(Messages.PI0111.format(siteUrl));

		lastDownloadStatus = false;
		URI uri = new URI(siteUrl);
		setProxy(uri);
		final URL url = uri.toURL();

		int ret = 0;
		while (ret == 0) {
			try {
				if (file != null) {
					// 通常のファイル生成.
					ret = updateFile(monitor, logger, file, new FileContentsHandler() {

						@Override
						InputStream getInputStream() throws IOException {

							return url.openStream();
						}
					});
					monitor.worked(perSiteWork);
					lastDownloadStatus = true;
				} else {
					// fileがnullの時は一時ファイルを作成し、ZipFileを返す仕様とする.

					InputStream is = url.openStream();
					OutputStream os = null;

					try {
						// サイズが取得で切れば取得する.
						int contentLength = url.openConnection().getContentLength();
						int perWork = perSiteWork;
						if (contentLength > 0) {
							perWork = Math.max(1, perSiteWork * DEFAULT_BUFFER_SIZE / contentLength);
						}

						// SE0093=INFO,{0}をダウンロードします。
						logger.log(Messages.SE0093, siteUrl);

						// ZIP対応.
						File tempFile = File.createTempFile(H5WizardPlugin.getId(), "tmp");
						// VM終了時に削除されるようにセット.
						tempFile.deleteOnExit();
						// 保存する.
						is = url.openStream();
						os = FileUtils.openOutputStream(tempFile);
						// IOUtils.copy(is, os);

						byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
						int n = 0;
						while (-1 != (n = is.read(buffer))) {
							os.write(buffer, 0, n);
							if (contentLength > 0) {
								monitor.worked(perWork);
							}
						}
						if (contentLength > 0) {
							monitor.worked(perSiteWork);
						}

						// SE0094=INFO,{0}をダウンロードしました。
						logger.log(Messages.SE0094, siteUrl);

						lastDownloadStatus = true;
						return new ZipFile(tempFile);
					} finally {
						IOUtils.closeQuietly(is);
						IOUtils.closeQuietly(os);
					}
				}
				ret = 1;
			} catch (IOException e) {
				// つながりません.
				MessageDialog dialog =
						new MessageDialog(null, Messages.SE0115.format(),
								Dialog.getImage(Dialog.DLG_IMG_MESSAGE_WARNING), Messages.SE0116.format(siteUrl),
								MessageDialog.QUESTION, new String[] { UIMessages.Dialog_RETRY,
							UIMessages.Dialog_IGNORE, UIMessages.Dialog_STOP }, 0);
				ret = dialog.open();
				if (ret == 2) {
					throw e;
				}
			}
		}
		return null;
	}

	/**
	 * プロジェクト構造をダウンロードする.
	 * 
	 * @param monitor モニター
	 * @param logger ロガー
	 * @param baseProject ベースプロジェクト
	 * @param proj 対象プロジェクト
	 */
	public void downloadProject(IProgressMonitor monitor, ResultStatus logger, BaseProject baseProject, IProject proj) {

		// 全てで400work.
		int perLibWork = 400;

		try {

			// SE0063=INFO,プロジェクト用ZIPファイルをダウンロードします。
			logger.log(Messages.SE0063);

			// 直接ファイルを展開するよう修正
			// 先に全上書きフラグは立てておく.
			defaultOverwriteMode = 1;

			final ZipFile zipFile = download(monitor, logger, null, baseProject.getUrl(), 100);
			perLibWork = perLibWork - 100;
			if (!lastDownloadStatus) {
				// ダウンロードエラー.
				logger.log(Messages.SE0068);
				logger.setSuccess(false);
			}

			// SE0064=INFO,プロジェクト用ZIPファイルをダウンロードしました。
			logger.log(Messages.SE0064);

			// 更新.
			//proj.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			//logger.log(Messages.SE0101);

			// プロジェクト構造の追加
			int perExtractWork = Math.max(1, perLibWork / zipFile.size());
			for (Enumeration<? extends ZipEntry> enumeration = zipFile.entries(); enumeration.hasMoreElements();) {
				final ZipEntry zipEntry = enumeration.nextElement();

				// PI0113=INFO,[{0}]を展開中...
				monitor.subTask(Messages.PI0113.format(zipEntry.getName()));

				if (zipEntry.isDirectory()) {
					// フォルダ
					IFolder iFolder = proj.getFolder(zipEntry.getName());
					if (!iFolder.exists()) {
						logger.log(Messages.SE0091, iFolder.getFullPath());
						H5IOUtils.createParentFolder(iFolder, monitor);
						logger.log(Messages.SE0092, iFolder.getFullPath());
					}
				} else {
					// ファイル
					IFile iFile = proj.getFile(zipEntry.getName());

					// ファイル保存.
					updateFile(monitor, logger, iFile, new FileContentsHandler() {

						@Override
						InputStream getInputStream() throws IOException {

							return zipFile.getInputStream(zipEntry);
						}
					});
				}
				monitor.worked(perExtractWork);
			}

			// // エラー ZIPに含まれていない.
			// H5StringUtils.log(getShell(), null, Messages.SE0043,
			// Messages.SE0045.format(site.getFile()));

			// 元に戻す.
			defaultOverwriteMode = 0;

			logger.log(Messages.SE0070);

		} catch (URISyntaxException e) {
			// SE0023=ERROR,予期しない例外が発生しました。
			logger.log(e, Messages.SE0023);

			H5LogUtils.putLog(e, Messages.SE0044);
		} catch (CoreException e) {
			// SE0023=ERROR,予期しない例外が発生しました。
			logger.log(e, Messages.SE0023);

			H5LogUtils.putLog(e, Messages.SE0044);
		} catch (IOException e) {
			// SE0023=ERROR,予期しない例外が発生しました。
			logger.log(e, Messages.SE0023);

			H5LogUtils.putLog(e, Messages.SE0044);
		}
	}

	/**
	 * ライブラリをダウンロードする.
	 * 
	 * @param monitor モニター
	 * @param logger ロガー
	 * @param selectedLibrarySet 選択済ライブラリ
	 * @param projectRoot プロジェクトルートフォルダ
	 * @return 処理が成功したかどうか
	 */
	public boolean downloadLibrary(IProgressMonitor monitor, ResultStatus logger, Set<LibraryNode> selectedLibrarySet,
			IContainer projectRoot) {

		if (H5WizardPlugin.getInstance().getSelectedLibrarySet().size() == 0) {
			return true;
		}

		// 全てで900work.
		int perLibWork = Math.max(1, 900 / H5WizardPlugin.getInstance().getSelectedLibrarySet().size());

		// SE0071=INFO,ライブラリ更新処理を開始します。
		logger.log(Messages.SE0071);

		// タスクを開始.
		// PI0102=INFO,ライブラリをダウンロード中...
		// monitor.beginTask(Messages.PI0102.format(), H5WizardPlugin.getInstance().getSelectedLibrarySet()
		// .size()); // ライブラリの数分登録.

		defaultOverwriteMode = 0;
		for (LibraryNode libraryNode : selectedLibrarySet) {
			if (libraryNode.isSelected()) {
				try {
					// 追加処理.
					CategoryNode categoryNode = libraryNode.getParent();
					Category category = categoryNode.getValue();

					// PI0114=INFO,ライブラリ[{0}{1}]を更新中...
					monitor.subTask(Messages.PI0114.format(category.getName(), libraryNode.getLabel()));

					// SE0073=INFO,ライブラリ{0}{1}の更新処理を開始します。
					logger.log(Messages.SE0073, category.getName(), libraryNode.getLabel());

					// 処理フォルダ.
					IContainer folder = categoryNode.getInstallFullPath(); // projectRoot.getFolder();

					if (libraryNode.getState() == LibraryState.REMOVE) {
						// 削除処理.

						for (String fileName : libraryNode.getFileList()) {

							IFile iFile = folder.getFile(Path.fromOSString(fileName));

							if (iFile.exists()) {
								// 他で利用されているかどうかチェック.
								if (isOtherLibraryResources(libraryNode, folder, fileName)) {
									logger.log(Messages.SE0102, iFile.getFullPath());
								} else {
									// 削除.
									logger.log(Messages.SE0095, iFile.getFullPath());
									iFile.delete(true, true, monitor);
									logger.log(Messages.SE0096, iFile.getFullPath());

									// フォルダが空なら削除.
									IContainer parentFolder = iFile.getParent();
									while (parentFolder.exists() && parentFolder.members().length == 0) {
										// 削除.
										logger.log(Messages.SE0095, parentFolder.getFullPath());
										((IFolder) parentFolder).delete(true, true, monitor);
										logger.log(Messages.SE0096, parentFolder.getFullPath());
										parentFolder = parentFolder.getParent();
									}
								}
							}
						}
						monitor.worked(perLibWork);
					} else if (libraryNode.isAddable()) {
						// 追加処理.
						if (!downloadZip(libraryNode, perLibWork, folder, monitor, logger)) {
							logger.setSuccess(false);
						}
					}

					// SE0074=INFO,ライブラリ{0}{1}の更新処理が完了しました。
					logger.log(Messages.SE0074, category.getName(), libraryNode.getLabel());

					// SE0072=INFO,ライブラリ更新処理が完了しました。
					logger.log(Messages.SE0072);

				} catch (Exception e) { // URISyntaxException, CoreException. IOException
					// SE0023=ERROR,予期しない例外が発生しました。
					logger.log(e, Messages.SE0023);

					H5LogUtils.putLog(e, Messages.SE0044);
				}
			}
		}
		return logger.isSuccess();
	}

	private boolean downloadZip(LibraryNode libraryNode, int perLibWork, IContainer folder, IProgressMonitor monitor,
			ResultStatus logger) throws IOException, URISyntaxException, CoreException {

		boolean result = true;
		boolean addStatus = false;
		ZipFile cachedZipFile = null;
		String cachedSite = null;
		Library library = libraryNode.getValue();

		if (!library.getSite().isEmpty()) {
			int perSiteWork = Math.max(1, perLibWork / library.getSite().size());

			for (Site site : library.getSite()) {
				String siteUrl = null;
				try {
					siteUrl = new URL(site.getUrl()).getPath();
				} catch (MalformedURLException e) {
					// エラー.
					logger.log(e, Messages.SE0082, site.getUrl());
				}
				if (siteUrl == null) {
					continue;
				}

				boolean setWorked = false;

				IContainer savedFolder = folder;
				if (site.getExtractPath() != null) {
					savedFolder = savedFolder.getFolder(Path.fromOSString(site.getExtractPath()));
				}

				// ファイルのダウンロード.
				IFile iFile = null;
				if (siteUrl.endsWith(".zip") || siteUrl.endsWith(".jar") || site.getFilePattern() != null) {

					// Zipダウンロード

					// 同じファイルはそのまま使う.
					if (!siteUrl.equals(cachedSite)) {
						cachedZipFile = download(monitor, logger, null, site.getUrl(), perSiteWork);
						setWorked = true;
						if (!lastDownloadStatus || cachedZipFile == null) {
							libraryNode.setState(LibraryState.DOWNLOAD_ERROR);
							result = false;
							break;
						}
						cachedSite = siteUrl;
					}

					final ZipFile zipFile = cachedZipFile;

					// Zip展開.
					for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
						final ZipEntry zipEntry = e.nextElement();
						if (site.getFilePattern() == null
								|| FilenameUtils.wildcardMatch(zipEntry.getName(), site.getFilePattern())) {
							// 一致.

							IContainer savedFolder2 = savedFolder;
							String wildCardStr = site.getFilePattern();
							if (wildCardStr.contains("*") && wildCardStr.contains("/")) {
								// 直前のフォルダまで取得する.
								wildCardStr = StringUtils.substringBeforeLast(site.getFilePattern(), "/");
							}

							String entryName = zipEntry.getName();
							if (entryName.startsWith(wildCardStr + "/")) {
								entryName = entryName.substring(wildCardStr.length() + 1);
							}

							if (zipEntry.isDirectory()) {
								// zipディレクトリは作成するだけ.
								if (StringUtils.isNotEmpty(entryName)) {
									// フォルダ作成.
									savedFolder2 = savedFolder2.getFolder(Path.fromOSString(entryName));
									if (libraryNode.isAddable() && !savedFolder2.exists()) {
										logger.log(Messages.SE0091, savedFolder2.getFullPath());
										H5IOUtils.createParentFolder(savedFolder2, monitor);
										logger.log(Messages.SE0092, savedFolder2.getFullPath());
									} else if (libraryNode.getState() == LibraryState.REMOVE && savedFolder2.exists()) {
										// 削除.
										logger.log(Messages.SE0095, savedFolder2.getFullPath());
										H5IOUtils.createParentFolder(savedFolder2, monitor);
										logger.log(Messages.SE0096, savedFolder2.getFullPath());
									}
								}
							} else {
								// zip以外ファイル追加.

								// ファイル名の変更.
								if (site.getReplaceFileName() != null) {
									iFile = savedFolder2.getFile(Path.fromOSString(site.getReplaceFileName()));
								} else {
									iFile = savedFolder2.getFile(Path.fromOSString(entryName));
								}

								// ファイル保存.
								updateFile(monitor, logger, iFile, new FileContentsHandler() {

									@Override
									InputStream getInputStream() throws IOException {

										return zipFile.getInputStream(zipEntry);
									}
								});
								addStatus = true;
							}

						} else {
							// 不一致.
							// System.out.println(zipEntry.getName() + "はマッチしないファイル");
						}
					}
					if (savedFolder.exists() && savedFolder.members().length == 0) {
						// 空のフォルダの場合は削除.
						savedFolder.delete(true, monitor);
					}
				} else {
					// ファイル名の変更.
					if (site.getReplaceFileName() != null) {
						iFile = savedFolder.getFile(Path.fromOSString(site.getReplaceFileName()));
					} else {
						// ファイル部分.
						iFile = savedFolder.getFile(Path.fromOSString(StringUtils.substringAfterLast(siteUrl, "/")));
					}

					// 追加.
					download(monitor, logger, iFile, site.getUrl(), perSiteWork);
					setWorked = true;
					if (!lastDownloadStatus) {

						// SE0101=ERROR,リソース({0})のダウンロードに失敗しました。URL={1}, File={2}
						logger.log(
								Messages.SE0101,
								iFile != null ? iFile.getFullPath().toString() : StringUtils.defaultString(site
										.getFilePattern()), site.getUrl(), site.getFilePattern());
						libraryNode.setState(LibraryState.DOWNLOAD_ERROR);
					} else {
						addStatus = true;
					}
				}

				// ファイルのダウンロードここまで.

				// 更新.
				if (!addStatus) {
					// SE0099=ERROR,ファイルの作成に失敗しました。URL={1}, File={2}
					logger.log(Messages.SE0099, site.getUrl(), iFile != null ? iFile.getFullPath().toString()
							: StringUtils.defaultString(site.getFilePattern()));
					libraryNode.setState(LibraryState.EXTRACT_ERROR);
					result = false;
				}

				// folder.refreshLocal(IResource.DEPTH_ZERO, null);
				// // SE0102=INFO,ワークスペースを更新しました。
				// logger.log(Messages.SE0102);
				// logger.log(Messages.SE0068, iFile.getFullPath());

				if (setWorked) {
					monitor.worked(perLibWork);
				}
			}
		}

		return result;
	}


	/**
	 * 他で利用しているリソースかどうか.
	 * 
	 * @param targetLibraryNode ライブラリノード.
	 * @param folder フォルダ
	 * @param targetFileName ターゲット名
	 * @return 他で利用しているリソースかどうか.
	 */
	private boolean isOtherLibraryResources(LibraryNode targetLibraryNode, IContainer folder, String targetFileName) {

		for (TreeNode node : targetLibraryNode.getParent().getParent().getChildren()){
			CategoryNode categoryNode = (CategoryNode)node;
			if (folder.equals(categoryNode.getInstallFullPath())){
				for (TreeNode node2 : categoryNode.getChildren()){
					LibraryNode libraryNode = (LibraryNode)node2;
					if (libraryNode != targetLibraryNode && libraryNode.isExists()) {
						for (String fileName: libraryNode.getFileList()){
							if (targetFileName.equals(fileName)){
								return true;
							}
						}
					}
				}

			}
		}
		return false;
	}

	/**
	 * プロキシサービスを取得する.
	 * 
	 * @return プロキシサービス
	 */
	private IProxyService getProxyService() {

		return (IProxyService) proxyTracker.getService();
	}

	/**
	 * URIに応じたプロキシを設定する.
	 * 
	 * @param uri URI
	 */
	public void setProxy(URI uri) {

		// プロキシ設定.
		IProxyService proxyService = getProxyService();
		IProxyData[] proxyDataForHost = proxyService.select(uri);

		for (IProxyData data : proxyDataForHost) {
			if (data.getHost() != null) {
				System.setProperty("http.proxySet", "true");
				System.setProperty("http.proxyHost", data.getHost());
				System.setProperty("http.proxyPort", String.valueOf(data.getPort()));
			} else {
				System.setProperty("http.proxySet", "false");
			}
			if (data.getUserId() != null) {
				System.setProperty("http.proxyUser", data.getUserId());
			}
			if (data.getPassword() != null) {
				System.setProperty("http.proxyPassword", data.getPassword());
			}
		}
	}
}
