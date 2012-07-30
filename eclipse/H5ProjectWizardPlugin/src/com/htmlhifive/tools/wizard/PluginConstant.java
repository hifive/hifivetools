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
 */
package com.htmlhifive.tools.wizard;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.jsdt.internal.ui.JavaPluginImages;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.wizards.NewWizardMessages;

import com.htmlhifive.tools.wizard.log.messages.Messages;

/**
 * <H3>プラグイン定数定義.</H3>
 * 
 * @author fkubo
 */
public abstract class PluginConstant {

	/** properties. */
	private static final Properties properties;

	/** resource-name. */
	private static final String resourceName = "/hifive-wizard-plugin.properties";

	// 読み込み処理.
	static {
		properties = new Properties();
		try {
			properties.load(PluginConstant.class.getResourceAsStream(resourceName));
		} catch (IOException e) {
			throw new IllegalStateException(Messages.SE0002.format(resourceName), e);
		}

	}

	/** ライブラリ用のURL. */
	public static final String URL_LIBRARY_LIST = properties.getProperty("url.library.list");

	/** ライブラリ用のURL_MIRROR. */
	public static final String URL_LIBRARY_LIST_MIRROR = properties.getProperty("url.library.list.mirror");

	/** JavaProjectWizardFirstPageName. */
	public static final String JavaProjectWizardFirstPageName =
			NewWizardMessages.JavaProjectWizardFirstPage_page_pageName;

	// 画像

	/** add. */
	public static final Image IMG_CORRECTION_ADD = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_ADD);
	/** remove. */
	public static final Image IMG_CORRECTION_REMOVE = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);

	/** quick_assist. */
	public static final Image IMG_QUICK_ASSIST = JavaPluginImages.DESC_OBJS_QUICK_ASSIST.createImage();

	/** quickfix_warning. */
	public static final Image IMG_OBJS_FIXABLE_PROBLEM = JavaPluginImages
			.get(JavaPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
	/** quickfix_error. */
	public static final Image IMG_OBJS_FIXABLE_ERROR = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_ERROR);

	/** fatalerror. */
	public static final Image IMG_REFACTORING_FATAL = JavaPluginImages.DESC_OBJS_REFACTORING_FATAL.createImage();
	/** error. */
	public static final Image IMG_REFACTORING_ERROR = JavaPluginImages.DESC_OBJS_REFACTORING_ERROR.createImage();
	/** warning. */
	public static final Image IMG_REFACTORING_WARNING = JavaPluginImages.DESC_OBJS_REFACTORING_WARNING.createImage();
	/** info. */
	public static final Image IMG_REFACTORING_INFO = JavaPluginImages.DESC_OBJS_REFACTORING_INFO.createImage();

	/** translate(checked). */
	public static final Image IMG_NLS_TRANSLATE = JavaPluginImages.DESC_OBJS_NLS_TRANSLATE.createImage();

	/** unknown. */
	public static final Image IMG_OBJS_UNKNOWN = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_UNKNOWN);

	/** unknown. */
	public static final Image IMG_OBJS_NLS_SKIP = JavaPluginImages.get(JavaPluginImages.IMG_OBJS_NLS_SKIP);

	/** field_public. */
	public static final Image IMG_FIELD_PUBLIC = JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
	/** field_default. */
	public static final Image IMG_FIELD_COMPARE = JavaPluginImages.createImageDescriptor(
			JavaScriptPlugin.getDefault().getBundle(),
			JavaPluginImages.ICONS_PATH.append("obj16").append("compare_field.gif"), true).createImage();

	/** category. */
	public static final Image IMG_CATEGORY = ProvUIImages.getImage(ProvUIImages.IMG_CATEGORY);

}