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
 *
 */
package com.htmlhifive.tools.codeassist.ui.view;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.ResourceSelectionUtil;

import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePlugin;
import com.htmlhifive.tools.codeassist.core.H5CodeAssistCorePluginConst;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.ui.config.CodeAssistConfigManager;
import com.htmlhifive.tools.codeassist.ui.dialog.FileSelectionDialog;
import com.htmlhifive.tools.codeassist.ui.dialog.FolderSelectionDialog;
import com.htmlhifive.tools.codeassist.ui.messages.UIMessages;
import com.htmlhifive.tools.codeassist.ui.view.bean.BeanChangeEvent;
import com.htmlhifive.tools.codeassist.ui.view.bean.BeanChangedListner;
import com.htmlhifive.tools.codeassist.ui.view.bean.OptionConfigureBean;

/**
 * オプションファイル設定コンポジット.
 *
 * @author NS Solutions Corporation
 *
 */
public class OptionConfigureComposite extends Composite {

	/**
	 * ロガー.
	 */
	private static H5CodeAssistPluginLogger logger = H5CodeAssistPluginLoggerFactory
			.getLogger(OptionConfigureComposite.class);

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
		optionGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		optionGroup.setText(UIMessages.UICL0001.getText());
		optionGroup.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(5, 5, 10, 5).create());

		// オプションファイルのラベル.
		Label optionPathLabel = new Label(optionGroup, SWT.None);
		GridData gdOptionPathLabel = new GridData();
		gdOptionPathLabel.horizontalSpan = 1;
		gdOptionPathLabel.grabExcessHorizontalSpace = false;
		gdOptionPathLabel.verticalAlignment = SWT.BEGINNING;
		optionPathLabel.setText(UIMessages.UICL0002.getText());
		optionPathLabel.setLayoutData(gdOptionPathLabel);

		// テキストの作成.
		optionPathText = new Text(optionGroup, SWT.BORDER);
		GridData gdText = new GridData(GridData.FILL_HORIZONTAL);
		gdText.horizontalSpan = 1;
		gdText.verticalAlignment = SWT.BEGINNING;
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
		Composite btnCompsite = new Composite(optionGroup, SWT.None);
		btnCompsite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 5).create());
		btnCompsite.setLayoutData(GridDataFactory.fillDefaults().create());

		Button btnSelect = new Button(btnCompsite, SWT.None);
		btnSelect.setText(UIMessages.UIBT0001.getText());
		btnSelect.setLayoutData(GridDataFactory.fillDefaults().hint(60, -1).create());

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

		Button btnExport = new Button(this, SWT.None);
		btnExport.setText(UIMessages.UIBT0002.getText());
		btnExport.setLayoutData(GridDataFactory.swtDefaults().hint(-1, -1).create());
		btnExport.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				SaveAsDialog dialog = new SaveAsDialog(getShell());
				dialog.setTitle(UIMessages.UIDT0003.getText());
				dialog.setHelpAvailable(false);
				dialog.setOriginalFile(getProject().getFile("h5-code-assist.xml"));
				if (dialog.open() == Window.OK) {
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(dialog.getResult());
					boolean fileExist = file.exists();
					InputStream is = H5CodeAssistCorePlugin.class.getClassLoader().getResourceAsStream(
							"h5-code-assist.xml");
					try {
						if (!fileExist) {
							file.create(is, true, null);
						} else {
							file.setContents(is, IResource.DEPTH_INFINITE, null);
						}
						MessageDialog.open(MessageDialog.INFORMATION, getShell(), UIMessages.UIDT0004.getText(),
								UIMessages.UIDM0002.format(file.getFullPath().toString()), SWT.None);
					} catch (CoreException e1) {
						logger.log(UIMessages.UIEM0002, e1);
						ErrorDialog.openError(getShell(), UIMessages.UIDT0002.getText(), e1.getMessage(),
								e1.getStatus());
					} finally {
						IOUtils.closeQuietly(is);
					}
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
