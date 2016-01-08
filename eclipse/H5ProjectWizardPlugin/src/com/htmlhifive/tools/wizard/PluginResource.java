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
 */
package com.htmlhifive.tools.wizard;

import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.jsdt.internal.ui.JavaPluginImages;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;

/**
 * <H3>プラグイン定数定義.</H3>
 * 
 * @author fkubo
 */
public abstract class PluginResource {
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