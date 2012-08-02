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
package com.htmlhifive.tools.wizard;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;

import com.htmlhifive.tools.wizard.library.LibraryFileParser;
import com.htmlhifive.tools.wizard.library.LibraryFileParserFactory;
import com.htmlhifive.tools.wizard.library.ParseException;
import com.htmlhifive.tools.wizard.library.model.LibraryList;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.ResultStatus;

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

		// 選択もクリアしておく
		H5WizardPlugin.getInstance().getSelectedLibrarySet().clear();

		ResultStatus resultStatus = new ResultStatus();

		// サイトを読み込む.
		for (String url : new String[] { PluginConstant.URL_LIBRARY_LIST, PluginConstant.URL_LIBRARY_LIST_MIRROR }) {
			if (StringUtils.isNotEmpty(url)) {
				InputStream is = null;
				try {
					URLConnection connection = new URL(url).openConnection();
					if (connection instanceof HttpURLConnection) {
						HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
						httpURLConnection.setConnectTimeout(PluginConstant.URL_LIBRARY_LIST_CONNECTION_TIMEOUT);
					}
					is = connection.getInputStream();
					if (is == null) {
						resultStatus.log(Messages.SE0046, url);
					} else {
						LibraryFileParser parser = LibraryFileParserFactory.createParser(is);
						LibraryList libraryList = parser.getLibraryList();
						if (connection.getLastModified() > 0) {
							libraryList.setLastModified(new Date(connection.getLastModified()));
						}
						libraryList.setSource(url);
						H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
						return libraryList;
					}
				} catch (ParseException e) {
					resultStatus.log(e, Messages.SE0046, url);
				} catch (MalformedURLException e) {
					resultStatus.log(e, Messages.SE0046, url);
				} catch (IOException e) {
					resultStatus.log(e, Messages.SE0046, url);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		}

		// ローカルのリソースを利用する.
		InputStream is = null;
		try {
			URLConnection connection = RemoteContentManager.class.getResource(LOCAL_LIBRARIES_XML).openConnection();
			is = connection.getInputStream();
			if (is == null) {
				resultStatus.log(Messages.SE0046, LOCAL_LIBRARIES_XML);
			} else {
				LibraryFileParser parser = LibraryFileParserFactory.createParser(is);
				LibraryList libraryList = parser.getLibraryList();
				if (connection.getLastModified() > 0) {
					libraryList.setLastModified(new Date(connection.getLastModified()));
				}
				libraryList.setSource(null);
				H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
				return libraryList;
			}
		} catch (ParseException e) {
			resultStatus.log(e, Messages.SE0046, LOCAL_LIBRARIES_XML);
		} catch (IOException e) {
			resultStatus.log(e, Messages.SE0046, LOCAL_LIBRARIES_XML);
		} finally {
			IOUtils.closeQuietly(is);
		}

		if (!resultStatus.isSuccess()) {
			// エラーを表示する.
			resultStatus.showDialog(Messages.PI0154);
		}

		return null;
	}

	// 進捗表示の場合
	//	ResultStatus resultStatus = new ResultStatus();
	//	final IRunnableWithProgress runnable = getSiteDownLoad(resultStatus, PluginConstant.URL_LIBRARY_LIST);
	//	ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
	//	try {
	//		dialog.run(false, false, runnable);
	//	} catch (InvocationTargetException e) {
	//		e.printStackTrace();
	//	} catch (InterruptedException e) {
	//		e.printStackTrace();
	//	}
	//	if (resultStatus.isSuccess()) {
	//		return H5WizardPlugin.getInstance().getLibraryList();
	//	}
	//
	//	private static IRunnableWithProgress getSiteDownLoad(final ResultStatus resultStatus, final String url) {
	//		return new IRunnableWithProgress() {
	//
	//			@Override
	//			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	//
	//				if (monitor == null) {
	//					// モニタを生成.
	//					monitor = new NullProgressMonitor();
	//				}
	//				InputStream is = null;
	//				try {
	//					HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	//					connection.setConnectTimeout(PluginConstant.URL_LIBRARY_CONNECTION_TIMEOUT);
	//					is = connection.getInputStream();
	//					LibraryFileParser parser = LibraryFileParserFactory.createParser(is);
	//					LibraryList libraryList = parser.getLibraryList();
	//					if (connection.getLastModified() > 0) {
	//						libraryList.setLastModified(new Date(connection.getLastModified()));
	//					}
	//					libraryList.setSource("");
	//					H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
	//				} catch (ParseException e) {
	//					H5LogUtils.putLog(e, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST_MIRROR);
	//					resultStatus.log(e, Messages.SE0046);
	//				} catch (MalformedURLException e) {
	//					H5LogUtils.putLog(e, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST_MIRROR);
	//					resultStatus.log(e, Messages.SE0046);
	//				} catch (IOException e) {
	//					H5LogUtils.putLog(e, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST_MIRROR);
	//					resultStatus.log(e, Messages.SE0046);
	//				} finally {
	//					IOUtils.closeQuietly(is);
	//				}
	//			}
	//		};
	//	}

}
