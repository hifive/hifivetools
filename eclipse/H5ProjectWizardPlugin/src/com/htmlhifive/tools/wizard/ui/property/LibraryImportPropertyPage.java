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
		container.initialize(getJavaScriptProject());
	}

	@Override
	public void setValid(boolean b) {

		// TODO 自動生成されたメソッド・スタブ
		super.setValid(b);
	}

	@Override
	protected void performDefaults() {

		container.refreshTreeLibrary(false, true);
		// getApplyButton().setSelection(false);
		// super.performDefaults();
	}

	@Override
	public boolean performCancel() {

		// ライセンス確認が必要かどうかを判定する.
		boolean needRun = false;
		for (LibraryNode libraryNode : H5WizardPlugin.getInstance().getSelectedLibrarySet()) {
			if (libraryNode.isSelected()) {
				needRun = true;
				break;
			}
		}
		if (needRun) {
			if (!MessageDialog.openConfirm(null, Messages.SE0111.format(), Messages.SE0112.format())) {
				return false;
			}
		}
		container.refreshTreeLibrary(false, true);

		return super.performCancel();
	}

	@Override
	protected void performApply() {

		super.performApply();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		// ライセンス確認が必要かどうかを判定する.
		boolean needRun = false;
		boolean needConfirmDialog = false;
		for (LibraryNode libraryNode : H5WizardPlugin.getInstance().getSelectedLibrarySet()) {
			if (libraryNode.isSelected()) {
				needRun = true;
			}
			if (libraryNode.isAddable()) {
				// TODO:とりあえず、追加するものがあれば表示ありとする(インストール済の場合は考慮しない)
				needConfirmDialog = true;
			}
		}
		if (!needRun) {
			return true;
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
			// We were cancelled...
			final IProject proj = getProject();
			if (proj != null && proj.exists()) {
				try {
					proj.delete(true, true, null);
				} catch (CoreException ex) {
					H5LogUtils.putLog(ex, Messages.SE0026);
				}
			}
		} finally {
			// 結果表示.
			logger.showDialog();
		}

		return logger.isSuccess();
	}

	/**
	 * プロジェクトを取得する.
	 * 
	 * @return プロジェクト.
	 */
	private IProject getProject() {

		IAdaptable adapt = getElement();

		// IAdaptable adaptable= getElement();
		if (adapt instanceof IJavaScriptProject) {
			return (IProject) adapt.getAdapter(IProject.class);
		}
		if (adapt instanceof IProject) {
			return (IProject) adapt;
		}
		return null;

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
					DownloadModule downloadModule = new DownloadModule();
					downloadModule.downloadLibrary(monitor, logger, H5WizardPlugin.getInstance()
							.getSelectedLibrarySet(), jsProject.getProject(), null);

					// ワークスペースとの同期.
					jsProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
					// SE0104=INFO,ワークスペースを更新しました。
					logger.log(Messages.SE0104);
					monitor.worked(1);

					// ファイルの存在チェック更新.
					container.refreshTreeLibrary(false, false);
					// SE0103=INFO,ライブラリの状態を最新化しました。
					logger.log(Messages.SE0103);

				} catch (OperationCanceledException e) {
					throw new InterruptedException();
				} catch (CoreException e) {
					// SE0023=ERROR,予期しない例外が発生しました。
					logger.log(e, Messages.SE0023);

					H5LogUtils.putLog(e, Messages.SE0044);
				} finally {
					monitor.done();
				}
			}
		};

	}
}
