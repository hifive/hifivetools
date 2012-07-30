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
 package com.htmlhifive.tools.codeassist.ui;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.ui.logger.H5CodeAssistPluginLoggerImpl;

/**
 * UIプラグインクラス.
 *
 * @author NS Solutions Corporation
 *
 */
public class H5CodeAssistUIPlugin extends AbstractUIPlugin {

	/**
	 * プラグインID.
	 */
	public static final String PLUGIN_ID = "com.htmlhifive.tools.codeassist.ui.H5CodeAssistUIPlugin"; //$NON-NLS-1$

	/**
	 * プラグイン.
	 */
	private static H5CodeAssistUIPlugin plugin;

	/**
	 * コンストラクタ.
	 */
	public H5CodeAssistUIPlugin() {

	}

	@Override
	public void start(BundleContext context) throws Exception {

		super.start(context);
		plugin = this;
		H5CodeAssistPluginLoggerFactory.setImpl(H5CodeAssistPluginLoggerImpl.class);
	}

	@Override
	public void stop(BundleContext context) throws Exception {

		plugin = null;
		super.stop(context);
	}

	/**
	 * プラグインインスタンスを取得する.
	 *
	 * @return プラグインインスタンス.
	 */
	public static H5CodeAssistUIPlugin getDefault() {

		return plugin;
	}

	/**
	 * プラグインからの相対パスからイメージデスクリプターを取得する.
	 *
	 * @param path 画像パス.
	 * @return イメージデスクリプター
	 */
	public static ImageDescriptor getImageDescriptor(String path) {

		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
