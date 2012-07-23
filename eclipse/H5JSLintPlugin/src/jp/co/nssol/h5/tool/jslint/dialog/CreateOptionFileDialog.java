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
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public CreateOptionFileDialog(Shell parentShell, String title) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setHelpAvailable(false);
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
		setTitle("生成ファイルの指定");
		setMessage("生成する種別を選択し、設定ファイルの名前を入力してください。");
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new GridLayout(3, false));

		Label labelOutputDir = new Label(composite, SWT.NONE);
		labelOutputDir.setText("出力ディレクトリ");

		Label label4 = new Label(composite, SWT.NONE);
		GridData gdLabel4 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gdLabel4.heightHint = 18;
		label4.setLayoutData(gdLabel4);
		label4.setText("：");

		Composite outputDircomp = new Composite(composite, SWT.NONE);
		outputDircomp.setLayout(new GridLayout(2, false));
		outputDircomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		textOutputDir = new Text(outputDircomp, SWT.BORDER);
		textOutputDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnOutPut = new Button(outputDircomp, SWT.NONE);
		GridData gdBtnOutPut = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdBtnOutPut.widthHint = 60;
		btnOutPut.setLayoutData(gdBtnOutPut);
		btnOutPut.setText("選択");
		btnOutPut.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), "生成するディレクトリの選択",
						"設定ファイルを生成するディレクトリを選択してください.");
				if (dialog.open() != Window.OK) {
					return;
				}
				IContainer outputDir = (IContainer) dialog.getFirstResult();
				textOutputDir.setText(outputDir.getFullPath().toString());
			}
		});

		Label labelOptionFileName = new Label(composite, SWT.NONE);
		labelOptionFileName.setText("設定ファイル名");

		Label label2 = new Label(composite, SWT.NONE);
		label2.setText("：");

		Composite inputComp = new Composite(composite, SWT.NONE);
		inputComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		inputComp.setLayout(new GridLayout(1, false));

		textOptionFileName = new Text(inputComp, SWT.BORDER);
		textOptionFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		initializeDataBindings();
		return area;
	}

	private void initializeDataBindings() {
		DataBindingContext context = new DataBindingContext();

		MultiValidator validator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				StringBuilder sb = new StringBuilder();
				String optionFileName = (String) wvOptionFileName.getValue();
				if (StringUtils.isEmpty(optionFileName)) {
					sb.append("設定ファイル名を入力してください.");
				} else if (!FilenameUtils.isExtension(optionFileName, "xml")) {
					optionFileName += ".xml";
				}
				String outputDir = (String) wvOutpuDir.getValue();

				if (StringUtils.isEmpty(outputDir)) {
					sb.append("出力先ディレクトリを指定してください。");
				} else {
					IPath path = new Path(outputDir);
					if (!ResourcesPlugin.getWorkspace().getRoot().exists(path)) {
						sb.append("出力先ディレクトリが存在しません。");
					}
				}

				if (StringUtils.isNotEmpty(optionFileName) && StringUtils.isNotEmpty(outputDir)) {
					IPath path = new Path(outputDir + "/" + optionFileName);
					if (ResourcesPlugin.getWorkspace().getRoot().exists(path)) {
						sb.append("指定したパスにファイルが既に存在します。");
					}
				}
				if (StringUtils.isEmpty(sb.toString())) {
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					return ValidationStatus.info("生成する種別を選択し、設定ファイルの名前を入力してください。");
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

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(488, 279);
	}

	public String getOutputFilePath() {
		String outputDir = (String) wvOutpuDir.getValue();
		String optionFileName = (String) wvOptionFileName.getValue();
		return outputDir + "/" + optionFileName;
	}
}
