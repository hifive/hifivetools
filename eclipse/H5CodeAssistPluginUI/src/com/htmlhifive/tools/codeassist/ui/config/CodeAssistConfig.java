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
package com.htmlhifive.tools.codeassist.ui.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;

import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.core.messages.Messages;
import com.htmlhifive.tools.codeassist.ui.H5CodeAssistUIPlugin;
import com.htmlhifive.tools.codeassist.ui.messages.UIMessages;
import com.htmlhifive.tools.codeassist.ui.view.bean.OptionConfigureBean;

/**
 * コードアシストの設定情報を管理するクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CodeAssistConfig {

	/**
	 * 設定情報のキープレフィックス.
	 */
	private static final String CONFIG_KEY_PREFIX = "hi5.tool.codeassist.";

	/**
	 * オプションファイルパス.
	 */
	private static final String KEY_OPTION_PATH = CONFIG_KEY_PREFIX + "option";

	/**
	 * ロガー.
	 */
	private static H5CodeAssistPluginLogger logger = H5CodeAssistPluginLoggerFactory.getLogger(CodeAssistConfig.class);

	/**
	 * 設定ファイル.
	 */
	private IFile configProp;

	/**
	 * 設定ビーン.
	 */
	private final OptionConfigureBean configBean;

	/**
	 * コンストラクタ.
	 * 
	 * @param file 設定ファイル.
	 */
	CodeAssistConfig(IFile file) {

		configBean = new OptionConfigureBean();
		configProp = file;
		load();
	}

	/**
	 * 設定ファイルをロードする.
	 */
	void load() {

		try {
			configProp.refreshLocal(IResource.DEPTH_ZERO, null);
			if (configProp.exists()) {
				Properties properties = new Properties();
				properties.load(configProp.getContents());
				configBean.setOptionFilePath(properties.getProperty(KEY_OPTION_PATH, ""));
			}
		} catch (CoreException e) {
			logger.log(Messages.EM0001, e);
		} catch (IOException e) {
			logger.log(Messages.EM0001, e);
		}

	}

	/**
	 * 設定ビーンを取得する.
	 * 
	 * @return 設定ビーン.
	 */
	public OptionConfigureBean getConfigBean() {

		return configBean;
	}

	/**
	 * ビーンを設定ファイルに保存する.
	 * 
	 * @return セーブが成功したかどうか
	 */
	public boolean saveConfig() {

		Properties properties = new Properties();
		properties.setProperty(KEY_OPTION_PATH, configBean.getOptionFilePath());
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		InputStream input = null;
		try {
			properties.store(output, "");
			output.flush();
			input = new ByteArrayInputStream(output.toByteArray());
			configProp.refreshLocal(IResource.DEPTH_ZERO, null);
			if (configProp.exists()) {
				configProp.setContents(input, false, false, null);
			} else {
				configProp.create(input, false, null);
			}
			return true;
		} catch (CoreException e) {
			logger.log(UIMessages.UIEM0002, e);
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					UIMessages.UIDT0002.getText(), UIMessages.UIEM0002.getText(), e.getStatus());
		} catch (IOException e) {
			logger.log(UIMessages.UIEM0002, e);
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), UIMessages.UIDT0002
					.getText(), UIMessages.UIEM0002.getText(), new Status(IStatus.ERROR,
					H5CodeAssistUIPlugin.PLUGIN_ID, e.getMessage()));
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
		}
		return false;
	}
}
