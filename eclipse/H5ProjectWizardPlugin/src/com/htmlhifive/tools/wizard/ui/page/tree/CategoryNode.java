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
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TreeNode;

import com.htmlhifive.tools.wizard.library.xml.Category;
import com.htmlhifive.tools.wizard.library.xml.Info;
import com.htmlhifive.tools.wizard.library.xml.Library;
import com.htmlhifive.tools.wizard.utils.H5IOUtils;

/**
 * <H3>カテゴリノード.</H3>
 * 
 * @author fkubo
 */
public class CategoryNode extends TreeNode implements LibraryTreeNode {

	/** parentPath. */
	private IContainer parentPath;

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット
	 * @param category カテゴリ
	 */
	public CategoryNode(TreeNode parent, Category category) {

		super(category);

		setParent(parent);
		List<LibraryNode> list = new ArrayList<LibraryNode>();
		for (Library library : category.getLibrary()) {
			list.add(new LibraryNode(this, library));
		}
		setChildren(list.toArray(new LibraryNode[0]));
	}

	/**
	 * Infoを取得する.
	 * 
	 * @return Info
	 */
	public Info getInfo() {

		Info targetInfo = null;
		for (Info info : getValue().getInfo()) {
			targetInfo = info;
			if (Locale.getDefault().getLanguage().equals(info.getLang())) {
				// 上書き.
				targetInfo = info;
				break;
			}
		}
		return targetInfo;
	}

	/**
	 * 説明を取得する.
	 * 
	 * @return 説明
	 */
	public String getDescription() {

		Info info = getInfo();
		if (info != null) {
			return info.getDescription();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.ui.page.tree.LibraryTreeNode#getLabel()
	 */
	@Override
	public String getLabel() {

		Info info = getInfo();
		if (info != null) {
			return info.getTitle();
		}
		return getLabel();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.TreeNode#getValue()
	 */
	@Override
	public Category getValue() {

		return (Category) super.getValue();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.TreeNode#getParent()
	 */
	@Override
	public RootNode getParent() {

		return (RootNode) super.getParent();
	}

	/**
	 * parentPathを取得します.
	 * 
	 * @return parentPath
	 */
	public IContainer getParentPath() {

		if (parentPath != null) {
			return parentPath;
		}
		// パスチェックは、Workspace.newResourceより取得.
		if (getValue().getPath().startsWith("${default.js.lib.path}")
				&& H5IOUtils.isValidWorkspacePath(getParent().getDefaultProjectPath().getFullPath()
						.append(getParent().getDefaultInstallPath()))) {
			// 正しい場合
			return getParent().getDefaultProjectPath()
					.getFolder(Path.fromOSString(getParent().getDefaultInstallPath()));
		}
		return getParent().getDefaultProjectPath();
	}

	/**
	 * parentPathを設定します.
	 * 
	 * @param parentPath parentPath
	 */
	public void setParentPath(IContainer parentPath) {

		this.parentPath = parentPath;
	}

	/**
	 * パスラベルを取得します.
	 * 
	 * @return pathLable
	 */
	public String getPathLable() {

		if (getInstallFullPath() != null) {
			return getInstallFullPath().getProjectRelativePath().toString() + "/"; // プロジェクト相対とする
		}
		return getInstallSubPath();// getValue().getPath();
	}

	/**
	 * インストール先のパスを取得します.
	 * 
	 * @return インストール先のパス
	 */
	public IContainer getInstallFullPath() {

		if (getValue().getPath() == null) {
			return null;
		}

		String realPath = getInstallSubPath();
		if (StringUtils.isEmpty(realPath)) {
			return getParentPath();
		}
		return getParentPath().getFolder(Path.fromOSString(realPath));
	}

	/**
	 * インストールパス用のサブフォルダ名を取得します.
	 * 
	 * @return インストールパス用のサブフォルダ名
	 */
	public String getInstallSubPath() {

		if (getValue().getPath() == null) {
			return null;
		}

		if (getValue().getPath().equals("${default.js.lib.path}")) {
			return "";
		} else if (getValue().getPath().startsWith("${default.js.lib.path}/")) {
			return getValue().getPath().substring("${default.js.lib.path}/".length());
		}
		return getValue().getPath();
	}

}
