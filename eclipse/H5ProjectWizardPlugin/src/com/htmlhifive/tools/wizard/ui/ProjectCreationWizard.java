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
package com.htmlhifive.tools.wizard.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.jsdt.internal.ui.wizards.JavaProjectWizard;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.download.DownloadModule;
import com.htmlhifive.tools.wizard.library.xml.BaseProject;
import com.htmlhifive.tools.wizard.library.xml.Nature;
import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.ResultStatus;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.page.ConfirmLicensePage;
import com.htmlhifive.tools.wizard.ui.page.LibraryImportPage;
import com.htmlhifive.tools.wizard.ui.page.StructureSelectPage;
import com.htmlhifive.tools.wizard.utils.H5IOUtils;
import com.htmlhifive.tools.wizard.utils.H5LogUtils;

/**
 * hifive プロジェクト作成ウィザード.
 * 
 * @author fkubo
 */
// public class ProjectCreationWizard extends BasicNewResourceWizard {
public class ProjectCreationWizard extends JavaProjectWizard {
	/** ロガー. */
	private static PluginLogger logger = PluginLoggerFactory.getLogger(ProjectCreationWizard.class);

	/**
	 * メインページ.
	 */
	// private WizardNewProjectCreationPage mainPage;

	/** ライブラリインポートページ. */
	private LibraryImportPage libraryImportPage;

	/** 構造決定ページ. */
	private StructureSelectPage structureSelectPage;

	/** ライセンス確認ページ. */
	private ConfirmLicensePage confirmLicensePage;

	// /** 結果ページ. */
	// private ResultPage resultPage;

	/** ライブラリダウンロード. */
	private final DownloadModule downloadModule;

	/**
	 * コンストラクタ.
	 */
	public ProjectCreationWizard() {

		super();

		downloadModule = new DownloadModule();
	}

