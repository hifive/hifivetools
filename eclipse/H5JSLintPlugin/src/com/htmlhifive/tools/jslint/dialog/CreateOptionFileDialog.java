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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * 入力ダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CreateOptionFileDialog extends TitleAreaDialog {

	/**
	 * オプションファイル名.
	 */
	private WritableValue wvOptionFileName = new WritableValue("", String.class);

	/**
	 * オプションファイル出力先ディレクトリ.
	 */
	private WritableValue wvOutpuDir = new WritableValue("", String.class);

	/**
	 * オプションファイル名テキスト.
	 */
	private Text textOptionFileName;

	/**
	 * タイトル.
	 */
	private String title;

	/**
	 * 出力先ディレクトリテキスト.
	 */
	private Text textOutputDir;

	/**
	 * 
	 * コンストラクタ.
	 * 
	 * @param parentShell シェル.
	 * @param title ダイアログのタイトル
	 */
	public CreateOptionFileDialog(Shell parentShell, String title) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
		this.title = title;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.DL0018.getText());
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		Label labelOutputDir = new Label(composite, SWT.NONE);
		labelOutputDir.setText(Messages.DL0020.getText());

		Composite outputDircomp = new Composite(composite, SWT.NONE);
		outputDircomp.setLayout(new GridLayout(2, false));
		outputDircomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		textOutputDir = new Text(outputDircomp, SWT.BORDER);
		textOutputDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnOutPut = new Button(outputDircomp, SWT.NONE);
		GridData gdBtnOutPut = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdBtnOutPut.widthHint = 60;
		btnOutPut.setLayoutData(gdBtnOutPut);
		btnOutPut.setText(Messages.B0001.getText());
		btnOutPut.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), Messages.DT0008.getText(),
						Messages.DL0022.getText());
				if (dialog.open() != Window.OK) {
					return;
				}
				IContainer outputDir = (IContainer) dialog.getFirstResult();
				textOutputDir.setText(outputDir.getFullPath().toString());
			}
		});

		Label labelOptionFileName = new Label(composite, SWT.NONE);
		labelOptionFileName.setText(Messages.DL0021.getText());

		Composite inputComp = new Composite(composite, SWT.NONE);
		inputComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		inputComp.setLayout(new GridLayout(1, false));

		textOptionFileName = new Text(inputComp, SWT.BORDER);
		textOptionFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		initializeDataBindings();
		setMessage(Messages.DL0019.getText(), IMessageProvider.INFORMATION);
		return area;
	}

	/**
	 * データバインドの初期化.
	 */
	private void initializeDataBindings() {
		DataBindingContext context = new DataBindingContext();
		// validator
		MultiValidator validator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				StringBuilder sb = new StringBuilder();
				String optionFileName = (String) wvOptionFileName.getValue();
				if (StringUtils.isEmpty(optionFileName)) {
					sb.append(Messages.EM0009.format(Messages.DL0021.getText()));
				} else if (!FilenameUtils.isExtension(optionFileName, "xml")) {
					optionFileName += ".xml";
				}
				String outputDir = (String) wvOutpuDir.getValue();

				if (StringUtils.isEmpty(outputDir)) {
					sb.append(Messages.EM0009.format(Messages.DL0020.getText()));
				} else {
					IPath path = new Path(outputDir);
					if (!ResourcesPlugin.getWorkspace().getRoot().exists(path)) {
						sb.append(Messages.EM0000.format(Messages.DL0020.getText()));
					}
				}

				if (StringUtils.isNotEmpty(optionFileName) && StringUtils.isNotEmpty(outputDir)) {
					IPath path = new Path(outputDir + "/" + optionFileName);
					if (ResourcesPlugin.getWorkspace().getRoot().exists(path)) {
						sb.append(Messages.EM0013.getText());
					}
				}
				if (StringUtils.isEmpty(sb.toString())) {
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					return ValidationStatus.info(Messages.DL0019.getText());
				}

				if (getButton(IDialogConstants.OK_ID) != null) {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
				return ValidationStatus.error(sb.toString());
			}
		};

		// 出力ディレクトリ
		IObservableValue outputDir = SWTObservables.observeText(textOutputDir, SWT.Modify);
		context.bindValue(outputDir, wvOutpuDir, null, null);

		// ファイル名
		IObservableValue optionFileName = SWTObservables.observeText(textOptionFileName, SWT.Modify);
		context.bindValue(optionFileName, wvOptionFileName, null, null);

		context.addValidationStatusProvider(validator);
		TitleAreaDialogSupport.create(this, context);

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
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(515, 279);
	}

	/**
	 * 出力先ファイルパスを取得する.
	 * 
	 * @return 出力先ファイルパス.
	 */
	public String getOutputFilePath() {
		String outputDir = (String) wvOutpuDir.getValue();
		String optionFileName = (String) wvOptionFileName.getValue();
		if (!FilenameUtils.isExtension(optionFileName, "xml")) {
			optionFileName += ".xml";
		}
		return outputDir + "/" + optionFileName;
	}
}
