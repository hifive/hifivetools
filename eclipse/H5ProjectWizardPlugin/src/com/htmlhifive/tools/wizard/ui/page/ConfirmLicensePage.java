/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
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
package com.htmlhifive.tools.wizard.ui.page;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.UIEventHelper;
import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * <H3>ライセンス確認ページ.</H3>
 * 
 * @author fkubo
 */
public class ConfirmLicensePage extends WizardPage {
	/** ロガー. */
	private static PluginLogger logger = PluginLoggerFactory.getLogger(ConfirmLicensePage.class);

	/** container. */
	ConfirmLicenseComposite container;

	/**
	 * コンストラクタ.
	 * 
	 * @param pageName ページ名
	 */
	public ConfirmLicensePage(String pageName) {

		super(pageName);

		logger.log(Messages.TR0011, getClass().getSimpleName(), "<init>");

		setMessage(UIMessages.ConfirmLicensePage_this_message);
		setTitle(UIMessages.LicenseListPage_this_title);
		//setPageComplete(false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		logger.log(Messages.TR0011, getClass().getSimpleName(), "createControl");

		container = new ConfirmLicenseComposite(parent, SWT.NONE);
		setControl(container);

		// 下からのメッセージを受ける.
		container.addListener(UIEventHelper.SET_PAGE_COMPLETE, new Listener() {

			@Override
			public void handleEvent(Event event) {

				setPageComplete(event.doit);
			}
		});

		// ページ初期表示時の処理.
		((IPageChangeProvider) getContainer()).addPageChangedListener(new IPageChangedListener() {
			@Override
			public void pageChanged(PageChangedEvent event) {

				if (event.getSelectedPage() == ConfirmLicensePage.this && event.getSource() == getContainer()) {

					setLiceseContents();

				}
			}
		});

		// ページ切替時の処理.
		((WizardDialog) getContainer()).addPageChangingListener(new IPageChangingListener() {

			@Override
			public void handlePageChanging(PageChangingEvent event) {

				//				// 画面表示時.
				//				if (!initFlag && event.getCurrentPage() != getNextPage()
				//						&& event.getTargetPage() == StructureSelectPage.this) {
				//					// 初期プロジェクト名.
				//					container.setProjectName("hifive-web");
				//
				//					// チェック.
				//					container.setInputComboZip();
				//					initFlag = true;
				//					return;
				//				}

				// 次のページ遷移時.
				//if (event.getCurrentPage() == ConfirmLicensePage.this && event.getTargetPage() == getPreviousPage()) {
				if (event.getCurrentPage() == ConfirmLicensePage.this) {
					container.rejected();
					container.setLiceseContents();
				}

			}
		});

	}

	/**
	 * ライセンスコンテンツを設定する.
	 * 
	 * @param isWizard ウィザードかプロパティページからか.
	 */
	public void setLiceseContents() {

		logger.log(Messages.TR0011, getClass().getSimpleName(), "setLiceseContents");

		container.setLiceseContents();
	}

	/**
	 * カテゴリをクリアする
	 */
	public void clearCategory() {

		logger.log(Messages.TR0011, getClass().getSimpleName(), "clearCategory");

		container.clearCategory();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {

		logger.log(Messages.TR0011, getClass().getSimpleName(), "canFlipToNextPage");

		return false; // 次の画面(JS関連)は見せない.
	}
}
