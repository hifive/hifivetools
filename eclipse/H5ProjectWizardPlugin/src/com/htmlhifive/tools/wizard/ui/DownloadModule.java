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
import java.net.URI;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeNode;
import org.osgi.util.tracker.ServiceTracker;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.PluginConstant;
import com.htmlhifive.tools.wizard.library.model.LibraryState;
import com.htmlhifive.tools.wizard.library.model.xml.BaseProject;
import com.htmlhifive.tools.wizard.library.model.xml.Category;
import com.htmlhifive.tools.wizard.library.model.xml.Library;
import com.htmlhifive.tools.wizard.library.model.xml.Site;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.page.tree.CategoryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.RootNode;
import com.htmlhifive.tools.wizard.utils.H5IOUtils;

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

		this.proxyTracker = new ServiceTracker<IProxyService, Object>(H5WizardPlugin.getInstance().getBundle()
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
	 * closeする.
	 */
	public void close() {
		proxyTracker.close();
	}

	/**
	 * 1つのファイルを保存する.
	 * 
	 * @param monitor モニター
	 * @param iFile ファイル
	 * @param fileContentsHandler ファイルコンテンツハンドラ
	 * @throws IOException IO例外
	 * @return 結果
	 */
	private boolean updateFile(IProgressMonitor monitor, ResultStatus logger, IFile iFile,
			FileContentsHandler fileContentsHandler) throws IOException {

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
					MessageDialog dialog = new MessageDialog(null, Messages.SE0113.format(),
							Dialog.getImage(Dialog.DLG_IMG_MESSAGE_INFO), Messages.SE0114.format(iFile.getRawLocation()
									.toString()), MessageDialog.QUESTION, new String[] { UIMessages.Dialog_OVERWRITE,
						UIMessages.Dialog_ALL_OVERWRITE, UIMessages.Dialog_IGNORE,
						UIMessages.Dialog_ALL_IGNORE }, 0);
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
			return true;
		} catch (CoreException e) {
			// SE0024=ERROR,({0})を操作中に入出力例外が発生しました。
			logger.log(e, Messages.SE0024, iFile.getFullPath().toString());
		} finally {
			IOUtils.closeQuietly(is);
		}
		return false;
	}

	/**
	 * 1つのファイルをダウンロードする.
	 * 
	 * @param monitor モニター
	 * @param file ファイル
	 * @param uri URI
	 * @param perSiteWork ここで進ませるworked
	 * @throws CoreException コア例外
	 */
	private ZipFile download(IProgressMonitor monitor, ResultStatus logger, IFile file, final String urlStr,
			int perSiteWork) throws CoreException {

		// PI0111=INFO,[{0}]をダウンロード中...
		monitor.subTask(Messages.PI0111.format(urlStr));

		lastDownloadStatus = false;

		int ret = 0;
		while (ret == 0) {
			try {
				if (file != null) {
					// 通常のファイル生成.
					boolean updateResult = updateFile(monitor, logger, file, new FileContentsHandler() {

						@Override
						InputStream getInputStream() throws IOException {

							if (H5IOUtils.isClassResources(urlStr)) {
								// クラスパスから取得する.
								return DownloadModule.class.getResourceAsStream(urlStr);
							}

							return DownloadModule.this.connectAsStream(urlStr,
									PluginConstant.URL_LIBRARY_CONNECTION_TIMEOUT);
						}
					});
					if (updateResult) {
						lastDownloadStatus = true;
					}
					monitor.worked(perSiteWork);
				} else {
					// fileがnullの時は一時ファイルを作成し、ZipFileを返す仕様とする.

					InputStream is = null;
					OutputStream os = null;

					try {
						int contentLength = 0;
						int perWork = perSiteWork;
						if (H5IOUtils.isClassResources(urlStr)) {
							// urlがnullの時は、クラスパスから取得する.
							is = DownloadModule.class.getResourceAsStream(urlStr);
						} else {
							// 通常のURL
							HttpMethod method = DownloadModule.this.connect(urlStr,
									PluginConstant.URL_LIBRARY_CONNECTION_TIMEOUT);
							if (method == null) {
								return null;
							}

							// サイズが取得で切れば取得する.
							Header header = method.getResponseHeader("Content-Length");
							if (header != null) {
								contentLength = Integer.valueOf(header.getValue());
							}
							if (contentLength > 0) {
								perWork = Math.max(1, perSiteWork * DEFAULT_BUFFER_SIZE / contentLength);
							}
							is = method.getResponseBodyAsStream();
						}
						if (is == null) {
							return null;
						}

						// SE0093=INFO,{0}をダウンロードします。
						logger.log(Messages.SE0093, urlStr);

						// ZIP対応.
						File tempFile = File.createTempFile(H5WizardPlugin.getId(), "tmp");
						// VM終了時に削除されるようにセット.
						tempFile.deleteOnExit();
						// 保存する.
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
						if (contentLength == 0) {
							monitor.worked(perSiteWork);
						}

						// SE0094=INFO,{0}をダウンロードしました。
						logger.log(Messages.SE0094, urlStr);

						lastDownloadStatus = true;
						return new ZipFile(tempFile);
					} finally {
						IOUtils.closeQuietly(is);
						IOUtils.closeQuietly(os);
					}
				}
				ret = 1;
			} catch (IOException e) {
				// SE0101=ERROR,リソース({0})のダウンロードに失敗しました。URL={1}, File={2}
				logger.log(e, Messages.SE0101, urlStr, file != null ? file.toString() : "");

				// つながりません.
				MessageDialog dialog = new MessageDialog(null, Messages.SE0115.format(),
						Dialog.getImage(Dialog.DLG_IMG_MESSAGE_WARNING), Messages.SE0116.format(urlStr),
						MessageDialog.QUESTION, new String[] { UIMessages.Dialog_RETRY, UIMessages.Dialog_IGNORE,
					UIMessages.Dialog_STOP }, 0);
				ret = dialog.open();
				if (ret == 2) {
					// 中断
					throw new OperationCanceledException(Messages.SE0101.format(urlStr, file != null ? file.toString()
							: ""));
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
	 * @throws CoreException コア例外
	 */
	public void downloadProject(IProgressMonitor monitor, ResultStatus logger, BaseProject baseProject, IProject proj)
			throws CoreException {

		// 全てで400work.
		int perLibWork = 400;

		// SE0063=INFO,プロジェクト用ZIPファイルをダウンロードします。
		logger.log(Messages.SE0063);

		// 直接ファイルを展開するよう修正
		// 先に全上書きフラグは立てておく.
		defaultOverwriteMode = 1;

		String siteUrl = baseProject.getUrl();
		String path = H5IOUtils.getURLPath(siteUrl);
		if (path == null) {
			logger.log(Messages.SE0082, baseProject.getUrl());
			logger.setSuccess(false);
			throw new CoreException(new Status(IStatus.ERROR, H5WizardPlugin.getId(),
					Messages.SE0082.format(baseProject.getUrl())));
		}

		final ZipFile zipFile;
		//try {
		//URI uri = new URI(baseProject.getUrl());
		zipFile = download(monitor, logger, null, siteUrl, 100);
		//} catch (URISyntaxException e) {
		//throw new CoreException(new Status(IStatus.ERROR, H5WizardPlugin.getId(), Messages.SE0013.format(), e));
		//}

		perLibWork = perLibWork - 100;
		if (!lastDownloadStatus) {
			// ダウンロードエラー.
			logger.log(Messages.SE0068);
			logger.setSuccess(false);
			throw new CoreException(new Status(IStatus.ERROR, H5WizardPlugin.getId(), Messages.SE0068.format()));
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
				try {
					updateFile(monitor, logger, iFile, new FileContentsHandler() {

						@Override
						InputStream getInputStream() throws IOException {

							return zipFile.getInputStream(zipEntry);
						}
					});
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, H5WizardPlugin.getId(), Messages.SE0068.format(),
							e));
				}
			}
			monitor.worked(perExtractWork);
		}

		// // エラー ZIPに含まれていない.
		// H5StringUtils.log(getShell(), null, Messages.SE0043,
		// Messages.SE0045.format(site.getFile()));

		// 元に戻す.
		defaultOverwriteMode = 0;

		logger.log(Messages.SE0070);
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
							// 1つ以上ダウンロードに失敗した場合.
							logger.setSuccess(false);
						}
					}

					// SE0074=INFO,ライブラリ{0}{1}の更新処理が完了しました。
					logger.log(Messages.SE0074, category.getName(), libraryNode.getLabel());

				} catch (IOException e) { // CoreException. IOException
					// SE0023=ERROR,予期しない例外が発生しました。
					logger.log(e, Messages.SE0023);
				} catch (CoreException e) { // CoreException. IOException
					// SE0023=ERROR,予期しない例外が発生しました。
					logger.log(e, Messages.SE0023);
				}
			}
		}
		return logger.isSuccess();
	}

	/**
	 * ZIPファイルをダウンロードする.
	 * 
	 * @param libraryNode ライブラリノード
	 * @param perLibWork libWork
	 * @param folder フォルダ
	 * @param monitor モニタ
	 * @param logger ロガー.
	 * @return 成功したかどうか.
	 * @throws IOException IO例外
	 * @throws CoreException コア例外
	 */
	private boolean downloadZip(LibraryNode libraryNode, int perLibWork, IContainer folder, IProgressMonitor monitor,
			ResultStatus logger) throws IOException, CoreException {

		boolean result = true;
		boolean addStatus = false;
		ZipFile cachedZipFile = null;
		String cachedSite = null;
		Library library = libraryNode.getValue();

		if (!library.getSite().isEmpty()) {
			int perSiteWork = Math.max(1, perLibWork / library.getSite().size());

			for (Site site : library.getSite()) {
				String siteUrl = site.getUrl();
				String path = H5IOUtils.getURLPath(siteUrl);
				if (path == null) {
					logger.log(Messages.SE0082, siteUrl);
					continue;
				}

				boolean setWorked = false;

				IContainer savedFolder = folder;
				if (site.getExtractPath() != null) {
					savedFolder = savedFolder.getFolder(Path.fromOSString(site.getExtractPath()));
				}

				// ファイルのダウンロード.
				IFile iFile = null;
				if (path.endsWith(".zip") || path.endsWith(".jar") || site.getFilePattern() != null) {

					// Zipダウンロード

					// 同じファイルはそのまま使う.
					if (!siteUrl.equals(cachedSite)) {
						cachedZipFile = download(monitor, logger, null, siteUrl, perSiteWork);
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
							String wildCardStr = StringUtils.defaultString(site.getFilePattern());
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
						iFile = savedFolder.getFile(Path.fromOSString(StringUtils.substringAfterLast(path, "/")));
					}

					// 追加.
					download(monitor, logger, iFile, siteUrl, perSiteWork);
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

		RootNode rootNode = targetLibraryNode.getParent().getParent();
		if (rootNode.getChildren() == null) {
			return false;
		}
		for (TreeNode node : rootNode.getChildren()) {
			CategoryNode categoryNode = (CategoryNode) node;
			if (folder.equals(categoryNode.getInstallFullPath())) {
				for (TreeNode node2 : categoryNode.getChildren()) {
					LibraryNode libraryNode = (LibraryNode) node2;
					if (libraryNode != targetLibraryNode && libraryNode.isExists()) {
						for (String fileName : libraryNode.getFileList()) {
							if (targetFileName.equals(fileName)) {
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
	 * @param urlStr urlStr
	 * @param client client
	 */
	public void setProxy(String urlStr, HttpClient client) {

		// プロキシ設定.
		IProxyService proxyService = getProxyService();
		IProxyData[] proxyDataForHost = proxyService.select(URI.create(urlStr));
		for (IProxyData data : proxyDataForHost) {
			if (data.getHost() != null) {
				client.getHostConfiguration().setProxy(data.getHost(), data.getPort());

				if (StringUtils.isNotEmpty(data.getUserId())) {
					client.getState().setProxyCredentials(new AuthScope(data.getHost(), data.getPort(), "relm"),
							new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));

				}
			}
		}
	}

	/**
	 * HttpMethod を取得する.
	 * 
	 * @param urlStr urlStr
	 * @param connectionTimeout connectionTimeout
	 * @return HttpMethod
	 * @throws IOException IO例外
	 */
	public HttpMethod connect(String urlStr, int connectionTimeout) throws IOException {

		HttpClient client = new HttpClient();
		HttpMethod getMethod = new GetMethod(urlStr);

		setProxy(urlStr, client);

		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, true));
		client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
				PluginConstant.URL_LIBRARY_LIST_CONNECTION_TIMEOUT);

		int result = client.executeMethod(getMethod);
		if (result != HttpStatus.SC_OK) {
			return null;
		}
		//Header header = getMethod.getResponseHeader("Content-Length");
		//int content = Integer.valueOf(header.getValue());
		return getMethod;
	}

	/**
	 * InputStream を取得する.
	 * 
	 * @param urlStr urlStr
	 * @param connectionTimeout connectionTimeout
	 * @return InputStream
	 * @throws IOException IO例外
	 */
	public InputStream connectAsStream(String urlStr, int connectionTimeout) throws IOException {

		HttpMethod method = connect(urlStr, connectionTimeout);

		if (method != null) {
			return method.getResponseBodyAsStream();
		}
		return null;
	}
}
