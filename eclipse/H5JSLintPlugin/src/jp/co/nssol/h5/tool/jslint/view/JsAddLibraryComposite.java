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

import jp.co.nssol.h5.tool.jslint.JSLintPluginConstant;
import jp.co.nssol.h5.tool.jslint.configure.ConfigBean;
import jp.co.nssol.h5.tool.jslint.dialog.FileSelectionDialog;
import jp.co.nssol.h5.tool.jslint.dialog.FolderSelectionDialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * ライブラリ追加コンポジット.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JsAddLibraryComposite extends AbstractJsLintPropertyComposite {

	/**
	 * jsファイル追加ボタン.
	 */
	private Button buttonAddJs;

	/**
	 * ライブラリフォルダ追加ボタン.
	 */
	private Button buttonAddLibFolder;

	/**
	 * ライブラリ削除ボタン.
	 */
	private Button buttonRemoveLib;

	/**
	 * 上へボタン.
	 */
	private Button buttonUp;

	/**
	 * 下へボタン.
	 */
	private Button buttonDown;

	/**
	 * 追加されたライブラリビューアー.
	 */
	private TableViewer addedLibViewer;

	/**
	 * プロジェクト.
	 * 
	 * @param parent 親コンポジット
	 * @param project プロジェクト
	 */
	public JsAddLibraryComposite(Composite parent, IProject project) {

		super(parent, project);

	}

	@Override
	protected void createMainArea() {

		// ベースコンポジットの作成
		Composite comp = new Composite(this, SWT.None);
		GridLayout layout = new GridLayout(8, false);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		// ライブラリ追加するラベル.
		Label labelAddLib = new Label(comp, SWT.None);
		labelAddLib.setText("ライブラリを追加します.");
		GridData gdLabelAddLib = new GridData(GridData.FILL_HORIZONTAL);
		gdLabelAddLib.horizontalSpan = 8;
		labelAddLib.setLayoutData(gdLabelAddLib);

		// 追加されたjsファイル、またはフォルダのビュー
		addedLibViewer = new TableViewer(comp);
		addedLibViewer.setContentProvider(new ArrayContentProvider());
		addedLibViewer.setLabelProvider(new WorkbenchLabelProvider());
		GridData gdAddedLibViewer = new GridData(GridData.FILL_BOTH);
		gdAddedLibViewer.horizontalSpan = 7;
		gdAddedLibViewer.verticalSpan = 8;
		addedLibViewer.getControl().setLayoutData(gdAddedLibViewer);
		addedLibViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {

				updateVariable();
			}
		});

		// jsファイル追加ボタン
		buttonAddJs = createButton(comp, "jsファイルの追加");
		buttonAddJs.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileSelectionDialog dialog = new FileSelectionDialog(getShell(), "ライブラリjsファイルの選択",
						"ライブラリとなるjsファイルを選択してください.", new String[] { JSLintPluginConstant.EXTENTION_JS });
				int result = dialog.open();
				if (Window.OK == result) {
					addedLibViewer.add(dialog.getFirstResult());
				}
				updateVariable();
			}
		});

		// ライブラリフォルダ追加ボタン
		buttonAddLibFolder = createButton(comp, "フォルダの追加");
		buttonAddLibFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), "ライブラリフォルダの選択",
						"ライブラリとなるフォルダを選択してください.");
				int result = dialog.open();
				if (Window.OK == result) {
					addedLibViewer.add(dialog.getFirstResult());
				}
				updateVariable();
			}
		});
		// 除去ボタン
		buttonRemoveLib = createButton(comp, "除去");
		buttonRemoveLib.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				Object selectItem = ((IStructuredSelection) addedLibViewer.getSelection()).getFirstElement();
				if (selectItem != null) {
					addedLibViewer.remove(selectItem);
					setEnableButton(false);
					return;
				}
				updateVariable();
			}
		});

		// 上へボタン
		buttonUp = createButton(comp, "上へ");
		buttonUp.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				updateVariable();
			}
		});

		// 下へボタン
		buttonDown = createButton(comp, "下へ");
		buttonDown.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				updateVariable();
			}
		});
	}

	/**
	 * ボタンを生成する.
	 * 
	 * @param parent 親コンポジット
	 * @param buttonName ボタン名
	 * @return ボタン
	 */
	private Button createButton(Composite parent, String buttonName) {

		Button btn = new Button(parent, SWT.None);
		btn.setText(buttonName);
		GridData gdButtonAddJs = new GridData();
		gdButtonAddJs.horizontalSpan = 1;
		gdButtonAddJs.widthHint = 220;
		btn.setLayoutData(gdButtonAddJs);
		return btn;
	}

	@Override
	protected void doUpdate() {

		TableItem[] items = addedLibViewer.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			IResource resource = (IResource) items[i].getData();
			getConfigBean().addLibList(resource.getFullPath().toString());
		}
		setEnableButton(!(addedLibViewer.getSelection() == null));
	}

	/**
	 * ボタンの活性非活性をセットする.
	 * 
	 * @param enable 活性:true 非活性:false
	 */
	private void setEnableButton(boolean enable) {

		buttonRemoveLib.setEnabled(enable);
		buttonDown.setEnabled(enable);
		buttonUp.setEnabled(enable);

	}

	@Override
	protected void doSetup(ConfigBean configBean) {

		setEnableButton(!(addedLibViewer.getSelection() == null));

	}

}
