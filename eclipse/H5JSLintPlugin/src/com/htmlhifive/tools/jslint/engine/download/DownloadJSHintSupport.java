package com.htmlhifive.tools.jslint.engine.download;

import com.htmlhifive.tools.jslint.engine.option.Engine;

public class DownloadJSHintSupport extends AbstractDownloadEngineSupport implements DownloadEngineSupport {

	private static final String SOURCE_URL = "https://raw.github.com/jshint/jshint/master/jshint.js";

	public DownloadJSHintSupport() {

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
		return line.startsWith(" */");
	}

	@Override
	public Engine getEngine() {
		return Engine.JSHINT;
	}

}
