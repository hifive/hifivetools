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

public class ConfirmLicenseDialog extends Dialog {
	private DataBindingContext m_bindingContext;
	private String licenseText;
	private Text text;
	private Button btnAcceptLicense;
	private String title;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public ConfirmLicenseDialog(Shell parentShell, String licenseText, String title) {
		super(parentShell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
		this.licenseText = licenseText;
		this.title = title;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Label label = new Label(container, SWT.NONE);
		label.setText("ライセンスの確認");

		text = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		text.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		btnAcceptLicense = new Button(composite, SWT.RADIO);
		btnAcceptLicense.setText("ライセンスに同意します");
		btnAcceptLicense.setSelection(false);

		Button btnDenyLicense = new Button(composite, SWT.RADIO);
		btnDenyLicense.setSelection(true);
		btnDenyLicense.setText("ライセンスに同意しません");
		return container;
	}

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

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		initialDataBinding();
		text.setText(licenseText);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return super.getInitialSize();
	}

}
