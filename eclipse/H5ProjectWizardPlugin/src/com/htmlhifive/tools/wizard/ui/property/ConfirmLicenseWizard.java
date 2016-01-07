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

import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.page.ConfirmLicensePage;

/**
 * ライセンス確認ウィザード.
 * 
 * @author fkubo
 */
public class ConfirmLicenseWizard extends Wizard {
	/** ロガー. */
	private static PluginLogger logger = PluginLoggerFactory.getLogger(ConfirmLicenseWizard.class);

	/** ライセンス確認ページ. */
	private ConfirmLicensePage confirmLicensePage;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {

		logger.log(Messages.TR0031, getClass().getSimpleName(), "addPages");

		confirmLicensePage = new ConfirmLicensePage("confirmLicensePage");
		addPage(confirmLicensePage);

		setNeedsProgressMonitor(false);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {

		logger.log(Messages.TR0031, getClass().getSimpleName(), "performFinish");

		// ここでは処理せず終了.

		return true;
	}

}
