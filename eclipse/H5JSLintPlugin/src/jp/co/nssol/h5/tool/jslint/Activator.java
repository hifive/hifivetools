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
 package jp.co.nssol.h5.tool.jslint;

import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLoggerFactory;
import jp.co.nssol.h5.tool.jslint.logger.impl.JSLintPluginLoggerDefaultImpl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * JSLintプラグインクラス.
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	/**
	 * プラグインID.
	 */
	public static final String PLUGIN_ID = "jp.co.nssol.javascript.parse"; //$NON-NLS-1$

	/**
	 * インスタンス.
	 */
	private static Activator plugin;

	/**
	 * コンストラクタ.
	 */
	public Activator() {

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
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
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
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
	public static Activator getDefault() {

		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {

		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	public void earlyStartup() {

		// 自動ビルドのチェックをするため、プラグインを活性化する必要がある。
	}
}