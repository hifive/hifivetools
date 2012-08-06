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

import com.htmlhifive.tools.wizard.library.model.xml.Site;

/**
 * <H3>サイトノード.</H3>
 * 
 * @author fkubo
 */
public class SiteNode extends TreeNode implements LibraryTreeNode {

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット
	 * @param site サイト
	 */
	public SiteNode(LibraryNode parent, Site site) {

		super(site);
		setParent(parent);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.ui.page.tree.LibraryTreeNode#getLabel()
	 */
	@Override
	public String getLabel() {

		return getValue().getUrl();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.TreeNode#getValue()
	 */
	@Override
	public Site getValue() {

		return (Site) super.getValue();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.TreeNode#getParent()
	 */
	@Override
	public LibraryNode getParent() {

		return (LibraryNode) super.getParent();
	}

}
