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
 *
 */
 package jp.co.nssol.h5.tool.jslint.dialog;

import jp.co.nssol.h5.tool.jslint.Activator;
import jp.co.nssol.h5.tool.jslint.messages.Messages;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * リソース選択ダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class FileSelectionDialog extends AbstractResourceSelectionDialog {

	/**
	 * 表示する拡張子.
	 */
	private String[] extensions;

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param parent シェル
	 * @param title ダイアログタイトル
	 * @param message ダイアログメッセージ
	 * @param extensions 表示する拡張子
	 */
	public FileSelectionDialog(Shell parent, String title, String message, String[] extensions) {

		super(parent, title, message);
		this.extensions = extensions.clone();

	}

	/**
	 * 
	 * ダイアログに表示される要素のフィルター.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	private class FileSelectionFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			// IFileだった場合は拡張子が合っていれば表示
			if (element instanceof IFile) {
				IFile ifile = (IFile) element;
				for (String extension : extensions) {
					if (StringUtils.equals(ifile.getFileExtension(), extension)) {
						return true;
					}
				}
				return false;
			}

			return true;
		}

	}

	/**
	 * ファイル選択ダイアログのバリデータクラス.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	private static class FileSelectionValidator implements ISelectionStatusValidator {

		@Override
		public IStatus validate(Object[] selection) {

			for (Object select : selection) {
				if (select instanceof IFile) {
					return new Status(IStatus.OK, Activator.PLUGIN_ID, "");
				}
			}
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.EM0002.getText());
		}

	}

	@Override
	protected ISelectionStatusValidator getValidator() {

		return new FileSelectionValidator();
	}

	@Override
	protected ViewerFilter getFilter() {

		return new FileSelectionFilter();
	}

}
