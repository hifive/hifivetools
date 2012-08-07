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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.TreeNode;

import com.htmlhifive.tools.wizard.library.LibraryList;
import com.htmlhifive.tools.wizard.library.xml.Category;
import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * <H3>ルートノード.</H3>
 * 
 * @author fkubo
 */
public class RootNode extends TreeNode implements LibraryTreeNode {

	/** defaultInstallPath. */
	private String defaultInstallPath;

	/** defaultProjectPath. */
	private IContainer defaultProjectPath;

	/**
	 * コンストラクタ.
	 * 
	 * @param libraryList ライブラリリスト.
	 */
	public RootNode(LibraryList libraryList) {

		super(null);

		setParent(null);
		List<CategoryNode> list = new ArrayList<CategoryNode>();
		for (Category category : libraryList.getLibraries().getSiteLibraries().getCategory()) {
			list.add(new CategoryNode(this, category));
		}
		setChildren(list.toArray(new CategoryNode[0]));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.ui.page.tree.LibraryTreeNode#getLabel()
	 */
	@Override
	public String getLabel() {

		return UIMessages.LibraryImportPageComposite_tree_root_text;
	}

	/**
	 * defaultInstallPath.を取得します.
	 * 
	 * @return defaultInstallPath.
	 */
	public String getDefaultInstallPath() {

		return defaultInstallPath;
	}

	/**
	 * defaultInstallPath.を設定します.
	 * 
	 * @param defaultInstallPath defaultInstallPath.
	 */
	public void setDefaultInstallPath(String defaultInstallPath) {

		this.defaultInstallPath = defaultInstallPath;
	}

	/**
	 * defaultProjectPath.を取得します.
	 * 
	 * @return defaultProjectPath.
	 */
	public IContainer getDefaultProjectPath() {

		return defaultProjectPath;
	}

	/**
	 * defaultProjectPath.を設定します.
	 * 
	 * @param defaultProjectPath defaultProjectPath.
	 */
	public void setDefaultProjectPath(IContainer defaultProjectPath) {

		this.defaultProjectPath = defaultProjectPath;
	}

}
