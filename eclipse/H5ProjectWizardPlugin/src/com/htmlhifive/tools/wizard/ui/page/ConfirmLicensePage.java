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
package com.htmlhifive.tools.wizard.ui.page;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.htmlhifive.tools.wizard.ui.UIMessages;

/**
 * <H3>ライセンス確認ページ.</H3>
 * 
 * @author fkubo
 */
public class ConfirmLicensePage extends WizardPage {

	/** container. */
	ConfirmLicenseComposite container;

	/**
	 * コンストラクタ.
	 * 
	 * @param pageName ページ名
	 */
	public ConfirmLicensePage(String pageName) {

		super(pageName);
		setMessage(UIMessages.ConfirmLicensePage_this_message);
		setTitle(UIMessages.LicenseListPage_this_title);
		setPageComplete(false); // 別に設定不要なので
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {

		container = new ConfirmLicenseComposite(parent, SWT.NONE);
		setControl(container);
		// イベント通知受付.
		container.addListener(SWT.ERROR_UNSPECIFIED, new Listener() {

			@Override
			public void handleEvent(Event event) {

				setPageComplete(event.doit);
			}
		});

	}

	/**
	 * ライセンスコンテンツを設定する.
	 */
	public void setLiceseContents() {

		if (isControlCreated()) {
			container.setLiceseContents();
		}
	}

	/**
	 * カテゴリをクリアする
	 */
	public void clearCategory() {

		if (isControlCreated()) {
			container.clearCategory();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {

		return false; // 次の画面(JS関連)は見せない.
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	@Override
	public boolean isPageComplete() {

		return container.isAccepted();
	}

}
