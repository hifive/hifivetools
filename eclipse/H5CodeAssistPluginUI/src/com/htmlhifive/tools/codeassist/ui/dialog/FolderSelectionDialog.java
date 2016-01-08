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
 *
 */
package com.htmlhifive.tools.codeassist.ui.dialog;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * フォルダ選択のダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class FolderSelectionDialog extends AbstractResourceSelectionDialog {

	/**
	 * コンストラクタ.
	 * 
	 * @param parent シェル
	 * @param title ダイアログタイトル.
	 * @param message メッセージ.
	 */
	public FolderSelectionDialog(Shell parent, String title, String message) {

		super(parent, title, message);
	}

	@Override
	protected ISelectionStatusValidator getValidator() {

		return null;
	}

	@Override
	protected ViewerFilter getFilter() {

		return new FolderFilter();
	}

	/**
	 * フォルダ選択フィルター.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	private static class FolderFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (element instanceof IContainer) {
				return true;
			}
			return false;
		}

	}

}
