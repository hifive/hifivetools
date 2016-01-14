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

import com.htmlhifive.tools.jslint.PropertyUtils;
import com.htmlhifive.tools.jslint.engine.option.Engine;

/**
 * JSLint用ダウンロード支援クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class DownloadJSLintSupport extends AbstractDownloadEngineSupport implements DownloadEngineSupport {

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tools.jslint.engine.download.AbstractDownloadEngineSupport
	 * #getDefaultSource()
	 */
	@Override
	public String getDefaultSource() {
		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see com.htmlhifive.tools.jslint.engine.download.DownloadEngineSupport#
	 * getEngineSourceUrl()
	 */
	@Override
	public String getEngineSourceUrl() {

		return PropertyUtils.getProperty("path.jslint.js");
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tools.jslint.engine.download.AbstractDownloadEngineSupport
	 * #isEndLicenseLine(java.lang.String)
	 */
	@Override
	protected boolean isEndLicenseLine(String line) {
		return line.startsWith("// WARNING:");
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tools.jslint.engine.download.DownloadEngineSupport#getEngine
	 * ()
	 */
	@Override
	public Engine getEngine() {
		return Engine.JSLINT;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see com.htmlhifive.tools.jslint.engine.download.DownloadEngineSupport#
	 * getLicenseSourceUrl ()
	 */
	@Override
	public String getLicenseSourceUrl() {
		return PropertyUtils.getProperty("path.jslint.license.js");
	}

}
