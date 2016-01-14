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
 *
 */
package com.htmlhifive.tools.jslint.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * eclipseのリソースに関するユーティルクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class PluginResourceUtils {
	/**
	 * コンストラクタ.
	 */
	private PluginResourceUtils() {

		// no create
	}

	/**
	 * 
	 * ワークスペース内の指定されたコンテナを取得する.
	 * 
	 * @param path パス
	 * @return コンテナ.
	 */
	public static IContainer pathToContainer(String path) {

		IContainer container;
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (null != path && 0 != path.trim().length()) {
			final IPath containerPath = new Path(path);
			if (1 == containerPath.segmentCount()) {
				// プロジェクト.
				container = root.getProject(containerPath.toFile().getName());

			} else {
				// フォルダ.
				container = root.getFolder(containerPath);
			}
		} else {
			container = root.getProject();
		}
		return container;

	}

	/**
	 * パスがワークスペース内に存在するかチェック.
	 * 
	 * @param path チェックするパス.
	 * @return ファイルがワークスペースに存在すればtrue、しなければfalse
	 */
	public static boolean isExistFile(String path) {

		return ResourcesPlugin.getWorkspace().getRoot().exists(new Path(path));
	}

	/**
	 * 指定したパスのファイルの中身を取得する
	 * 
	 * @param filePath ファイルパス
	 * @return ファイルの中身の文字列
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	public static String getFileContent(String filePath)
			throws IOException, SecurityException, IllegalArgumentException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		return getFileContent(filePath, BufferedReader.class);
	}

	/**
	 * 指定したパスのファイルの中身を取得する
	 * 
	 * @param filePath ファイルパス
	 * @param readerClass ファイル読み込みクラス
	 * @return ファイルの中身の文字列
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	public static String getFileContent(String filePath, Class<? extends BufferedReader> readerClass)
			throws IOException, SecurityException, NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		InputStream fileStream = PluginResourceUtils.class.getClassLoader().getResourceAsStream(filePath);

		Constructor<? extends BufferedReader> constructor = readerClass.getConstructor(Reader.class);
		BufferedReader reader = constructor.newInstance(new InputStreamReader(fileStream));

		StringBuilder builder = new StringBuilder();
		String line;
		final String separator = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(separator);
		}
		return builder.toString();
	}

}
