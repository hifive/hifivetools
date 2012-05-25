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
 package jp.co.nssol.h5.tools.codeassist.ui.dialog;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * リソース選択ダイアログの抽象クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public abstract class AbstractResourceSelectionDialog extends ElementTreeSelectionDialog {
	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param parent シェル
	 * @param title ダイアログタイトル
	 * @param message ダイアログメッセージ
	 */
	public AbstractResourceSelectionDialog(Shell parent, String title, String message) {

		super(parent, new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
		setTitle(title);
		setMessage(message);
		addFilter(getFilter());
		setInput(ResourcesPlugin.getWorkspace().getRoot());
		setValidator(getValidator());
	}

	/**
	 * バリデータを取得する.必要ない場合はnullを返す.
	 * 
	 * @return バリデータ.
	 */
	protected abstract ISelectionStatusValidator getValidator();

	/**
	 * フィルターを返す.すべて表示させる場合はnullを返す.
	 * 
	 * @return ビューフィルタ.
	 */
	protected abstract ViewerFilter getFilter();
}
