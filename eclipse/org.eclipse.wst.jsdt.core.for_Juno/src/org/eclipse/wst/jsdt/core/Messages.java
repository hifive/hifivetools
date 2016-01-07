package org.eclipse.wst.jsdt.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.wst.jsdt.core.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key, Object[] objects) {
		try {
			return  MessageFormat.format( RESOURCE_BUNDLE.getString(key),  objects);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
