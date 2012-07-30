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
package com.htmlhifive.tools.jslint.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.htmlhifive.tools.jslint.JSLintPluginConstant;
import com.htmlhifive.tools.jslint.configure.ConfigBean;
import com.htmlhifive.tools.jslint.dialog.CreateEngineDialog;
import com.htmlhifive.tools.jslint.dialog.CreateOptionFileDialog;
import com.htmlhifive.tools.jslint.dialog.FileSelectionDialog;
import com.htmlhifive.tools.jslint.dialog.OptionSettingDialog;
import com.htmlhifive.tools.jslint.engine.option.CheckOptionFileWrapperFactory;
import com.htmlhifive.tools.jslint.engine.option.Engine;
import com.htmlhifive.tools.jslint.event.FilterBeanListChangeEvent;
import com.htmlhifive.tools.jslint.event.FilterBeanListChangeListener;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * 設定ページ用コンポジット.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JslintPropertyComposite extends AbstractJsLintPropertyComposite {

	/**
	 * プロジェクトリストの高さ.
	 */
	private static final int HEIGHT_PROJECT_LIST = 100;

	/**
	 * 説明フィルタの高さ.
	 */
	private static final int HEIGHT_DESCRIPTION = 200;

	/**
	 * ボタンの幅.
	 */
	private static final int BUTTON_WIDTH = 60;

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JslintPropertyComposite.class);

	/**
	 * オプション設定ファイルのパス.
	 */
	private Text textOptionPath;

	/**
	 * JSLint.jsファイルパス.
	 */
	private Text textJslintPath;

	/**
	 * 他プロジェクトの設定を利用するかチェックボックス.
	 */
	private Button checkUseOtherProject;

	/**
	 * オプションファイル選択ボタン.
	 */
	private Button buttonOptionPath;

	/**
	 * JSLint.jsファイル選択ボタン.
	 */
	private Button buttonJslintPath;

	/**
	 * プロジェクト選択ビューア.
	 */
	private TableViewer listviewer;

	/**
	 * オプション編集ボタン.
	 */
	private Button buttonOptionForm;

	/**
	 * JSLintのヘルプページ用のリンク.
	 */
	private Link linkHelpPage;

	/**
	 * グルーピングコンポジット.
	 */
	private DescriptionGroupComposite filterComp;

	/**
	 * 新規ボタン.
	 */
	private Button buttonNewOption;

	/**
	 * JslintDownloadボタン.
	 */
	private Button buttonNewJslint;

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット
	 * @param project 選択プロジェクト
	 * 
	 */
	public JslintPropertyComposite(Composite parent, IProject project) {

		super(parent, project);

	}

	@Override
	protected void createMainArea() {

		createOtherProject();
		createGroup1();
		createGroup2();

	}

	/**
	 * 別プロジェクトの設定を選択するエリアの作成.
	 */
	private void createOtherProject() {

		// 全体のベースコンポジット
		Composite comp = createBaseComposite(2);
		// JSLintの設定トップラベル
		Label messageLabel = new Label(comp, SWT.None);
		GridData gdMessageLabel = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		messageLabel.setText(Messages.DL0009.getText());
		messageLabel.setLayoutData(gdMessageLabel);

		// ヘルプリンクの作成
		linkHelpPage = new Link(comp, SWT.None);
		GridData gdLinkHelpPage = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		linkHelpPage.setText(Messages.DL0010.getText());
		linkHelpPage.setLayoutData(gdLinkHelpPage);
		linkHelpPage.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				Program.launch(Messages.DL0011.getText());
			}
		});
		Group group = createGroup(comp, Messages.WT0005.getText(), 2, 2);
		// チェックボックス作成
		checkUseOtherProject = new Button(group, SWT.CHECK);
		GridData gdUseOtherProjectCheck = new GridData();
		gdUseOtherProjectCheck.horizontalSpan = 1;
		checkUseOtherProject.setLayoutData(gdUseOtherProjectCheck);
		checkUseOtherProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				setActive(checkUseOtherProject.getSelection());
				updateVariable();

			}

		});

		// チェックボックスのラベル作成.
		Label labelUseOtherProject = new Label(group, SWT.None);
		GridData gdLabelUseOtherProject = new GridData();
		gdLabelUseOtherProject.horizontalSpan = 1;
		labelUseOtherProject.setText(Messages.DL0003.getText());
		labelUseOtherProject.setLayoutData(gdLabelUseOtherProject);

		// プロジェクトリスト.
		listviewer = new TableViewer(group, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		listviewer.setContentProvider(new ArrayContentProvider());
		listviewer.setLabelProvider(new WorkbenchLabelProvider());
		GridData gdListviewer = new GridData(GridData.FILL_HORIZONTAL);
		gdListviewer.horizontalSpan = 2;
		gdListviewer.heightHint = HEIGHT_PROJECT_LIST;
		listviewer.getControl().setLayoutData(gdListviewer);
		listviewer.add(getJsLintProjects());

		listviewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				updateVariable();

			}
		});

	}

	/**
	 * ベースコンポジットを生成する.
	 * 
	 * @param numColumn カラム数
	 * @return ベースコンポジット
	 */
	private Composite createBaseComposite(int numColumn) {
		Composite comp = new Composite(this, SWT.None);
		comp.setLayoutData(new GridData(SWT.FILL, 1, true, false));
		GridLayout layout = new GridLayout(numColumn, false);
		comp.setLayout(layout);
		return comp;
	}

	/**
	 * JSLint設定ファイルがあるプロジェクトを取得する.
	 * 
	 * @return JSLint設定ファイルを持っているプロジェクト.
	 */
	private IProject[] getJsLintProjects() {

		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> jslintProjects = new ArrayList<IProject>();
		for (IProject iProject : allProjects) {
			if (iProject.getFile(".jslint") != null && iProject.getFile(".jslint").exists()) {
				jslintProjects.add(iProject);
			}
		}
		return (IProject[]) jslintProjects.toArray(new IProject[jslintProjects.size()]);
	}

	/**
	 * 上部のコンポジットを作成する.
	 */
	private void createGroup1() {

		Group group = createGroup(createBaseComposite(1), Messages.DL0001.getText(), 1, 1);
		textOptionPath = new Text(group, SWT.BORDER);
		GridData gridOptionPath = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		textOptionPath.setLayoutData(gridOptionPath);
		textOptionPath.setText(getConfigBean().getOptionFilePath());
		textOptionPath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				updateVariable();
			}

		});
		// ボタン用コンポジット
		Composite buttonComp = new Composite(group, SWT.None);
		buttonComp.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		buttonComp.setLayoutData(GridDataFactory.fillDefaults().create());

		buttonNewOption = locateButton(buttonComp, 1, Messages.B0005.getText());
		buttonNewOption.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				CreateOptionFileDialog createOptionFileDialog = new CreateOptionFileDialog(getShell(), Messages.DT0007
						.getText());
				if (createOptionFileDialog.open() == Window.OK) {
					String outputPath = createOptionFileDialog.getOutputFilePath();
					logger.debug(createOptionFileDialog.getOutputFilePath());
					try {
						CheckOptionFileWrapperFactory.createCheckOptionFileWrapper(outputPath);
					} catch (CoreException e1) {
						e1.printStackTrace();
					}

					textOptionPath.setText(outputPath);
					updateVariable();
				}
			}

		});

		buttonOptionPath = locateButton(buttonComp, 1, Messages.B0001.getText());
		buttonOptionPath.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileSelectionDialog dialog = new FileSelectionDialog(getShell(), Messages.DT0001.getText(),
						Messages.DM0001.format(Messages.E0001.getText()), new String[] { "xml" });
				dialog.setInitialSelection(ResourcesPlugin.getWorkspace().getRoot()
						.findMember(textOptionPath.getText()));
				if (Window.OK == dialog.open()) {
					IFile jsFile = (IFile) dialog.getFirstResult();
					textOptionPath.setText(jsFile.getFullPath().toString());
				}
			}

		});
		buttonOptionForm = locateButton(buttonComp, 1, Messages.B0004.getText());
		buttonOptionForm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				OptionSettingDialog dialog = new OptionSettingDialog(getShell(), getProject(), Engine
						.getEngine(getConfigBean().getJsLintPath()));

				dialog.open();

			}

		});

	}

	/**
	 * 下部のコンポジットを作成する.
	 */
	private void createGroup2() {
		Composite composite = createBaseComposite(1);
		Group group = createGroup(composite, Messages.DL0002.getText(), 1, 1);
		// JSLint説明ラベル
		Label descLabel = new Label(group, SWT.None);
		GridData gdDescLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		descLabel.setLayoutData(gdDescLabel);
		descLabel.setText(Messages.DL0012.getText());
		// リンクコンポジット
		Composite linkComp = new Composite(group, SWT.None);
		linkComp.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).margins(30, 5).create());
		GridData gdlinkComp = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		linkComp.setLayoutData(gdlinkComp);

		// JSLINTのリンク
		Link linkJslint = new Link(linkComp, SWT.None);
		GridData gdLinkJslint = GridDataFactory.fillDefaults().create();
		linkJslint.setLayoutData(gdLinkJslint);
		linkJslint.setText(Messages.DL0013.getText());
		linkJslint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Messages.DL0014.getText());
			}
		});
		// JSHINTのリンク
		Link linkJshint = new Link(linkComp, SWT.None);
		GridData gdLinkJshint = GridDataFactory.fillDefaults().indent(50, 0).create();
		linkJshint.setLayoutData(gdLinkJshint);
		linkJshint.setText(Messages.DL0015.getText());
		linkJshint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Messages.DL0016.getText());
			}
		});
		textJslintPath = new Text(group, SWT.BORDER);
		GridData gridOptionPath = new GridData(GridData.FILL_HORIZONTAL);
		gridOptionPath.horizontalSpan = 1;
		textJslintPath.setLayoutData(gridOptionPath);
		textJslintPath.setText(getConfigBean().getJsLintPath());
		textJslintPath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				updateVariable();
			}
		});

		// 新規と選択ボタンコンポジット.
		Composite buttonComposite = new Composite(group, SWT.NONE);
		buttonComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		// 新規ダウンロードボタン
		buttonNewJslint = locateButton(buttonComposite, 1, Messages.B0008.getText());
		buttonNewJslint.setLayoutData(GridDataFactory.fillDefaults().create());
		buttonNewJslint.setToolTipText(Messages.DTT0001.getText());
		buttonNewJslint.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateEngineDialog dialog = new CreateEngineDialog(getShell(), getProject(), Messages.DT0010.getText());
				if (dialog.open() == Dialog.OK) {
					textJslintPath.setText(dialog.getEngineFilePath());
					updateVariable();
				}
			}
		});

		buttonJslintPath = locateButton(buttonComposite, 1, Messages.B0001.getText());
		buttonJslintPath.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileSelectionDialog dialog = new FileSelectionDialog(getShell(), Messages.DT0002.getText(),
						Messages.DM0001.format(Messages.E0000.getText()),
						new String[] { JSLintPluginConstant.EXTENTION_JS });
				dialog.setInitialSelection(ResourcesPlugin.getWorkspace().getRoot()
						.findMember(textJslintPath.getText()));
				if (Window.OK == dialog.open()) {
					IFile jsFile = (IFile) dialog.getFirstResult();
					textJslintPath.setText(jsFile.getFullPath().toString());
				}
			}

		});

		Group descriptionFilter = createGroup(composite, Messages.DL0006.getText(), 1, 1);
		((GridData) descriptionFilter.getLayoutData()).heightHint = HEIGHT_DESCRIPTION;
		filterComp = new DescriptionGroupComposite(descriptionFilter, SWT.None);
		filterComp.addFilterBeanListChangeListener(new FilterBeanListChangeListener() {

			@Override
			public void modify(FilterBeanListChangeEvent event) {

				updateVariable();
			}
		});
	}

	/**
	 * Gridカラム8のグループを生成する.
	 * 
	 * @param parent 親コンポジット
	 * @param groupName グループ名
	 * @param column カラム数
	 * @param horizontalSpan horizontalSpan
	 * @return グループ
	 */
	private Group createGroup(Composite parent, String groupName, int column, int horizontalSpan) {

		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout(column, false);
		group.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, horizontalSpan, 1);
		group.setLayoutData(gd);
		group.setText(groupName);
		return group;
	}

	/**
	 * GridLayoutのレイアウトを持つコンポジット(parent)にボタンを配置する.
	 * 
	 * @param parent 親コンポジット
	 * @param horizontalSpan カラム数.
	 * @param text ボタン名
	 * @return 配置したボタン
	 */
	private Button locateButton(Composite parent, int horizontalSpan, String text) {

		Button button = new Button(parent, SWT.None);
		GridData gridbutton = new GridData(SWT.LEFT, SWT.CENTER, false, false, horizontalSpan, 1);
		gridbutton.widthHint = BUTTON_WIDTH;
		button.setLayoutData(gridbutton);
		button.setText(text);
		return button;
	}

	/**
	 * 他のプロジェクトを利用するかどうかによる、ウィジェットの活性・非活性を設定する.
	 * 
	 * @param useOtherProject 他プロジェクトを使用するかどうか.
	 */
	private void setActive(boolean useOtherProject) {

		listviewer.getControl().setEnabled(useOtherProject);
		buttonJslintPath.setEnabled(!useOtherProject);
		textJslintPath.setEnabled(!useOtherProject);
		buttonOptionPath.setEnabled(!useOtherProject);
		buttonOptionForm.setEnabled(!useOtherProject);
		textOptionPath.setEnabled(!useOtherProject);
		filterComp.setEnabled(!useOtherProject);
		buttonNewOption.setEnabled(!useOtherProject);

	}

	@Override
	protected void doUpdate() {

		getConfigBean().setJsLintPath(textJslintPath.getText());
		getConfigBean().setOptionFilePath(textOptionPath.getText());
		IStructuredSelection selection = (IStructuredSelection) listviewer.getSelection();
		if (selection != null && selection.getFirstElement() != null) {
			getConfigBean().setOtherProjectPath(((IProject) selection.getFirstElement()).getName());
		}
		getConfigBean().setUseOtherProject(checkUseOtherProject.getSelection());
		getConfigBean().replaceFilterBeans(filterComp.getFilterBeans());
		if (existsFile(getConfigBean().getJsLintPath())
				&& existsFile(getConfigBean().getOptionFilePath())
				&& StringUtils.endsWithAny(getConfigBean().getJsLintPath(), new String[] {
						JSLintPluginConstant.JS_HINT_NAME, JSLintPluginConstant.JS_LINT_NAME })) {
			buttonOptionForm.setEnabled(true);
		} else {
			buttonOptionForm.setEnabled(false);
		}
	}

	/**
	 * ワークスペース内に指定したパスのファイルが存在するかをチェックする.
	 * 
	 * @param path パス.
	 * @return 指定したファイルが存在するかどうか.
	 */
	private boolean existsFile(String path) {

		if (StringUtils.isEmpty(path)) {
			return false;
		}
		if (ResourcesPlugin.getWorkspace().getRoot().exists(new Path(path))) {
			return true;
		}
		return false;
	}

	@Override
	protected void doSetup(ConfigBean configBean) {

		checkUseOtherProject.setSelection(configBean.isUseOtherProject());
		textJslintPath.setText(configBean.getJsLintPath());
		textOptionPath.setText(configBean.getOptionFilePath());
		filterComp.setUpTableElement(configBean.getFilterBeans());
		if (StringUtils.isNotEmpty(configBean.getOtherProjectPath())) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(configBean.getOtherProjectPath());
			if (project != null) {
				listviewer.setSelection(new StructuredSelection(project));
			}
		}
		setActive(configBean.isUseOtherProject());

	}

}
