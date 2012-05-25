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
 package jp.co.nssol.h5.tool.jslint.view;

import java.util.ArrayList;
import java.util.List;

import jp.co.nssol.h5.tool.jslint.JSLintPluginConstant;
import jp.co.nssol.h5.tool.jslint.configure.ConfigBean;
import jp.co.nssol.h5.tool.jslint.dialog.FileSelectionDialog;
import jp.co.nssol.h5.tool.jslint.dialog.OptionSettingDialog;
import jp.co.nssol.h5.tool.jslint.engine.option.Engine;
import jp.co.nssol.h5.tool.jslint.event.FilterBeanListChangeEvent;
import jp.co.nssol.h5.tool.jslint.event.FilterBeanListChangeListener;
import jp.co.nssol.h5.tool.jslint.messages.Messages;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * 設定ページ用コンポジット.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JslintPropertyComposite extends AbstractJsLintPropertyComposite {

	/**
	 * ボタンの幅.
	 */
	private static final int BUTTON_WIDTH = 60;

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
	 * グルーピングコンポジット.
	 */
	private DescriptionGroupComposite filterComp;

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット
	 * @param project 選択プロジェクト
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
		Composite comp = new Composite(this, SWT.None);
		GridData gdComp = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gdComp);
		GridLayout layout = new GridLayout(8, false);
		comp.setLayout(layout);

		// チェックボックス作成
		checkUseOtherProject = new Button(comp, SWT.CHECK);
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
		Label labelUseOtherProject = new Label(comp, SWT.None);
		GridData gdLabelUseOtherProject = new GridData();
		gdLabelUseOtherProject.horizontalSpan = 7;
		labelUseOtherProject.setText(Messages.DL0003.getText());
		labelUseOtherProject.setLayoutData(gdLabelUseOtherProject);

		// プロジェクトリスト.
		listviewer = new TableViewer(comp, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		listviewer.setContentProvider(new ArrayContentProvider());
		listviewer.setLabelProvider(new WorkbenchLabelProvider());
		GridData gdListviewer = new GridData(GridData.FILL_HORIZONTAL);
		gdListviewer.horizontalSpan = 8;
		gdListviewer.heightHint = 100;
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

		Group group = createGroup(Messages.DL0001.getText(), 8, GridData.FILL_HORIZONTAL);
		textOptionPath = new Text(group, SWT.BORDER);
		GridData gridOptionPath = new GridData(GridData.FILL_HORIZONTAL);
		gridOptionPath.horizontalSpan = 7;
		gridOptionPath.verticalSpan = 2;
		gridOptionPath.verticalAlignment = SWT.TOP;
		textOptionPath.setLayoutData(gridOptionPath);
		textOptionPath.setText(getConfigBean().getOptionFilePath());
		textOptionPath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				updateVariable();
			}

		});
		buttonOptionPath = locateButton(group, 1, Messages.B0001.getText());
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
		buttonOptionForm = locateButton(group, 1, Messages.B0004.getText());
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

		Group group = createGroup(Messages.DL0002.getText(), 8, GridData.FILL_HORIZONTAL);

		textJslintPath = new Text(group, SWT.BORDER);
		GridData gridOptionPath = new GridData(GridData.FILL_HORIZONTAL);
		gridOptionPath.horizontalSpan = 7;
		textJslintPath.setLayoutData(gridOptionPath);
		textJslintPath.setText(getConfigBean().getJsLintPath());
		textJslintPath.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				updateVariable();
			}
		});
		buttonJslintPath = locateButton(group, 1, Messages.B0001.getText());
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

		Group descriptionFilter = createGroup(Messages.DL0006.getText(), 1, GridData.FILL_BOTH);
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
	 * @param groupName グループ名
	 * @param column カラム数
	 * @param style スタイル
	 * @return グループ
	 */
	private Group createGroup(String groupName, int column, int style) {

		Group group = new Group(this, SWT.NONE);
		GridLayout layout = new GridLayout(column, false);
		group.setLayout(layout);
		group.setLayoutData(new GridData(style));
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
		GridData gridbutton = new GridData();
		gridbutton.widthHint = BUTTON_WIDTH;
		gridbutton.horizontalSpan = horizontalSpan;
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
