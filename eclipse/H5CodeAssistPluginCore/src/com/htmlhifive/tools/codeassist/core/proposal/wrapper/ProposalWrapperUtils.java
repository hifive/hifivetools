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
package com.htmlhifive.tools.codeassist.core.proposal.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.osgi.framework.Bundle;

/**
 * プロポーザルラッパークラスのユーティルクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
@SuppressWarnings("restriction")
public final class ProposalWrapperUtils {
	/**
	 * コンストラクタ.
	 */
	private ProposalWrapperUtils() {

		// nocreate
	}

	/**
	 * ヘルプドキュメントのCSSスタイルを取得する.
	 * 
	 * @return ヘルプドキュメントCSS.
	 */
	public static String getCSSStyles() {

		Bundle bundle = Platform.getBundle(JavaScriptPlugin.getPluginId());
		URL url = bundle.getEntry("/JavadocHoverStyleSheet.css"); //$NON-NLS-1$
		if (url != null) {
			BufferedReader reader = null;
			try {
				url = FileLocator.toFileURL(url);
				return IOUtils.toString(url.openStream());
			} catch (IOException ex) {
				JavaScriptPlugin.log(ex);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
		return null;
	}

}
