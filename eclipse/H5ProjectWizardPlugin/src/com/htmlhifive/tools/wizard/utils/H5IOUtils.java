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
package com.htmlhifive.tools.wizard.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;

import com.htmlhifive.tools.wizard.log.messages.Messages;

/**
 * <H3>Eclipseウィザード用ユーティリティ.</H3>
 * 
 * @author fkubo
 */
public abstract class H5IOUtils {

	//
	// /** バッファ. */
	// private static final int BUFFER = 4096;

	/** PROJECT_NAME. */
	private static final String PROJECT_NAME = "PROJECT_NAME";

	/**
	 * 再帰的に親フォルダを生成する.
	 * 
	 * @param parent 親コンテナ
	 * @param monitor モニター
	 * @throws CoreException コア例外
	 */
	public static void createParentFolder(IContainer parent, IProgressMonitor monitor) throws CoreException {

		if (!ResourcesPlugin.getWorkspace().getRoot().exists(parent.getFullPath())) {
			createParentFolder(parent.getParent(), monitor);
			((IFolder) parent).create(true, true, monitor);
			((IFolder) parent).refreshLocal(IResource.DEPTH_ZERO, monitor);
		}
	}

	//
	// /**
	// * 一時的なzipファイルの作成する.
	// *
	// * @param shell シェル
	// * @param is 入力ストリーム
	// * @return zipファイルのインスタンス
	// */
	// public static File createTemporaryZipFile(Shell shell, InputStream is) {
	//
	// File outputFile = null;
	// BufferedInputStream input = null;
	// BufferedOutputStream output = null;
	// try {
	// input = new BufferedInputStream(is);
	// outputFile = File.createTempFile("structure", ".zip");
	// // VM終了時に削除されるようにセット.
	// outputFile.deleteOnExit();
	// output = new BufferedOutputStream(new FileOutputStream(outputFile));
	// final byte[] buf = new byte[BUFFER];
	// int len;
	// while ((len = input.read(buf)) != -1) {
	// output.write(buf, 0, len);
	// }
	// output.flush();
	// } catch (IOException e) {
	// log(shell, e, Messages.SE0022.format(), Messages.SE0024);
	// } finally {
	// IOUtils.closeQuietly(is);
	// IOUtils.closeQuietly(input);
	// IOUtils.closeQuietly(output);
	// }
	// return outputFile;
	// }

	/**
	 * 設定ファイルを書き換える. ${PROJECT_NAME}とある部分をプロジェクト名に変換する.
	 * 
	 * @param shell シェル
	 * @param projectHandle プロジェクトハンドル
	 * @param fileName ファイル名
	 */
	public static void convertProjectName(Shell shell, IProject projectHandle, String fileName) {

		convertName(shell, projectHandle, fileName, new String[] { PROJECT_NAME },
				new String[] { projectHandle.getName() });
	}

	/**
	 * 設定ファイルを書き換える. ${PROJECT_NAME}とある部分をプロジェクト名に変換する.
	 * 
	 * @param shell シェル
	 * @param projectHandle プロジェクトハンドル
	 * @param fileName ファイル名
	 * @param varNames 変数名
	 */
	private static void convertName(Shell shell, IProject projectHandle, String fileName, String[] varNames,
			String[] varValues) {

		final IFile file = projectHandle.getFile(new Path(fileName));
		if (!file.isAccessible()) {
			return;
		}

		InputStream is = null;
		InputStream is2 = null;
		try {
			is = file.getContents();
			String text = IOUtils.toString(is, file.getCharset());
			for (int i = 0; i < varNames.length; i++) {
				text = StringUtils.replace(text, "${" + varNames[i] + "}", varValues[i]);
			}
			is2 = IOUtils.toInputStream(text, file.getCharset());
			file.setContents(is2, true, true, null);

		} catch (IOException e) {
			H5LogUtils.putLog(e, Messages.SE0024, fileName);
			//H5LogUtils.showLog(e, Messages.SE0022, Messages.SE0024, fileName);
		} catch (CoreException e) {
			H5LogUtils.putLog(e, Messages.SE0024, fileName);
			//H5LogUtils.showLog(e, Messages.SE0022, Messages.SE0024, fileName);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(is2);
		}
	}

	/**
	 * 正しいパスかをチェックする.
	 * 
	 * @param path
	 * @return パスが正しいかどうか
	 */
	public static boolean isValidWorkspacePath(IPath path) {

		// パスチェックは、Workspace.newResourceより取得.
		return path.isValidPath(path.toString()) && path.segmentCount() >= ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH;
	}

	/**
	 * URLがクラスパスリソースを対象としているかどうかを取得する.
	 * 
	 * @param url URL
	 * @return URLがクラスパスリソースを対象としているかどうか
	 */
	public static boolean isClassResources(String url) {
		return url.startsWith("/");
	}

	/**
	 * クエリを除いたパスを返す.
	 * 
	 * @param siteUrl URL
	 * @return クエリを除いたパス
	 */
	public static String getURLPath(String siteUrl) {
		try {
			if (!H5IOUtils.isClassResources(siteUrl)) {
				// クエリを除く.
				return new URL(siteUrl).getPath();
			}
			return siteUrl;
		} catch (MalformedURLException e) {
		}
		return null;
	}
}