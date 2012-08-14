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
 */
package com.htmlhifive.tools.wizard.ui.property;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.JavaScriptCore;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.download.DownloadModule;
import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.ResultStatus;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.ProjectCreationWizard;
import com.htmlhifive.tools.wizard.ui.UIEventHelper;
import com.htmlhifive.tools.wizard.ui.page.LibraryImportComposite;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.utils.H5LogUtils;

/**
 * <H3>ライブラリインポートプロパティページ.</H3>
 * 
 * @author fkubo
 */
public class LibraryImportPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	/** ロガー. */
	private static PluginLogger logger = PluginLoggerFactory.getLogger(LibraryImportPropertyPage.class);

	/** container. */
	LibraryImportComposite container;

	/**
	 * デフォルトコンストラクタ.
	 */
	public LibraryImportPropertyPage() {

		super();

		logger.log(Messages.TR0021, getClass().getName(), "<init>");

		// setMessage(UIMessages.WizardPropertyPage_this_message);
		// setTitle(UIMessages.WizardPropertyPage_this_title);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		logger.log(Messages.TR0021, getClass().getName(), "createContents");

		container = new LibraryImportComposite(parent, SWT.NONE);

		// 下からのメッセージを受ける.
		container.addListener(UIEventHelper.SET_MESSAGE, new Listener() {

			@Override
			public void handleEvent(Event event) {

				// メッセージを設定.
				setErrorMessage(event.text); // WizardPage

				setValid(event.text == null);

				getContainer().updateButtons();
			}
		});
		// チェックボックス変更時.
		container.addListener(UIEventHelper.TABLE_SELECTION_CHANGE, new Listener() {

			@Override
			public void handleEvent(Event event) {

				if (event.item instanceof Table) { // 一応.
					boolean enabled = ((Table) event.item).getItemCount() > 0;

					getDefaultsButton().setEnabled(enabled);
					getApplyButton().setEnabled(enabled);
				}

				getContainer().updateButtons();
			}
		});
		container.addListener(UIEventHelper.LIST_RELOAD, new Listener() {

			@Override
			public void handleEvent(Event event) {

				// 初期化.
				IJavaScriptProject jsProject = getJavaScriptProject();
				if (jsProject != null) {
					container.initialize(jsProject, jsProject.getProject().getName(), null, true, true);
				} else {
					//if (getElement() instanceof IProject) {
					//container.initialize(null, ((IProject) getElement()).getName(), null, true);
					//}
					container.initialize(null, null, null, true, true);

					H5LogUtils.putLog(null, Messages.SE0023, "JavaScriptProject is null");
				}
			}
		});

		return container;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		super.createControl(parent);
		if (getContainer() instanceof IPageChangeProvider) {
			((IPageChangeProvider) getContainer()).addPageChangedListener(new IPageChangedListener() {

				@Override
				public void pageChanged(PageChangedEvent event) {

					if (event.getSelectedPage() == LibraryImportPropertyPage.this) {
						if (getJavaScriptProject() == null) {
							setVisible(false);
							if (addNature((IProject) getElement().getAdapter(IProject.class), JavaScriptCore.NATURE_ID)) {
								MessageDialog.openInformation(getShell(), Messages.SE0121.format(),
										Messages.SE0122.format());
								// 閉じる.
								if (getContainer() instanceof PreferenceDialog) {
									((PreferenceDialog) getContainer()).close();
								}
							}
							return;
						}
						setVisible(true);
					}
				}
			});
		}

		// 初期化.
		IJavaScriptProject jsProject = getJavaScriptProject();
		if (jsProject != null) {
			container.initialize(jsProject, jsProject.getProject().getName(), null, false, true);
		} else {
			H5LogUtils.putLog(null, Messages.SE0023, "JavaScriptProject is null");
		}

	}

	/**
	 * JavaScriptProjectを取得する.
	 * 
	 * @return JavaScriptProject
	 */
	private IJavaScriptProject getJavaScriptProject() {

		IAdaptable adaptable = getElement();
		if (adaptable != null) {
			IProject project = (IProject) adaptable.getAdapter(IProject.class);
			IJavaScriptProject jsProject = null;
			try {
				jsProject = (IJavaScriptProject) project.getNature(JavaScriptCore.NATURE_ID);
			} catch (CoreException e) {
				// 無視.
			}
			if (jsProject != null) {
				return jsProject;
			}
		}
		return null;
	}

	/**
	 * Natureの存在チェック.
	 * 
	 * @param project プロジェクト
	 * @param natureId NatureID
	 * @return 変更したかどうか.
	 */
	private boolean addNature(IProject project, String natureId) {
		// JSNatureを追加する.
		if (MessageDialog.openQuestion(getShell(), Messages.SE0119.format(), Messages.SE0120.format(getTitle()))) {
			// ロガーを生成.
			final ResultStatus logger = new ResultStatus();

			try {
				// ライブラリダウンロード.

				final IRunnableWithProgress downloadRunnable = getAddNatureRunnnable(logger, project, natureId);

				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				dialog.run(false, false, downloadRunnable);

				// 実行したので一度閉じる.
				return true;
			} catch (InvocationTargetException e) {
				final Throwable ex = e.getTargetException();

				H5LogUtils.putLog(ex, Messages.SE0025);

			} catch (InterruptedException e) {
				logger.setInterrupted(true);
				// We were cancelled...
				//removeProject(logger);

			} finally {
				//					// 結果表示.
				//					logger.showDialog(Messages.PI0138);
				if (logger.isSuccess()) {
					return true;
				}

				// ファイルの存在チェック更新(チェックを戻す).
				container.refreshTreeLibrary(false, true);
				// SE0103=INFO,ライブラリの状態を最新化しました。
				logger.log(Messages.SE0103);
			}

		}
		return false;
	}

	@Override
	protected void performDefaults() {

		logger.log(Messages.TR0021, getClass().getName(), "performDefaults");

		container.refreshTreeLibrary(false, true);

	}

	@Override
	public boolean okToLeave() {

		logger.log(Messages.TR0021, getClass().getName(), "okToLeave");

		// 変更が必要かを判定する.
		if (getJavaScriptProject() == null || !getApplyButton().isEnabled()
				|| H5WizardPlugin.getInstance().getSelectedLibrarySet().isEmpty()) { // 変更チェック.
			return super.okToLeave();
		}

		// 確認
		if (!MessageDialog.openConfirm(null, Messages.SE0111.format(), Messages.SE0112.format())) {
			return false;
		}

		// 変更を戻す
		getDefaultsButton().setEnabled(false);
		getApplyButton().setEnabled(false);

		return true;
	}

	@Override
	public boolean performCancel() {

		logger.log(Messages.TR0021, getClass().getName(), "performCancel");

		if (!getApplyButton().isEnabled()) { // 変更チェック.
			return super.performCancel();
		}
		// FIXME: Eclipse 3.7 だと正しく動作しないのでコメントアウト
		//		if (!MessageDialog.openConfirm(null, Messages.SE0111.format(), Messages.SE0112.format())) {
		//			return false;
		//		}
		//
		//		// 変更を戻す
		//		performDefaults();

		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		logger.log(Messages.TR0021, getClass().getName(), "performOk");

		if (!getApplyButton().isEnabled()) { // 変更チェック.
			return true;
		}

		// ライセンス確認が必要かどうかを判定する.
		boolean needConfirmDialog = false;
		for (LibraryNode libraryNode : H5WizardPlugin.getInstance().getSelectedLibrarySet()) {
			if (libraryNode.isNeedConfirmDialog()) {
				needConfirmDialog = true;
			}
		}

		// ライセンスダイアログの表示.
		if (needConfirmDialog) {
			WizardDialog confirmLicenseWizardDialog = new WizardDialog(getShell(), new ConfirmLicenseWizard());
			confirmLicenseWizardDialog.setPageSize(getShell().getSize()); // 元と同じサイズで表示.
			int ret = confirmLicenseWizardDialog.open();
			if (ret == SWT.ERROR) {
				// キャンセル.
				return false;
			}
		}

		// ロガーを生成.
		final ResultStatus logger = new ResultStatus();

		try {
			// ライブラリダウンロード.
			final IRunnableWithProgress downloadRunnable = getDownloadRunnnable(logger);

			ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
			dialog.run(false, false, downloadRunnable);

		} catch (InvocationTargetException e) {
			final Throwable ex = e.getTargetException();

			H5LogUtils.putLog(ex, Messages.SE0025);

		} catch (InterruptedException e) {
			logger.setInterrupted(true);
			// We were cancelled...
			//removeProject(logger);

			return false;
		} finally {
			// 結果表示.
			logger.showDialog(Messages.PI0138);

			// ファイルの存在チェック更新(チェックを戻す).
			container.refreshTreeLibrary(false, true);
			// SE0103=INFO,ライブラリの状態を最新化しました。
			logger.log(Messages.SE0103);
		}

		return logger.isSuccess();
	}

	/**
	 * プロジェクト展開処理を行なうRunnable を取得.
	 * 
	 * @param logger ロガー
	 * @return プロジェクト展開処理を行なうRunnable
	 */
	private IRunnableWithProgress getDownloadRunnnable(final ResultStatus logger) {

		return new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				DownloadModule downloadModule = new DownloadModule();
				try {
					if (monitor == null) {
						// モニタを生成.
						monitor = new NullProgressMonitor();
					}

					// タスクを開始.
					monitor.beginTask(Messages.PI0103.format(), 10000);

					IJavaScriptProject jsProject = getJavaScriptProject();

					// 現在のデフォルトインストール先を取得する.

					// ダウンロードの実行
					downloadModule.downloadLibrary(monitor, 8000, logger, H5WizardPlugin.getInstance()
							.getSelectedLibrarySortedSet(), jsProject.getProject()); // 8000

					// ワークスペースとの同期.
					jsProject.getProject().refreshLocal(IResource.DEPTH_ONE, monitor);

					// SE0104=INFO,ワークスペースを更新しました。
					logger.log(Messages.SE0104);
					monitor.subTask(Messages.SE0104.format());
					monitor.worked(2000);

				} catch (OperationCanceledException e) {
					// 処理手動停止.
					throw new InterruptedException(e.getMessage());
				} catch (CoreException e) {
					// SE0023=ERROR,予期しない例外が発生しました。
					logger.log(e, Messages.SE0023, "");
					throw new InvocationTargetException(e, Messages.SE0023.format());
				} finally {
					downloadModule.close();
					monitor.done();
				}
			}
		};

	}

	/**
	 * Nature追加処理を行なうRunnable を取得.
	 * 
	 * @param logger ロガー
	 * @return Nature追加処理を行なうRunnable
	 */
	private static IRunnableWithProgress getAddNatureRunnnable(final ResultStatus logger, final IProject project,
			final String natureId) {

		return new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					// SE0065=INFO,Nature{0}を追加します。
					logger.log(Messages.SE0065, natureId);

					ProjectCreationWizard.addNature(project, monitor, natureId);

					// SE0066=INFO,Nature{0}を追加しました。
					logger.log(Messages.SE0066, natureId);
				} catch (CoreException e) {

					// 失敗にはしない.
					// SE0067=INFO,Nature{0}追加に失敗しました。
					logger.logIgnoreSetSuccess(e, Messages.SE0067, natureId);

					// SE0031=ERROR,プラグインがインストールされていない可能性があります。name={0}, natureId={1}
					//H5LogUtils.putLog(e, Messages.SE0031, nature.getName(), nature.getId());
					H5LogUtils.showLog(e, Messages.SE0032, Messages.SE0031, "", natureId);
				}

			}
		};
	}

}