	@Override
	public void dispose() {

		downloadModule.close();
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {

		logger.log(Messages.TR0031, getClass().getSimpleName(), "addPages");

		structureSelectPage = new StructureSelectPage("structureSelectPage");
		addPage(structureSelectPage);

		libraryImportPage = new LibraryImportPage("libraryImportPage");
		addPage(libraryImportPage);

		confirmLicensePage = new ConfirmLicensePage("confirmLicensePage");
		addPage(confirmLicensePage);

		// first, second ページを追加.
		super.addPages();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {

		logger.log(Messages.TR0031, getClass().getSimpleName(), "performFinish");

		// 結果ページに移動.
		// getContainer().showPage(resultPage);

		if (!super.performFinish()) {
			// 親でエラー発生.;
			return false;
		}

		final ResultStatus logger = new ResultStatus();

		try {
			// プロジェクトZIP展開.
			final IRunnableWithProgress runnable = getExtractRunnnable(logger);
			getContainer().run(false, false, runnable);

		} catch (InvocationTargetException e) {
			final Throwable ex = e.getTargetException();
			// SE0023=ERROR,予期しない例外が発生しました。
			logger.log(ex, Messages.SE0023);

			// We were cancelled...
			removeProject(logger);
			return false;
		} catch (InterruptedException e) {
			logger.setInterrupted(true);

			// We were cancelled...
			removeProject(logger);
			return false;
		} finally {
			// 結果表示.
			logger.showDialog(Messages.PI0137);
		}

		// Wizardの場合は結果に関わらず終了する.
		//return logger.isSuccess();
		return true;
	}

	/**
	 * 失敗したプロジェクトを除去する.
	 * 
	 * @param logger ロガー
	 */
	private void removeProject(ResultStatus logger) {

		logger.log(Messages.TR0031, getClass().getSimpleName(), "removeProject");

		final IProject proj = structureSelectPage.getProjectHandle();
		if (proj != null && proj.exists()) {
			try {
				logger.log(Messages.SE0105, proj.getName());
				proj.delete(true, true, null);
				logger.log(Messages.SE0106, proj.getName());
			} catch (CoreException ex) {
				// SE0100=INFO,プロジェクト({0})削除処理中にエラーが発生しました。
				logger.log(ex, Messages.SE0100, proj.getName());
			}
		}
	}

	/**
	 * Natureを追加する.
	 * 
	 * @param project プロジェクト
	 * @param monitor モニタ－
	 * @param natureId NatureID
	 * @throws CoreException コア例外
	 */
	public static void addNature(IProject project, IProgressMonitor monitor, String natureId) throws CoreException {

		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (!project.hasNature(natureId)) {
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = natureId;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		}
	}

	/**
	 * プロジェクト展開処理を行なうRunnable を取得.
	 * 
	 * @return プロジェクト展開処理を行なうRunnable.
	 */
	private IRunnableWithProgress getExtractRunnnable(final ResultStatus logger) {

		return new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				if (monitor == null) {
					// モニタを生成.
					monitor = new NullProgressMonitor();
				}

				// 対象のプロジェクトを取得.
				BaseProject baseProject = structureSelectPage.getBaseProject();
				if (baseProject == null) {
					// 続行不可.
					H5LogUtils.putLog(null, Messages.SE0048);
					//H5LogUtils.showLog(null, Messages.SE0043, Messages.SE0048);
					logger.setSuccess(false);
					return;
				}

				// タスクを開始.
				monitor.beginTask(Messages.PI0101.format(), 10000);

				// nature追加, project-download, zip-extract, replace, reflesh

				// create the project
				try {
					final IProject proj = structureSelectPage.getProjectHandle();

					// 文字コード設定.
					proj.setDefaultCharset("UTF-8", monitor);

					// プロジェクトダウンロード処理(Core例外発生の可能性あり).
					downloadModule.downloadProject(monitor, 2000, logger, baseProject, proj); // 2000

					// SE0061=INFO,プロジェクト構成を作成します。
					logger.log(Messages.SE0061);
					monitor.worked(1000); // 状態変更 1000

					// 文字列置換.
					for (com.htmlhifive.tools.wizard.library.xml.File file : baseProject.getReplace().getFile()) {
						H5IOUtils.convertProjectName(getShell(), proj, file.getName());
						// SE0069=INFO,リソース({0})内のプロジェクト名を変更しました。
						logger.log(Messages.SE0069, file.getName());
					}

					// Nature追加.
					if (baseProject.getNatures() != null) {
						for (Nature nature : baseProject.getNatures().getNature()) {
							try {
								// SE0065=INFO,Nature{0}を追加します。
								logger.log(Messages.SE0065, nature.getId());

								addNature(proj, monitor, nature.getId());

								// SE0066=INFO,Nature{0}を追加しました。
								logger.log(Messages.SE0066, nature.getId());
							} catch (CoreException e) {

								// 失敗にはしない.
								// SE0067=INFO,Nature{0}追加に失敗しました。
								logger.logIgnoreSetSuccess(e, Messages.SE0067, nature.getId());

								// SE0031=ERROR,プラグインがインストールされていない可能性があります。name={0}, natureId={1}
								//H5LogUtils.putLog(e, Messages.SE0031, nature.getName(), nature.getId());
								H5LogUtils.showLog(e, Messages.SE0032, Messages.SE0031, nature.getName(),
										nature.getId());
							}
						}
					}
					monitor.worked(1000);

					// SE0062=INFO,プロジェクト構成の作成が完了しました。
					logger.log(Messages.SE0062);

					// プロジェクトを取得.
					final IProject project = structureSelectPage.getProjectHandle();

					// ダウンロードの実行
					downloadModule.downloadLibrary(monitor, 5000, logger, H5WizardPlugin.getInstance()
							.getSelectedLibrarySortedSet(), project); // 5000

					// ワークスペースとの同期.
					project.refreshLocal(IResource.DEPTH_ONE, monitor);
					// SE0104=INFO,ワークスペースを更新しました。
					logger.log(Messages.SE0104);
					monitor.worked(1000);

				} catch (OperationCanceledException e) {
					// 処理手動停止.
					throw new InterruptedException(e.getMessage());
				} catch (CoreException e) {
					// SE0023=ERROR,予期しない例外が発生しました。
					logger.log(e, Messages.SE0023);
					throw new InvocationTargetException(e, Messages.SE0023.format());
				} finally {
					monitor.done();
				}
			}
		};

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPageControls(Composite pageContainer) {

		logger.log(Messages.TR0031, getClass().getSimpleName(), "createPageControls");

		super.createPageControls(pageContainer);

		//		((WizardDialog) getContainer()).addPageChangedListener(new IPageChangedListener() {
		//
		//			@Override
		//			public void pageChanged(PageChangedEvent event) {
		//
		//				if (event.getSelectedPage() instanceof LibraryImportPage) {
		//					// プロジェクト名を設定する.
		//					((JavaProjectWizardFirstPage) getPage(NewWizardMessages.JavaProjectWizardFirstPage_page_pageName))
		//					.setName(ProjectCreationWizard.this.structureSelectPage.getProjectName());
		//				}
		//
		//				if (event.getSelectedPage() instanceof ConfirmLicensePage) {
		//					// ライセンスのタブを更新する.
		//					((ConfirmLicensePage) getPage("confirmLicensePage")).setLiceseContents();
		//				}
		//			}
		//		});
	}

}
