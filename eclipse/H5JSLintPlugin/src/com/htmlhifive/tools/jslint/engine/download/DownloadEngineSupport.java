package com.htmlhifive.tools.jslint.engine.download;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.htmlhifive.tools.jslint.engine.option.Engine;

public interface DownloadEngineSupport {

	Engine getEngine();

	String getDefaultSource();

	String getEngineSourceUrl();

	EngineInfo getEngineInfo(IProgressMonitor monitor) throws IOException;

}
