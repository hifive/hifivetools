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
package com.htmlhifive.tools.jslint;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.logger.impl.JSLintPluginLoggerDefaultImpl;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * JSLintプラグインクラス.
 */
public class JSLintPlugin extends AbstractUIPlugin {

	/**
	 * プラグインID.
	 */
	public static final String PLUGIN_ID = "com.htmlhifive.tools.jslint.H5JSLintPlugin"; //$NON-NLS-1$

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JSLintPlugin.class);

	/**
	 * インスタンス.
	 */
	private static JSLintPlugin plugin;

	/**
	 * コンストラクタ.
	 */
	public JSLintPlugin() {

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {

		super.start(context);
		plugin = this;
		JSLintPluginLoggerFactory.setLogger(JSLintPluginLoggerDefaultImpl.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static JSLintPlugin getDefault() {

		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {

		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * プロジェクトがJSLintのネーチャーを保持しているかどうかを判定する.
	 * 
	 * @param project チェック対象プロジェクト.
	 * @return JSLintネーチャーを保持しているかどうか.
	 */
	public static boolean hasJSLintNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natureIds = description.getNatureIds();
			for (String string : natureIds) {
				if (StringUtils.equals(JSLintPluginNature.NATURE_ID, string)) {
					return true;
				}
			}
		} catch (CoreException e) {
			logger.put(Messages.EM0011, project.getName());
		}
		return false;
	}

	/**
	 * プロジェクトからJSLintNatureを削除する.
	 * 
	 * @param project 対象プロジェクト.
	 */
	public static void removeJSLintNature(IProject project) {
		// ネーチャー削除処理.
		try {
			if (!hasJSLintNature(project)) {
				return;
			}
			IProjectDescription description = project.getDescription();
			String[] natureIds = description.getNatureIds();
			for (int i = 0; i < natureIds.length; ++i) {
				String[] newNatures = new String[natureIds.length - 1];
				System.arraycopy(natureIds, 0, newNatures, 0, i);
				System.arraycopy(natureIds, i + 1, newNatures, i, natureIds.length - i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
			}
		} catch (CoreException e) {
			logger.put(Messages.EM0011, e, project.getName());
		}
	}

	/**
	 * JSLintNatureを追加する.
	 * 
	 * @param project 対象プロジェクト.
	 */
	public static void addJSLintNature(IProject project) {
		try {
			if (hasJSLintNature(project)) {
				return;
			}
			IProjectDescription description = project.getDescription();
			String[] natureIds = description.getNatureIds();
			// ネーチャー追加処理.
			String[] newNatures = new String[natureIds.length + 1];
			System.arraycopy(natureIds, 0, newNatures, 0, natureIds.length);
			newNatures[natureIds.length] = JSLintPluginNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (CoreException e) {
			logger.put(Messages.EM0011, e, project.getName());
		}
	}
}
