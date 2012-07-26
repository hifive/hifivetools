package com.htmlhifive.tools.jslint.engine.download;

import com.htmlhifive.tools.jslint.engine.option.Engine;

public class DownloadJSLintSupport extends AbstractDownloadEngineSupport implements DownloadEngineSupport {

	private static final String SOURCE_URL = "https://raw.github.com/douglascrockford/JSLint/master/jslint.js";

	public DownloadJSLintSupport() {

	}

	@Override
	public String getDefaultSource() {
		return null;
	}

	@Override
	public String getEngineSourceUrl() {
		return SOURCE_URL;
	}

	@Override
	protected boolean isEndLicenseLine(String line) {
		return line.startsWith("// WARNING:");
	}

	@Override
	public Engine getEngine() {
		return Engine.JSLINT;
	}

}
