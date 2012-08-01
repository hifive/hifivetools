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
package com.htmlhifive.tools.wizard.ui.page.tree;

import org.eclipse.jface.viewers.TreeNode;

import com.htmlhifive.tools.wizard.library.model.LibraryState;
import com.htmlhifive.tools.wizard.library.model.xml.Library;

/**
 * <H3>ライブラリノード.</H3>
 * 
 * @author fkubo
 */
public class LibraryNode extends TreeNode implements LibraryTreeNode {

	/** 存在しているかどうか. */
	private boolean exists = false;

	/** 推奨設定. */
	private boolean recommended = false;

	/** 処理状態. */
	private LibraryState state = LibraryState.DEFAULT;

	/** ファイルリスト. */
	private String[] fileList = null;

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット
	 * @param library ライブラリ
	 */
	public LibraryNode(CategoryNode parent, Library library) {

		super(library);

		setParent(parent);
		// Siteは追加しない.
		// List<SiteNode> list = new ArrayList<SiteNode>();
		// for (Site site: library.getSite()){
		// list.add(new SiteNode(this,site));
		// }
		// setChildren(list.toArray(new SiteNode[0]));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.ui.page.tree.LibraryTreeNode#getLabel()
	 */
	@Override
	public String getLabel() {

		if (getValue().getVersion().matches("[0-9.]+")) {
			return "v" + getValue().getVersion();
		}
		return getValue().getVersion();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.TreeNode#getValue()
	 */
	@Override
	public Library getValue() {

		return (Library) super.getValue();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.TreeNode#getParent()
	 */
	@Override
	public CategoryNode getParent() {

		return (CategoryNode) super.getParent();
	}

	/**
	 * 存在しているかどうかを取得します。
	 * 
	 * @return 存在しているかどうか
	 */
	public boolean isExists() {

		return exists;
	}

	/**
	 * 存在しているかどうかを設定します。
	 * 
	 * @param exists 存在しているかどうか
	 */
	public void setExists(boolean exists) {

		this.exists = exists;
	}

	/**
	 * 推奨設定を取得します.
	 * 
	 * @return 推奨設定.
	 */
	public boolean isRecommended() {

		return this.recommended;
	}

	/**
	 * 推奨設定を設定します.
	 * 
	 * @param recommended 推奨設定.
	 */
	public void setRecommended(boolean recommended) {

		this.recommended = recommended;
	}

	/**
	 * stateを取得します.
	 * 
	 * @return state.
	 */
	public LibraryState getState() {

		return state;
	}

	/**
	 * existsを設定します.
	 * 
	 * @param state exists.
	 */
	public void setState(LibraryState state) {

		if (this.state == LibraryState.DEFAULT || this.state == LibraryState.ADD || this.state == LibraryState.REMOVE
				|| this.state == LibraryState.EXISTS) {
			this.state = state;
		}
	}

	/**
	 * 処理が必要かどうかを取得します.
	 * 
	 * @return 処理が必要かどうか.
	 */
	public boolean isSelected() {

		if (state != LibraryState.DEFAULT) {
			return true;
		}
		return false;
	}

	/**
	 * 追加可能かどうかを取得します.
	 * 
	 * @return 追加可能かどうか.
	 */
	public boolean isAddable() {

		if (state != LibraryState.DEFAULT && state != LibraryState.REMOVE) {
			return true;
		}
		return false;
	}

	/**
	 * ファイルリスト.を取得します.
	 * 
	 * @return ファイルリスト.
	 */
	public String[] getFileList() {

		return fileList;
	}

	/**
	 * ファイルリスト.を設定します.
	 * 
	 * @param fileList ファイルリスト.
	 */
	public void setFileList(String[] fileList) {

		this.fileList = fileList;
	}

	/**
	 * ライセンスチェックが必要かどうかを確認する
	 * 
	 * @return ライセンスチェックが必要かどうか
	 */
	public boolean isNeedConfirmDialog() {
		if (!isAddable()) {
			return false;
		}
		for (TreeNode treeNode : getParent().getChildren()) {
			LibraryNode libraryNode = (LibraryNode) treeNode;
			if (libraryNode != this) {
				if (libraryNode.isExists()) {
					// 存在していればチェックしない.
					return false;
				}
			}
		}
		return true;
	}
}
