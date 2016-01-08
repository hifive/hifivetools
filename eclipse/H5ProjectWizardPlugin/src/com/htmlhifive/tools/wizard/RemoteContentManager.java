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
package com.htmlhifive.tools.wizard;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.htmlhifive.tools.wizard.download.ConnectMethodFactory;
import com.htmlhifive.tools.wizard.download.DownloadModule;
import com.htmlhifive.tools.wizard.download.IConnectMethod;
import com.htmlhifive.tools.wizard.library.LibraryList;
import com.htmlhifive.tools.wizard.library.parser.LibraryFileParser;
import com.htmlhifive.tools.wizard.library.parser.LibraryFileParserFactory;
import com.htmlhifive.tools.wizard.library.parser.ParseException;
import com.htmlhifive.tools.wizard.log.ResultStatus;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.utils.H5IOUtils;

/**
 * <H3>リモートコンテンツマネージャー.</H3>
 * 
 * @author fkubo
 */
public abstract class RemoteContentManager {

	private static final String LOCAL_LIBRARIES_XML = "/local-libraries.xml";

	/**
	 * ライブラリリスト.
	 * 
	 * @param shell シェル
	 * @return ライブラリリスト
	 * @throws CoreException コア例外
	 */
	public static LibraryList getLibraryList() {

		return getLibraryList(false);
	}

	/**
	 * ライブラリリスト.
	 * 
	 * @param refresh リフレッシュ
	 * @param shell シェル
	 * @return ライブラリリスト
	 */
	public static LibraryList getLibraryList(boolean refresh) {

		if (!refresh && H5WizardPlugin.getInstance().getLibraryList() != null) {
			return H5WizardPlugin.getInstance().getLibraryList();
		}

		// クリアしておく
		H5WizardPlugin.getInstance().setLibraryList(null);
		H5WizardPlugin.getInstance().getSelectedLibrarySet().clear();

		ResultStatus resultStatus = new ResultStatus();

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);

		// サイトを読み込む.
		for (String urlStr : new String[] { PluginConstant.URL_LIBRARY_LIST, PluginConstant.URL_LIBRARY_LIST_MIRROR,
				LOCAL_LIBRARIES_XML }) {
			if (StringUtils.isNotEmpty(urlStr)) {
				InputStream is = null;
				DownloadModule downloadModule = new DownloadModule();
				try {

					//					IConnectMethod method = ConnectMethodFactory.getMethod(urlStr, true);
					//					method.setConnectionTimeout(PluginConstant.URL_LIBRARY_LIST_CONNECTION_TIMEOUT);
					//					method.setProxy(downloadModule.getProxyService());
					//					is = method.getInputStream();
					//					if (is == null) {
					//						resultStatus.log(Messages.SE0046, urlStr);
					//					} else {
					//						LibraryFileParser parser = LibraryFileParserFactory.createParser(is);
					//						LibraryList libraryList = parser.getLibraryList();
					//						libraryList.setLastModified(method.getLastModified());
					//						libraryList.setSource(urlStr);
					//						H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
					//						return libraryList;
					//					}
					//				} catch (ParseException e) {
					//					resultStatus.log(e, Messages.SE0046, urlStr);
					//				} catch (MalformedURLException e) {
					//					resultStatus.log(e, Messages.SE0046, urlStr);
					//				} catch (IOException e) {
					//					resultStatus.log(e, Messages.SE0046, urlStr);

					//進捗表示の場合
					final IRunnableWithProgress runnable = getSiteDownLoad(resultStatus, urlStr, downloadModule);

					try {
						dialog.run(true, false, runnable);
						if (resultStatus.isSuccess()) {
							return H5WizardPlugin.getInstance().getLibraryList();
						}
					} catch (InvocationTargetException e) {
						resultStatus.log(e, Messages.SE0046, urlStr);
					} catch (InterruptedException e) {
						resultStatus.log(e, Messages.SE0046, urlStr);
					}
				} finally {
					downloadModule.close();
					IOUtils.closeQuietly(is);
				}
			}
		}

		//		// ローカルのリソースを利用する.
		//		InputStream is = null;
		//		try {
		//			URLConnection connection = RemoteContentManager.class.getResource(LOCAL_LIBRARIES_XML).openConnection();
		//			is = connection.getInputStream();
		//			if (is == null) {
		//				resultStatus.log(Messages.SE0046, LOCAL_LIBRARIES_XML);
		//			} else {
		//				LibraryFileParser parser = LibraryFileParserFactory.createParser(is);
		//				LibraryList libraryList = parser.getLibraryList();
		//				if (connection.getLastModified() > 0) {
		//					libraryList.setLastModified(new Date(connection.getLastModified()));
		//				}
		//				libraryList.setSource(null);
		//				H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
		//				return libraryList;
		//			}
		//		} catch (ParseException e) {
		//			resultStatus.log(e, Messages.SE0046, LOCAL_LIBRARIES_XML);
		//		} catch (IOException e) {
		//			resultStatus.log(e, Messages.SE0046, LOCAL_LIBRARIES_XML);
		//		} finally {
		//			IOUtils.closeQuietly(is);
		//		}

		if (!resultStatus.isSuccess()) {
			// エラーを表示する.
			resultStatus.falureDialog(Messages.PI0139, Messages.PI0140);
		}

		return null;
	}

	private static IRunnableWithProgress getSiteDownLoad(final ResultStatus resultStatus, final String urlStr,
			final DownloadModule downloadModule) {

		return new IRunnableWithProgress() {

			@Override
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				monitor.setTaskName(Messages.PI0141.format(urlStr));

				BufferedInputStream bufferIs = null;
				try {
					IConnectMethod method = ConnectMethodFactory.getMethod(urlStr, true);
					if (!H5IOUtils.isClassResources(urlStr)) {
						method.setConnectionTimeout(PluginConstant.URL_LIBRARY_LIST_CONNECTION_TIMEOUT);
						method.setProxy(downloadModule.getProxyService());
					}
					InputStream is = method.getInputStream();
					if (is == null) {
						resultStatus.log(Messages.SE0046, urlStr);
					} else {

						final int content = method.getContentLength();
						monitor.beginTask(Messages.PI0142.format(urlStr), content);

						bufferIs = new BufferedInputStream(is) {
							private int current = 0;

							@Override
							public synchronized int read() throws IOException {

								int result = super.read();
								current += result * 16;
								monitor.subTask(Messages.PI0143.format(current, content, urlStr));
								monitor.worked(result * 16);

								return result;
							}
						};

						LibraryFileParser parser = LibraryFileParserFactory.createParser(bufferIs);
						LibraryList libraryList = parser.getLibraryList();
						libraryList.setLastModified(method.getLastModified());
						if (H5IOUtils.isClassResources(urlStr)) {
							libraryList.setSource(null); // クラスパスリソース出ない時設定.
						} else {
							libraryList.setSource(urlStr);
						}
						H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
						resultStatus.setSuccess(true); // 途中で失敗してても成功.
						monitor.done();
					}
				} catch (ParseException e) {
					resultStatus.log(e, Messages.SE0046, urlStr);
				} catch (MalformedURLException e) {
					resultStatus.log(e, Messages.SE0046, urlStr);
				} catch (IOException e) {
					resultStatus.log(e, Messages.SE0046, urlStr);
				} finally {
					IOUtils.closeQuietly(bufferIs);
					//Thread.currentThread().wait(5000);
				}
			}
		};
	}
}
