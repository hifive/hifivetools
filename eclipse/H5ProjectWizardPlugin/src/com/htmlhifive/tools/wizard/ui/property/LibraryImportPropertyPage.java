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
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.DownloadModule;
import com.htmlhifive.tools.wizard.ui.ResultStatus;
import com.htmlhifive.tools.wizard.ui.page.LibraryImportComposite;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.utils.H5LogUtils;

/**
 * <H3>ライブラリインポートプロパティページ.</H3>
 * 
 * @author fkubo
 */
public class LibraryImportPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	/** container. */
	LibraryImportComposite container;

	/**
	 * デフォルトコンストラクタ.
	 */
	public LibraryImportPropertyPage() {

		super();
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

		container = new LibraryImportComposite(parent, SWT.NONE);

		// イベント通知受付.
		container.addListener(SWT.CHANGED, new Listener() {

			@Override
			public void handleEvent(Event event) {

				if (event.item instanceof Table) { // 一応.
					boolean enabled = event.detail > 0;

					getDefaultsButton().setEnabled(enabled);
					getApplyButton().setEnabled(enabled);
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

		// 初期化.
		container.initialize(getJavaScriptProject(), null, null);
		setValid(true); // 常にOK
	}

	@Override
	protected void performDefaults() {

		container.refreshTreeLibrary(false, true);
	}

	@Override
	public boolean okToLeave() {
		// 変更が必要かを判定する.
		if (!getApplyButton().isEnabled()) { // 変更チェック.
			return super.okToLeave();
		}

		// TODO:無駄なダイアログが表示される
		if (!MessageDialog.openConfirm(null, Messages.SE0111.format(), Messages.SE0112.format())) {
			return false;
		}

		// 変更を戻す
		performDefaults();

		return true;
	}

	@Override
	public boolean performCancel() {

		if (!getApplyButton().isEnabled()) { // 変更チェック.
			return super.performCancel();
		}

		// 正しく動作しないのでコメントアウト
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
	 * JavaScriptProjectを取得する.
	 * 
	 * @return JavaScriptProject
	 */
	private IJavaScriptProject getJavaScriptProject() {

		if (getElement().getAdapter(IJavaScriptElement.class) != null) {
			return ((IJavaScriptElement) getElement().getAdapter(IJavaScriptElement.class)).getJavaScriptProject();
		}
		return null;
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
					monitor.beginTask(Messages.PI0103.format(), 1000);

					IJavaScriptProject jsProject = getJavaScriptProject();

					// 現在のデフォルトインストール先を取得する.

					// ダウンロードの実行
					downloadModule.downloadLibrary(monitor, logger, H5WizardPlugin.getInstance()
							.getSelectedLibrarySortedSet(), jsProject.getProject()); // 900

					// ワークスペースとの同期.
					jsProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

					// SE0104=INFO,ワークスペースを更新しました。
					logger.log(Messages.SE0104);
					monitor.subTask(Messages.SE0104.format());
					monitor.worked(100);

				} catch (OperationCanceledException e) {
					// 処理手動停止.
					throw new InterruptedException(e.getMessage());
				} catch (CoreException e) {
					// SE0023=ERROR,予期しない例外が発生しました。
					logger.log(e, Messages.SE0023);
					throw new InvocationTargetException(e, Messages.SE0023.format());
				} finally {
					downloadModule.close();
					monitor.done();
				}
			}
		};

	}
}
