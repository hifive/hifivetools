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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

import com.htmlhifive.tools.wizard.ui.page.ConfirmLicensePage;

/**
 * ライセンス確認ウィザード.
 * 
 * @author fkubo
 */
public class ConfirmLicenseWizard extends Wizard {

	/** ライセンス確認ページ. */
	private ConfirmLicensePage confirmLicensePage;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {

		confirmLicensePage = new ConfirmLicensePage("confirmLicensePage");
		addPage(confirmLicensePage);

		setNeedsProgressMonitor(false);

	}

	@Override
	public void createPageControls(Composite pageContainer) {

		// TODO 自動生成されたメソッド・スタブ
		super.createPageControls(pageContainer);

		confirmLicensePage.setLiceseContents();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {

		// ここでは処理せず終了.

		return true;
	}

}
