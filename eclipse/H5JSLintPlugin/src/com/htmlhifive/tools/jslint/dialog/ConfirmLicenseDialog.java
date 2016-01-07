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
package com.htmlhifive.tools.jslint.dialog;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * ライセンス確認ダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class ConfirmLicenseDialog extends Dialog {
	/**
	 * ライセンス.
	 */
	private String license;
	/**
	 * ライセンステキスト.
	 */
	private Text textLicenseText;
	/**
	 * ライセンス同意ラジオボタン.
	 */
	private Button btnAcceptLicense;
	/**
	 * ダイアログタイトル.
	 */
	private String title;

	/**
	 * @param parentShell 親シェル
	 * @param license ライセンス.
	 * @param title ダイアログタイトル.
	 */
	public ConfirmLicenseDialog(Shell parentShell, String license, String title) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
		this.license = license;
		this.title = title;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.DL0028.getText());

		textLicenseText = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL
				| SWT.MULTI);
		textLicenseText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		textLicenseText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		btnAcceptLicense = new Button(composite, SWT.RADIO);
		btnAcceptLicense.setText(Messages.DL0029.getText());
		btnAcceptLicense.setSelection(false);

		Button btnDenyLicense = new Button(composite, SWT.RADIO);
		btnDenyLicense.setSelection(true);
		btnDenyLicense.setText(Messages.DL0030.getText());
		return container;
	}

	/**
	 * データバインドの初期化.
	 */
	private void initialDataBinding() {

		DataBindingContext context = new DataBindingContext();

		// OKボタンのValidatorセット
		UpdateValueStrategy strategy = new UpdateValueStrategy();
		strategy.setBeforeSetValidator(new IValidator() {
			@Override
			public IStatus validate(Object value) {
				getButton(IDialogConstants.OK_ID).setEnabled((Boolean) value);
				return null;
			}
		});
		IObservableValue obsSelectiveAcceptLicense = WidgetProperties.selection().observe(btnAcceptLicense);
		IObservableValue obsEnabledOkButton = WidgetProperties.enabled().observe(getButton(IDialogConstants.OK_ID));
		context.bindValue(obsSelectiveAcceptLicense, obsEnabledOkButton, null, null);

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		initialDataBinding();
		textLicenseText.setText(license);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return super.getInitialSize();
	}

}
