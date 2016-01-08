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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.htmlhifive.tools.wizard.library.LibraryList;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;

/**
 * MahoWizardプラグイン.
 * 
 * @author fkubo
 */
public class H5WizardPlugin extends AbstractUIPlugin {

	/** ダウンロードしたライブラリ情報. */
	private LibraryList libraryList;

	/** 選択されたライブラリ情報. */
	private final Set<LibraryNode> selectedLibrarySet = new LinkedHashSet<LibraryNode>();

	/**
	 * プラグインID.
	 */
	private static final String PLUGIN_ID = "com.htmlhifive.tools.wizard";

	/**
	 * プラグインインスタンス.
	 */
	private static H5WizardPlugin instance;

	/**
	 * コンストラクタ.
	 */
	public H5WizardPlugin() {

		super();
		instance = this;
	}

	/**
	 * instanceを取得します.
	 * 
	 * @return instance
	 */
	public static H5WizardPlugin getInstance() {

		return instance;
	}

	/**
	 * idを取得します.
	 * 
	 * @return id
	 */
	public static String getId() {

		return PLUGIN_ID;
	}

	/**
	 * ライブラリリストを取得する.
	 * 
	 * @return ライブラリリスト
	 */
	public LibraryList getLibraryList() {

		return libraryList;
	}

	/**
	 * ライブラリリストを設定する.
	 * 
	 * @param libraryList ライブラリリスト
	 */
	public void setLibraryList(LibraryList libraryList) {

		this.libraryList = libraryList;
	}

	/**
	 * 選択済ライブラリセットを取得する
	 * 
	 * @return 選択済ライブラリセット
	 */
	public Set<LibraryNode> getSelectedLibrarySet() {

		return selectedLibrarySet;
	}

	/**
	 * ソートされた選択済ライブラリセットを取得する
	 * 
	 * @return ソートされた選択済ライブラリセット
	 */
	public Set<LibraryNode> getSelectedLibrarySortedSet() {

		// 削除だけ先にする.
		Set<LibraryNode> sortedSet = new LinkedHashSet<LibraryNode>();
		for (LibraryNode libraryNode : selectedLibrarySet) {
			if (libraryNode.isAddable()) {
				sortedSet.add(libraryNode);
			}
		}
		for (LibraryNode libraryNode : selectedLibrarySet) {
			if (!libraryNode.isAddable()) {
				sortedSet.add(libraryNode);
			}
		}
		return sortedSet;
	}
}
