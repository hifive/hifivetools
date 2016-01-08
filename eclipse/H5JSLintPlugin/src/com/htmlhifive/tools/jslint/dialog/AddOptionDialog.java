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
package com.htmlhifive.tools.jslint.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 新規オプションを追加するダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class AddOptionDialog extends Dialog {

	// private Text textOptionKey;
	//
	// private Combo comboClass;
	//
	// private Text textDescription;
	//
	// private Text textDetail;

	/**
	 * コンストラクタ.
	 * 
	 * @param parentShell シェル.
	 */
	public AddOptionDialog(Shell parentShell) {

		super(parentShell);
	}

	@Override
	protected Control createContents(Composite parent) {

		return super.createContents(parent);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(8, true));
		Label label = new Label(comp, SWT.None);
		label.setText("test");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getShell().setText("aaaaaaaaa");
		return comp;
	}

}
