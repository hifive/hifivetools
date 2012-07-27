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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.htmlhifive.tools.jslint.JSLintPlugin;
import com.htmlhifive.tools.jslint.engine.download.DownloadEngineSupport;
import com.htmlhifive.tools.jslint.engine.download.DownloadJSHintSupport;
import com.htmlhifive.tools.jslint.engine.download.DownloadJSLintSupport;
import com.htmlhifive.tools.jslint.engine.download.EngineInfo;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * エンジンファイル(jslint.js等)をダウンロードするダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CreateEngineDialog extends TitleAreaDialog {
	/**
	 * 
	 * ダウンロード実行Runnable.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	private static class DownloadRunnable implements IRunnableWithProgress {

		/**
		 * エンジンファイル情報.
		 */
		private EngineInfo result;

		/**
		 * ダウンロード支援クラス.
		 */
		private DownloadEngineSupport support;

		/**
		 * コンストラクタ.
		 * 
		 * @param support ダウンロード支援クラス.
		 */
		public DownloadRunnable(DownloadEngineSupport support) {
			this.support = support;
		}

		/*
		 * (非 Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse
		 * .core.runtime.IProgressMonitor)
		 */
		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			try {
				result = support.getEngineInfo(monitor);
			} catch (IOException e) {
				throw new InvocationTargetException(e, e.getMessage());
			}
		}

		/**
		 * ダウンロード結果を取得する.
		 * 
		 * @return ダウンロード結果.
		 */
		public EngineInfo getResult() {
			return result;
		}

	}

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(CreateEngineDialog.class);
	/**
	 * 出力先ディレクトリ.
	 */
	private Text textOutputDir;
	/**
	 * 出力先ディレクトリWritableValue.
	 */
	private WritableValue wvOutputDir = new WritableValue("", String.class);
	/**
	 * JSLintラジオボタン.
	 */
	private Button btnRadioJSLint;
	/**
	 * JSLint選択WritableValue.
	 */
	private WritableValue wvJslint = new WritableValue(true, Boolean.class);
	/**
	 * JSHint選択ラジオボタン.
	 */
	private Button btnRadioJSHint;
	/**
	 * JSHint選択WritableValue.
	 */
	private WritableValue wvJshint = new WritableValue(false, Boolean.class);
	/**
	 * 選択プロジェクト.
	 */
	private IProject project;
	/**
	 * エンジンファイルパス.
	 */
	private String engineFilePath;
	/**
	 * ダイアログタイトル.
	 */
	private String title;

	/**
	 * コンストラクタ.
	 * 
	 * @param parentShell 親シェル.
	 * @param project 選択プロジェクト.
	 * @param title ダイアログタイトル.
	 */
	public CreateEngineDialog(Shell parentShell, IProject project, String title) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.project = project;
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
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.DL0023.getText());
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite baseComposite = new Composite(container, SWT.NONE);
		GridLayout glBaseComposite = new GridLayout(2, false);
		baseComposite.setLayout(glBaseComposite);
		baseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label lblType = new Label(baseComposite, SWT.NONE);
		lblType.setText(Messages.DL0024.getText());

		Composite compositeSelectionType = new Composite(baseComposite, SWT.NONE);
		compositeSelectionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout glCompositeSelectionType = new GridLayout(2, false);
		glCompositeSelectionType.horizontalSpacing = 20;
		compositeSelectionType.setLayout(glCompositeSelectionType);

		btnRadioJSLint = new Button(compositeSelectionType, SWT.RADIO);
		btnRadioJSLint.setText(Messages.DL0025.getText());

		btnRadioJSHint = new Button(compositeSelectionType, SWT.RADIO);
		btnRadioJSHint.setText(Messages.DL0026.getText());

		Label labelOutputDir = new Label(baseComposite, SWT.NONE);
		labelOutputDir.setBounds(0, 0, 61, 18);
		labelOutputDir.setText(Messages.DL0020.getText());

		Composite compositeOutputDir = new Composite(baseComposite, SWT.NONE);
		compositeOutputDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeOutputDir.setLayout(new GridLayout(2, false));

		textOutputDir = new Text(compositeOutputDir, SWT.BORDER);
		textOutputDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnSelectionOutputDir = new Button(compositeOutputDir, SWT.NONE);
		GridData gdBtnSelectionOutputDir = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdBtnSelectionOutputDir.widthHint = 60;
		btnSelectionOutputDir.setLayoutData(gdBtnSelectionOutputDir);
		btnSelectionOutputDir.setText(Messages.B0001.getText());
		btnSelectionOutputDir.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), Messages.DT0008.getText(),
						Messages.DL0022.getText());
				dialog.setInitialSelection(project);
				if (dialog.open() != Window.OK) {
					return;
				}
				IContainer outputDir = (IContainer) dialog.getFirstResult();
				textOutputDir.setText(outputDir.getFullPath().toString());

			}
		});

		return area;
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
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		initialDataBinding();
		setMessage(Messages.DL0027.getText(), IMessageProvider.INFORMATION);
	}

	/**
	 * データバインド初期化.
	 */
	private void initialDataBinding() {

		DataBindingContext context = new DataBindingContext();

		// Validator
		MultiValidator validator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				StringBuilder sb = new StringBuilder();

				if ((Boolean) wvJshint.getValue() && (Boolean) wvJslint.getValue()) {
					sb.append(Messages.EM0014.getText());
				}
				String outputPath = (String) wvOutputDir.getValue();
				if (StringUtils.isEmpty(outputPath)) {
					sb.append(Messages.EM0009.format(Messages.DL0020.getText()));
				} else {
					IPath path = new Path(outputPath);
					if (!ResourcesPlugin.getWorkspace().getRoot().exists(path)) {
						sb.append(Messages.EM0000.format(Messages.DL0020.getText()));
					}
				}

				if (StringUtils.isEmpty(sb.toString())) {
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					return ValidationStatus.info(Messages.DL0027.getText());
				}

				if (getButton(IDialogConstants.OK_ID) != null) {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
				return ValidationStatus.error(sb.toString());
			}
		};

		// 出力先ディレクトリのバインド
		IObservableValue obsOutputDirText = SWTObservables.observeText(textOutputDir, SWT.Modify);
		context.bindValue(obsOutputDirText, wvOutputDir, null, null);

		// JSLint選択のバインド
		IObservableValue obsSelectJslint = WidgetProperties.selection().observe(btnRadioJSLint);
		context.bindValue(obsSelectJslint, wvJslint, null, null);

		// JSHint選択のバインド
		IObservableValue obsSelectJsHint = WidgetProperties.selection().observe(btnRadioJSHint);
		context.bindValue(obsSelectJsHint, wvJshint);

		// バリデータのセッティング
		context.addValidationStatusProvider(validator);
		TitleAreaDialogSupport.create(this, context);

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(482, 271);
	}

	@Override
	protected void okPressed() {
		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
		DownloadEngineSupport support = createEngineDownload((Boolean) wvJslint.getValue());
		try {
			IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(new Path(wvOutputDir.getValue() + "/" + support.getEngine().getFileName()));
			if (file.exists()) {
				MessageBox box = new MessageBox(getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
				box.setMessage(Messages.DM0004.getText());
				box.setText(Messages.DT0011.getText());
				if (box.open() == SWT.CANCEL) {
					return;
				}
			}
			DownloadRunnable progress = new DownloadRunnable(support);
			progressDialog.run(true, false, progress);
			EngineInfo info = progress.getResult();
			ConfirmLicenseDialog dialog = new ConfirmLicenseDialog(getShell(), StringUtils.trim(info.getLicenseStr()),
					Messages.DT0009.getText());

			if (dialog.open() == IDialogConstants.OK_ID) {
				if (file.exists()) {
					file.setContents(new ByteArrayInputStream(info.getMainSource().getBytes()), IResource.FORCE, null);
				} else {
					file.create(new ByteArrayInputStream(info.getMainSource().getBytes()), true, null);
				}
				this.engineFilePath = file.getFullPath().toString();
			} else {
				return;
			}
		} catch (InvocationTargetException e) {
			ErrorDialog.openError(getShell(), Messages.DT0003.getText(), Messages.EM0015.getText(), new Status(
					IStatus.ERROR, JSLintPlugin.PLUGIN_ID, e.getMessage(), e));
			logger.put(Messages.EM0100, e);
		} catch (InterruptedException e) {
			// キャンセル無視しているので来ない.
			throw new AssertionError();
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), Messages.DT0003.getText(), Messages.EM0016.getText(), e.getStatus());
			logger.put(Messages.EM0100, e);
		}

		super.okPressed();
	}

	/**
	 * Download支援クラスを取得する.
	 * 
	 * @See {@link DownloadJSHintSupport}
	 * @See {@link DownloadJSLintSupport}
	 * 
	 * @param isJslint JSLintかどうか.
	 * @return Download支援クラス.
	 */
	private DownloadEngineSupport createEngineDownload(boolean isJslint) {
		if (isJslint) {
			return new DownloadJSLintSupport();
		}
		return new DownloadJSHintSupport();
	}

	/**
	 * ダウンロードしファイル生成後のエンジンファイルのパス.
	 * 
	 * @return エンジンファイルのパス.
	 */
	public String getEngineFilePath() {
		return engineFilePath;
	}
}
