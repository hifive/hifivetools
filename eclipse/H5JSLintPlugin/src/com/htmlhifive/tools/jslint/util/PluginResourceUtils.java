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
package com.htmlhifive.tools.jslint.util;

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

}
