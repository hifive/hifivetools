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
import org.eclipse.core.net.proxy.IProxyService;
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
import org.osgi.util.tracker.ServiceTracker;

import com.htmlhifive.tools.jslint.JSLintPlugin;
import com.htmlhifive.tools.jslint.engine.download.DownloadEngineSupport;
import com.htmlhifive.tools.jslint.engine.download.DownloadJSHintSupport;
import com.htmlhifive.tools.jslint.engine.download.DownloadJSLintSupport;
import com.htmlhifive.tools.jslint.engine.download.EngineInfo;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

public class CreateEngineDialog extends TitleAreaDialog {
	private static class DownloadRunnable implements IRunnableWithProgress {

		private static JSLintPluginLogger logger = JSLintPluginLoggerFactory
				.getLogger(CreateEngineDialog.DownloadRunnable.class);

		private EngineInfo result;

		private DownloadEngineSupport support;

		public DownloadRunnable(DownloadEngineSupport support) {
			this.support = support;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

			try {
				result = support.getEngineInfo(monitor);
			} catch (IOException e) {
				throw new InvocationTargetException(e, e.getMessage());
			}
		}

		public EngineInfo getResult() {
			return result;
		}

	}

	private final ServiceTracker<IProxyService, IProxyService> proxyTracker;
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(CreateEngineDialog.class);
	private Text textOutputDir;
	private WritableValue wvOutputDir = new WritableValue("", String.class);
	private Button btnRadioJSLint;
	private WritableValue wvJslint = new WritableValue(true, Boolean.class);
	private Button btnRadioJSHint;
	private WritableValue wvJshint = new WritableValue(false, Boolean.class);
	private IProject project;
	private String engineFilePath;
	private String title;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 * @param title
	 */
	public CreateEngineDialog(Shell parentShell, IProject project, String title) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.proxyTracker = new ServiceTracker<IProxyService, IProxyService>(JSLintPlugin.getDefault().getBundle()
				.getBundleContext(), IProxyService.class, null);
		proxyTracker.open();
		this.project = project;
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
		setTitle("JSLint.js又はJSHint.jsの取得");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite baseComposite = new Composite(container, SWT.NONE);
		GridLayout glBaseComposite = new GridLayout(2, false);
		baseComposite.setLayout(glBaseComposite);
		baseComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label lblType = new Label(baseComposite, SWT.NONE);
		lblType.setText("タイプ種別");

		Composite compositeSelectionType = new Composite(baseComposite, SWT.NONE);
		compositeSelectionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout glCompositeSelectionType = new GridLayout(2, false);
		glCompositeSelectionType.horizontalSpacing = 20;
		compositeSelectionType.setLayout(glCompositeSelectionType);

		btnRadioJSLint = new Button(compositeSelectionType, SWT.RADIO);
		btnRadioJSLint.setText("JSLint");

		btnRadioJSHint = new Button(compositeSelectionType, SWT.RADIO);
		btnRadioJSHint.setText("JSHint");

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
				if (dialog.open() != Window.OK) {
					return;
				}
				IContainer outputDir = (IContainer) dialog.getFirstResult();
				textOutputDir.setText(outputDir.getFullPath().toString());

			}
		});

		return area;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		initialDataBinding();
		setMessage("種別と出力先ディレクトリを指定しJSLintファイルを取得します。", IMessageProvider.INFORMATION);
	}

	private void initialDataBinding() {

		DataBindingContext context = new DataBindingContext();

		// Validator
		MultiValidator validator = new MultiValidator() {

			@Override
			protected IStatus validate() {
				StringBuilder sb = new StringBuilder();

				if ((Boolean) wvJshint.getValue() && (Boolean) wvJslint.getValue()) {
					sb.append("どちらか選択してください。");
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
					return ValidationStatus.info("種別と出力先ディレクトリを指定しJSLintファイルを取得します。");
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

	/**
	 * Return the initial size of the dialog.
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
				box.setMessage("既にファイルが存在しています。上書きしますか？");
				if (box.open() == SWT.CANCEL) {
					return;
				}
			}
			DownloadRunnable progress = new DownloadRunnable(support);
			progressDialog.run(true, false, progress);
			EngineInfo info = progress.getResult();
			ConfirmLicenseDialog dialog = new ConfirmLicenseDialog(getShell(), StringUtils.trim(info.getLicenseStr()),
					"ライセンスの確認");

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
			ErrorDialog.openError(getShell(), "エラーダイアログ", "情報取得中に例外が発生しました", new Status(IStatus.ERROR,
					JSLintPlugin.PLUGIN_ID, e.getMessage(), e));
			logger.put(Messages.EM0100, e);
		} catch (InterruptedException e) {
			// キャンセル無視しているので来ない.
			throw new AssertionError();
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), "エラーダイアログ", "ファイル出力中に例外が発生しました", e.getStatus());
			logger.put(Messages.EM0100, e);
		}

		super.okPressed();
	}

	private DownloadEngineSupport createEngineDownload(boolean isJslint) {
		if (isJslint) {
			return new DownloadJSLintSupport();
		}
		return new DownloadJSHintSupport();
	}

	public Button getBtnRadioJSLint() {
		return btnRadioJSLint;
	}

	public Button getBtnRadioJSHint() {
		return btnRadioJSHint;
	}

	public String getEngineFilePath() {
		return engineFilePath;
	}
}
