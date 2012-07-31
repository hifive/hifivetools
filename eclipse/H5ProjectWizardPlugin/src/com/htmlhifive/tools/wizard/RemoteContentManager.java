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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import com.htmlhifive.tools.wizard.library.LibraryFileParser;
import com.htmlhifive.tools.wizard.library.LibraryFileParserFactory;
import com.htmlhifive.tools.wizard.library.ParseException;
import com.htmlhifive.tools.wizard.library.model.LibraryList;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.utils.H5LogUtils;

/**
 * <H3>リモートコンテンツマネージャー.</H3>
 * 
 * @author fkubo
 */
public abstract class RemoteContentManager {

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

		IStatus status = null;

		// メインのサイトを読み込む.
		InputStream is = null;
		try {
			is = new URL(PluginConstant.URL_LIBRARY_LIST).openStream();
			LibraryFileParser parser = LibraryFileParserFactory.createParser(is, "xml");

			LibraryList libraryList = parser.getLibraryList();
			H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
			return libraryList;
		} catch (ParseException ex) {
			status = H5LogUtils.putLog(ex, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST);
		} catch (MalformedURLException ex) {
			status = H5LogUtils.putLog(ex, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST);
		} catch (IOException ex) {
			status = H5LogUtils.putLog(ex, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST);
		} finally {
			IOUtils.closeQuietly(is);
		}

		// ミラーのサイトを読み込む.
		is = null;
		try {
			is = new URL(PluginConstant.URL_LIBRARY_LIST_MIRROR).openStream();
			LibraryFileParser parser = LibraryFileParserFactory.createParser(is, "xml");

			LibraryList libraryList = parser.getLibraryList();
			H5WizardPlugin.getInstance().setLibraryList(libraryList); // キャッシュさせておく.
			return libraryList;
		} catch (ParseException ex) {
			H5LogUtils.putLog(ex, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST_MIRROR);
		} catch (MalformedURLException ex) {
			H5LogUtils.putLog(ex, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST_MIRROR);
		} catch (IOException ex) {
			H5LogUtils.putLog(ex, Messages.SE0046, PluginConstant.URL_LIBRARY_LIST_MIRROR);
		} finally {
			IOUtils.closeQuietly(is);
		}

		if (status != null) {
			// メインのサイトのエラーをダイアログに表示する.
			H5LogUtils.showLog(Messages.SE0041, status);
		}

		return null;
	}

}
