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
 *
 */
package jp.co.nssol.h5.tool.jslint.dialog;

import java.util.ArrayList;
import java.util.List;

import jp.co.nssol.h5.tool.jslint.engine.option.CheckOption;
import jp.co.nssol.h5.tool.jslint.engine.option.CheckOptionFileWrapper;
import jp.co.nssol.h5.tool.jslint.engine.option.Engine;
import jp.co.nssol.h5.tool.jslint.event.CheckOptionChangeEvent;
import jp.co.nssol.h5.tool.jslint.event.CheckOptionChangeListener;
import jp.co.nssol.h5.tool.jslint.messages.Messages;
import jp.co.nssol.h5.tool.jslint.view.JslintOptionComposite;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * チェックをオプションを設定するダイアログ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class OptionSettingDialog extends Dialog {

	/**
	 * 対象プロジェクト.
	 */
	private IProject project;
	/**
	 * エラーメッセージラベル.
	 */
	private CLabel messageLabel;
	/**
	 * オプション設定コンポジット.
	 */
	private JslintOptionComposite comp;
	/**
	 * 使用エンジン.
	 */
	private Engine engine;

	/**
	 * コンストラクタ.
	 * 
	 * @param parentShell シェル
	 * @param project プロジェクト
	 * @param engine エンジン
	 */
	public OptionSettingDialog(Shell parentShell, IProject project, Engine engine) {

		super(parentShell);
		this.project = project;
		this.engine = engine;

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);
		createMessageArea(composite);

		comp = new JslintOptionComposite(composite, project);
		comp.addCheckOptionChangeListener(new CheckOptionChangeListener() {

			@Override
			public void modify(CheckOptionChangeEvent event) {

				CheckOptionFileWrapper optionFile = event.getOptionFile();
				String[] errorMessages = checkOption(optionFile);
				getButton(OK).setEnabled(errorMessages.length == 0);
				updateErrorMessage(errorMessages);

			}

		});
		GridData gdComp = new GridData(GridData.FILL_BOTH);
		gdComp.widthHint = 900;
		comp.setLayoutData(gdComp);
		return super.createDialogArea(parent);

	}

	/**
	 * エラーメッセージのエリアを生成する.
	 * 
	 * @param parent 親コンポジット.
	 * @return コンポジット.
	 */
	private Composite createMessageArea(Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// エラーメッセージ用のラベル
		messageLabel = new CLabel(composite, SWT.HORIZONTAL);
		messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		messageLabel.setText(Messages.DT0006.getText());
		// セパレータ
		Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return composite;
	}

	/**
	 * ダイアログのエラーメッセージを更新する.
	 * 
	 * @param errorMessages エラーメッセージ
	 */
	private void updateErrorMessage(String[] errorMessages) {

		if (errorMessages == null || errorMessages.length == 0) {
			messageLabel.setText(Messages.DT0006.getText());
			messageLabel.setImage(null);
			return;
		}
		messageLabel.setText(StringUtils.join(errorMessages, " "));
		messageLabel.setImage(Dialog.getImage(DLG_IMG_MESSAGE_ERROR));
	}

	/**
	 * オプション設定ページの入力チェック.
	 * 
	 * @param optionFile オプションファイル.
	 * @return エラーメッセージ
	 */
	private String[] checkOption(CheckOptionFileWrapper optionFile) {

		List<String> errorMessageList = new ArrayList<String>();

		CheckOption[] options = optionFile.getOptions(engine);
		for (CheckOption option : options) {
			if (option.getClazz() == Integer.class && option.isEnable()) {
				String value = option.getValue();
				if (!NumberUtils.isNumber(value)) {
					errorMessageList.add(Messages.EM0005.format(option.getKey()));
				}
			}
		}
		return (String[]) errorMessageList.toArray(new String[errorMessageList.size()]);

	}

	@Override
	protected void okPressed() {

		super.okPressed();
		comp.getOptionFile().saveOption();
	}

	@Override
	protected boolean isResizable() {

		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {

		super.configureShell(newShell);
		newShell.setText(Messages.DT0006.getText());
	}

}
