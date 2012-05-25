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
 package jp.co.nssol.h5.tools.codeassist.ui.view;

import java.util.ArrayList;
import java.util.List;

import jp.co.nssol.h5.tools.codeassist.ui.config.CodeAssistConfigManager;
import jp.co.nssol.h5.tools.codeassist.ui.dialog.FileSelectionDialog;
import jp.co.nssol.h5.tools.codeassist.ui.messages.UIMessages;
import jp.co.nssol.h5.tools.codeassist.ui.view.bean.BeanChangeEvent;
import jp.co.nssol.h5.tools.codeassist.ui.view.bean.BeanChangedListner;
import jp.co.nssol.h5.tools.codeassist.ui.view.bean.OptionConfigureBean;
import jp.co.nssol.h5.tools.codeassist.core.H5CodeAssistCorePluginConst;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * オプションファイル設定コンポジット.
 *
 * @author NS Solutions Corporation
 *
 */
public class OptionConfigureComposite extends Composite {

	/**
	 * オプションファイルパスのテキスト.
	 */
	private Text optionPathText;
	/**
	 * プロジェクト.
	 */
	private IProject project;

	/**
	 * 設定情報ビーン.
	 */
	private OptionConfigureBean bean;
	/**
	 * ビーンチェンジリスト.
	 */
	private List<BeanChangedListner> beanChangedListnerList;

	/**
	 * コンストラクタ.
	 *
	 * @param parent 親コンポジット.
	 * @param project プロジェクト.
	 * @throws CoreException コンポジット生成失敗.
	 */
	public OptionConfigureComposite(Composite parent, IProject project) throws CoreException {

		super(parent, SWT.None);
		this.project = project;
		bean = CodeAssistConfigManager.getConfig(project, true).getConfigBean();
		beanChangedListnerList = new ArrayList<BeanChangedListner>();
		createComposite();
	}

	/**
	 * コンポジット生成.
	 *
	 * @throws CoreException コンポジット生成失敗.
	 */
	private void createComposite() throws CoreException {

		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.setLayout(new GridLayout(1, false));
		// オプションファイルグループの生成.
		Group optionGroup = new Group(this, SWT.None);
		optionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		optionGroup.setText(UIMessages.UICL0001.getText());
		optionGroup.setLayout(new GridLayout(8, false));

		// オプションファイルのラベル.
		Label optionPathLabel = new Label(optionGroup, SWT.None);
		GridData gdOptionPathLabel = new GridData();
		gdOptionPathLabel.horizontalSpan = 1;
		gdOptionPathLabel.grabExcessHorizontalSpace = false;
		optionPathLabel.setText(UIMessages.UICL0002.getText());
		optionPathLabel.setLayoutData(gdOptionPathLabel);

		// テキストの作成.
		optionPathText = new Text(optionGroup, SWT.BORDER);
		GridData gdText = new GridData(GridData.FILL_HORIZONTAL);
		gdText.horizontalSpan = 6;
		optionPathText.setLayoutData(gdText);
		if (bean.getOptionFilePath() != null) {
			optionPathText.setText(bean.getOptionFilePath());
		}
		optionPathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				updateBean();
			}

		});
		Button btnSelect = new Button(optionGroup, SWT.None);
		btnSelect.setText(UIMessages.UIBT0001.getText());
		GridData gdBtnSelect = new GridData();
		gdBtnSelect.widthHint = 60;
		btnSelect.setLayoutData(gdBtnSelect);

		btnSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				FileSelectionDialog dialog = new FileSelectionDialog(getShell(), UIMessages.UIDT0001.getText(),
						UIMessages.UIDM0001.getText(), new String[] { H5CodeAssistCorePluginConst.EXTENTION_XML });
				dialog.setInitialSelection(ResourcesPlugin.getWorkspace().getRoot()
						.findMember(optionPathText.getText()));
				if (dialog.open() == Window.OK) {
					IFile file = (IFile) dialog.getFirstResult();
					optionPathText.setText(file.getFullPath().toString());
					updateBean();
				}
			}
		});

	}

	/**
	 * ビーンの更新処理.
	 */
	private void updateBean() {

		bean.setOptionFilePath(optionPathText.getText());
		for (BeanChangedListner listener : beanChangedListnerList) {
			listener.beanChanged(new BeanChangeEvent(bean));
		}
	}

	/**
	 * プロジェクトを取得する.
	 *
	 * @return プロジェクト.
	 */
	protected IProject getProject() {

		return project;
	}

	/**
	 * リスナを追加する.
	 *
	 * @param beanChangedListner リスナ.
	 */
	public void addBeanChangedListner(BeanChangedListner beanChangedListner) {

		if (beanChangedListner != null) {
			beanChangedListnerList.add(beanChangedListner);
		}
	}

	/**
	 * 設定情報ビーンを取得する.
	 *
	 * @return 設定情報ビーン
	 */
	public OptionConfigureBean getBean() {

		return bean;
	}

}
