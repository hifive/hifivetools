package com.htmlhifive.tools.wizard.download;

import java.io.InputStream;
import java.util.Date;

public interface HttpMethodWrapper {

	void setConnectionTimeout(int connectionTimeout);

	void setProxy();

	InputStream getInputStream();

	int getContentLength();

	Date LastModified();
}
