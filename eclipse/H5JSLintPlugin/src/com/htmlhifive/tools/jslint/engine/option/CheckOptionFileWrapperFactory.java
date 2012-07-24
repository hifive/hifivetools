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
package com.htmlhifive.tools.jslint.engine.option;

import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import com.htmlhifive.tools.jslint.Activator;
import com.htmlhifive.tools.jslint.engine.option.xml.JaxbUtil;
import com.htmlhifive.tools.jslint.engine.option.xml.JsCheckOption;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * チェックオプションファイルラッパークラスのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class CheckOptionFileWrapperFactory {
	/**
	 * コンストラクタ.
	 */
	private CheckOptionFileWrapperFactory() {

	}

	/**
	 * チェックオプションファイルラッパーを生成する.
	 * 
	 * @param file チェックオプションファイル.
	 * @return チェックオプションファイルラッパー.
	 * @throws CoreException 生成例外.
	 */
	public static CheckOptionFileWrapper createCheckOptionFileWrapper(IFile file) throws CoreException {

		return createCheckOptionFileWrapper(file, file.getFileExtension());
	}

	/**
	 * チェックオプションファイルラッパーを生成する.
	 * 
	 * @param path チェックオプションファイルパス.
	 * @return チェックオプションファイルラッパー.
	 * @throws CoreException 生成例外.
	 */
	public static CheckOptionFileWrapper createCheckOptionFileWrapper(String path) throws CoreException {

		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
		if (file != null) {
			return createCheckOptionFileWrapper(file);
		}
		// TODO ワークスペース外のファイルを指定した場合.
		return null;
	}

	/**
	 * チェックオプションファイルラッパーを生成する.
	 * 
	 * @param file チェックオプションファイル.
	 * @param extension 拡張子.
	 * @return チェックオプションファイルラッパー.
	 * @throws CoreException 生成例外.
	 */
	private static CheckOptionFileWrapper createCheckOptionFileWrapper(IFile file, String extension)
			throws CoreException {

		try {
			if (!file.exists()) {
				JaxbUtil.saveJsCheckOption(new JsCheckOption(), file);
			}
			if (StringUtils.endsWith(extension, "xml")) {
				return new CheckOptionXmlWrapper(file);
			} else if (StringUtils.endsWith(extension, "properties")) {
				Properties prop = new Properties();
				prop.load(file.getContents());
				// TODO プロパティのプレフィックス.
				return new CheckOptionPropertyWrapper(prop);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (JAXBException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.EM0008.getText(), e));
		}
		return null;
	}
}
