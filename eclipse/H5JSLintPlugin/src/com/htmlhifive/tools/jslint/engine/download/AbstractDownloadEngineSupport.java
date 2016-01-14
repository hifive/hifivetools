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
package com.htmlhifive.tools.jslint.engine.download;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.htmlhifive.tools.jslint.messages.Messages;
import com.htmlhifive.tools.jslint.util.PluginResourceUtils;

/**
 * ダウンロード支援の抽象クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public abstract class AbstractDownloadEngineSupport implements DownloadEngineSupport {

	@Override
	public String getDefaultSource() {
		return null;
	}

	@Override
	public EngineInfo getEngineInfo(IProgressMonitor monitor)
			throws IOException, SecurityException, IllegalArgumentException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
		IProgressMonitor actualMonitor = monitor;
		if (monitor == null) {
			actualMonitor = new NullProgressMonitor();
		}
		actualMonitor.setTaskName(Messages.T0009.getText());

		EngineInfo info = new EngineInfo();
		info.setLicenseStr(PluginResourceUtils.getFileContent(getLicenseSourcePath(), LicenseFileReader.class));
		info.setMainSource(PluginResourceUtils.getFileContent(getEngineSourcePath()));
		monitor.done();
		return info;
	}
}
