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
package com.htmlhifive.tools.wizard.ui.page.tree;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * <H3>ライブラリツリー用最新バージョン用フィルター.</H3>
 * 
 * @author fkubo
 */
public class LibraryTreeLatestViewerFilter extends ViewerFilter {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		if (((CheckboxTreeViewer) viewer).getChecked(element)) {
			// チェック済み.
			return true;
		}
		if (parentElement instanceof CategoryNode && element instanceof LibraryNode) {
			CategoryNode categoryNode = (CategoryNode) parentElement;
			if (categoryNode.getChildren() != null && categoryNode.getChildren().length > 0) {
				if (categoryNode.getChildren()[categoryNode.getChildren().length - 1] == element) {
					// 最終子ノードが一致したら最新とみなす.
					return true;
				}
				LibraryNode libraryNode = (LibraryNode) element;
				if (libraryNode.isExists() || libraryNode.isSelected()) {
					// 存在するので消さない.
					return true;
				}

				return false;
			}
			return true;
		}
		return true;
	}
}
