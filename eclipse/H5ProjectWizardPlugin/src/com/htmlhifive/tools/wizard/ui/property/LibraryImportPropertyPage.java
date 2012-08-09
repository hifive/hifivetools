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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.download.DownloadModule;
import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.ResultStatus;
import com.htmlhifive.tools.wizard.log.messages.Messages;
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

		return container;
	}

	@Override
	public void setVisible(boolean visible) {

		logger.log(Messages.TR0021, getClass().getSimpleName(), "setVisible");

		if (visible) {
			// 初期化.
			IJavaScriptProject jsProject = getJavaScriptProject();
			if (jsProject != null) {
				container.initialize(jsProject, jsProject.getProject().getName(), null);
			} else {
				H5LogUtils.putLog(null, Messages.SE0023, "JavaScriptProject is null");
			}
		}

		// TODO 自動生成されたメソッド・スタブ
		super.setVisible(visible);
	}

	/**
	 * 初期化.
	 * 
	 * @param jsProject プロジェクト
	 * @param projectName プロジェクト名
	 * @param defaultInstallPath 初期インストール場所
	 * @return 変更あり
	 */
	public boolean initialize(IJavaScriptProject jsProject, String projectName, String defaultInstallPath) {

		logger.log(Messages.TR0021, getClass().getSimpleName(), "initialize");

		if (isControlCreated()) {
			// 初期化.
			if (jsProject != null) {
				return container.initialize(jsProject, null, null);
			} else {
				H5LogUtils.putLog(null, Messages.SE0023, "JavaScriptProject is null");
			}
		}
		return false;
	}

	/**
	 * JavaScriptProjectを取得する.
	 * 
	 * @return JavaScriptProject
	 */
	private IJavaScriptProject getJavaScriptProject() {

		//logger.debug("getElement().getClass(): " + getElement().getClass());
		//logger.debug("getElement().getAdapter(IJavaScriptElement.class): " + getElement().getAdapter(IJavaScriptElement.class));

		IAdaptable adaptable = getElement();
		if (adaptable != null) {
			if (adaptable.getAdapter(IJavaScriptElement.class) != null) {
				return ((IJavaScriptElement) adaptable.getAdapter(IJavaScriptElement.class)).getJavaScriptProject();
			}
		}
		return null;
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
		if (!getApplyButton().isEnabled()) { // 変更チェック.
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
}
