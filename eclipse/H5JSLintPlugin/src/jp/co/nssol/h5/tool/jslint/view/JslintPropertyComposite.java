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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
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
	 * JSLintのヘルプページ用のリンク.
	 */
	private Link linkHelpPage;

	/**
	 * グルーピングコンポジット.
	 */
	private DescriptionGroupComposite filterComp;

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
		messageLabel.setText("JSLintプラグインの設定を行います。");
		messageLabel.setLayoutData(gdMessageLabel);

		// ヘルプリンクの作成
		linkHelpPage = new Link(comp, SWT.None);
		GridData gdLinkHelpPage = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		linkHelpPage.setText("JSLintヘルプページは<a>こちら</a>");
		linkHelpPage.setLayoutData(gdLinkHelpPage);
		linkHelpPage.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				Program.launch("http://www.htmlhifive.com/conts/web/view/reference/eclipseplugins#HJSLint30D730E930B030A430F3");
			}
		});
		Group group = createGroup(comp, "プロジェクト参照", 2, 2, GridData.FILL_HORIZONTAL);
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

		Group group = createGroup(createBaseComposite(2), Messages.DL0001.getText(), 2, 2, GridData.FILL_HORIZONTAL);
		textOptionPath = new Text(group, SWT.BORDER);
		GridData gridOptionPath = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
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
		Composite composite = createBaseComposite(1);
		Group group = createGroup(composite, Messages.DL0002.getText(), 1, 1, GridData.FILL_HORIZONTAL);

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

		Group descriptionFilter = createGroup(composite, Messages.DL0006.getText(), 1, 1, GridData.FILL_HORIZONTAL);
		((GridData) descriptionFilter.getLayoutData()).heightHint = 300;
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
	 * @param style スタイル
	 * @return グループ
	 */
	private Group createGroup(Composite parent, String groupName, int column, int horizontalSpan, int style) {

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
