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
package com.htmlhifive.tools.wizard.library;

import org.eclipse.swt.graphics.Image;

import com.htmlhifive.tools.wizard.PluginResource;
import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * <H3>ライブラリ処理状態.</H3>
 * 
 * @author fkubo
 */
public enum LibraryState {

	/** DEFAULT. */
	DEFAULT("", "", PluginResource.IMG_FIELD_COMPARE),
	/** ADDITION. */
	ADD("+", UIMessages.LibraryState_ADD, PluginResource.IMG_CORRECTION_ADD),
	/** REMOVE. */
	REMOVE("-", UIMessages.LibraryState_REMOVE, PluginResource.IMG_CORRECTION_REMOVE),
	/** EXISTS. */
	EXISTS(">", UIMessages.LibraryState_EXIST, PluginResource.IMG_FIELD_PUBLIC);
	//	/** EXISTS. */
	//	INCOMPLETE(">", UIMessages.LibraryState_INCOMPLETE, PluginResource.IMG_REFACTORING_WARNING),
	//	/** DOWNLOAD_ERROR. */
	//	DOWNLOAD_ERROR("x", UIMessages.LibraryState_DOWNLOAD_ERROR, PluginResource.IMG_OBJS_FIXABLE_ERROR),
	//	/** EXTRACT_ERROR. */
	//	EXTRACT_ERROR("x", UIMessages.LibraryState_EXTRACT_ERROR, PluginResource.IMG_OBJS_FIXABLE_PROBLEM);

	/** 文字記号. */
	private String mark;

	/** 文字表現. */
	private String text;

	/** 画像パス. */
	private Image image;

	/**
	 * コンストラクタ.
	 * 
	 * @param mark 文字記号
	 * @param text テキスト
	 * @param imagePath 画像パス
	 */
	LibraryState(String mark, String text, Image image) {

		this.mark = mark;
		this.text = text;
		this.image = image;
	}

	/**
	 * 文字記号を取得する.
	 * 
	 * @return 文字記号
	 */
	public String getMark() {

		return mark;
	}

	/**
	 * 文字表現を取得する.
	 * 
	 * @return 文字表現
	 */
	public String getText() {

		return text;
	}

	/**
	 * イメージを取得する.
	 * 
	 * @return イメージ
	 */
	public Image getImage() {

		return image;
	}
}
