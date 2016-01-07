package com.htmlhifive.tools.jslint;

import java.io.IOException;
import java.util.Properties;

public class PropertyUtils {

	private static Properties properties;

	static {
		properties = new Properties();
		try {
			properties.load(PropertyUtils.class.getClassLoader().getResourceAsStream("app.properties"));
		} catch (IOException e) {
			// ignore
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

}
