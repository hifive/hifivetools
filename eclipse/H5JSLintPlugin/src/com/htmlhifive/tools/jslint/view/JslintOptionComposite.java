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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.htmlhifive.tools.jslint.configure.ConfigBean;
import com.htmlhifive.tools.jslint.engine.option.CheckOption;
import com.htmlhifive.tools.jslint.engine.option.CheckOptionFileWrapper;
import com.htmlhifive.tools.jslint.engine.option.CheckOptionFileWrapperFactory;
import com.htmlhifive.tools.jslint.engine.option.Engine;
import com.htmlhifive.tools.jslint.event.CheckOptionChangeEvent;
import com.htmlhifive.tools.jslint.event.CheckOptionChangeListener;
import com.htmlhifive.tools.jslint.exception.JSLintPluginException;
import com.htmlhifive.tools.jslint.messages.Messages;
import com.htmlhifive.tools.jslint.util.ConfigBeanUtil;

/**
 * オプションを設定するコンポジット.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class JslintOptionComposite extends AbstractJsLintPropertyComposite {

	/**
	 * キーカラムの幅.
	 */
	private static final int COLUMN_WIDTH_KEY = 100;

	/**
	 * チェックボックスカラムの幅.
	 */
	private static final int COLUMN_WIDTH_CHECK = 50;

	/**
	 * 説明カラムの幅.
	 */
	private static final int COLUMN_WIDTH_DESCRIPTION = 600;

	/**
	 * 値カラムの幅.
	 */
	private static final int COLUMN_WIDTH_VALUE = 100;

	/**
	 * integer型オプションチェックボックステーブルビューアー.
	 */
	private CheckboxTableViewer integerTableViewer;

	/**
	 * 説明詳細のテキスト.
	 */
	private Text detail;

	/**
	 * サッシュ.
	 */
	private SashForm sash;

	/**
	 * オプションファイル.
	 */
	private CheckOptionFileWrapper optionFile;

	/**
	 * リスナ.
	 */
	private List<CheckOptionChangeListener> listenerList = new ArrayList<CheckOptionChangeListener>();

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット.
	 * @param project プロジェクト.
	 */
	public JslintOptionComposite(Composite parent, IProject project) {

		super(parent, project);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see com.htmlhifive.tool.jslint.view.AbstractJsLintPropertyComposite#
	 * createMainArea()
	 */
	@Override
	protected void createMainArea() {

		Label label = new Label(this, SWT.None);
		label.setText(Messages.DL0005.getText());
		sash = new SashForm(this, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));
		createOptionArea();
		createDetailArea();
		sash.setWeights(new int[] { 70, 30 });
		sash.setSize(COLUMN_WIDTH_CHECK + COLUMN_WIDTH_KEY + COLUMN_WIDTH_DESCRIPTION + COLUMN_WIDTH_VALUE, 500);

	}

	/**
	 * オプション設定エリアを生成する.
	 */
	private void createOptionArea() {

		// グループの作成
		Group group = createGroup(sash, Messages.WT0000.getText());

		// テーブルビューアの作成
		integerTableViewer = CheckboxTableViewer.newCheckList(group, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SINGLE | SWT.CHECK);
		Table table = integerTableViewer.getTable();
		// テーブルの設定
		settingTable(table, new int[] { COLUMN_WIDTH_CHECK, COLUMN_WIDTH_KEY, COLUMN_WIDTH_DESCRIPTION,
				COLUMN_WIDTH_VALUE });

		TableViewerColumn enableColumn = new TableViewerColumn(integerTableViewer, SWT.LEFT);
		enableColumn.getColumn().setText(Messages.TC0000.getText());
		enableColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return "";
			}
		});
		TableViewerColumn keyColumn = new TableViewerColumn(integerTableViewer, SWT.LEFT);
		keyColumn.getColumn().setText(Messages.TC0001.getText());
		keyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return ((CheckOption) element).getKey();
			}
		});

		TableViewerColumn descriptionColumn = new TableViewerColumn(integerTableViewer, SWT.LEFT);
		descriptionColumn.getColumn().setText(Messages.TC0002.getText());
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return ((CheckOption) element).getDescription();
			}
		});
		TableViewerColumn valuecolumn = new TableViewerColumn(integerTableViewer, SWT.LEFT | SWT.BORDER);
		valuecolumn.getColumn().setText(Messages.TC0003.getText());
		valuecolumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return ((CheckOption) element).getValue();
			}

			@Override
			public Color getBackground(Object element) {

				CheckOption option = (CheckOption) element;
				if (option.getClazz() == Boolean.class) {
					return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
				}
				return super.getBackground(element);
			}
		});
		valuecolumn.setEditingSupport(new EditingSupport(integerTableViewer) {

			@Override
			protected void setValue(Object element, Object value) {

				CheckOption option = (CheckOption) element;
				option.setValue((String) value);
				integerTableViewer.refresh();
				updateVariable();
			}

			@Override
			protected Object getValue(Object element) {

				String value = ((CheckOption) element).getValue();
				return value == null ? "" : value;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {

				return new TextCellEditor(integerTableViewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {

				CheckOption option = (CheckOption) element;
				if (option.getClazz() == Boolean.class) {
					return false;
				}
				return true;
			}
		});
		integerTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		integerTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				CheckOption option = (CheckOption) selection.getFirstElement();
				if (option != null) {
					detail.setText(option.getDetail() == null ? "" : option.getDetail());
				}

			}
		});

		integerTableViewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {

				CheckOption option = (CheckOption) event.getElement();
				option.setEnable(event.getChecked());
				updateVariable();
			}
		});

		// 追加ボタン
		// TODO オプション追加機能.
		// Button addButton = createButton(group,
		// Messages.B0002.getText());
		// addButton.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		//
		// AddOptionDialog dialog = new AddOptionDialog(getShell());
		// dialog.open();
		//
		// }
		// });
		// createButton(group, Messages.B0003.getText());

	}

	/**
	 * グループを作成する.
	 * 
	 * @param parents 親コンポジット
	 * @param title タイトル
	 * @return 生成したグループ
	 */
	private Group createGroup(Composite parents, String title) {

		Group group = new Group(parents, SWT.None);
		GridData gdGroup = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gdGroup);
		group.setLayout(new GridLayout(8, false));
		group.setText(title);
		return group;
	}

	// /**
	// * ボタンを作成する.
	// *
	// * @param parent 親コンポジット.
	// * @param buttonName ボタン名.
	// * @return ボタン.
	// */
	// private Button createButton(Composite parent, String buttonName) {
	//
	// Button btn = new Button(parent, SWT.None);
	// btn.setText(buttonName);
	// GridData gdButtonAddJs = new GridData();
	// gdButtonAddJs.horizontalSpan = 1;
	// gdButtonAddJs.widthHint = 60;
	// btn.setLayoutData(gdButtonAddJs);
	// return btn;
	// }

	/**
	 * 指定のテーブルをセットする.<br>
	 * tableカラムの大きさをセット.
	 * 
	 * @param table テーブル.
	 * @param columnPixels カラムの大きさ
	 */
	private void settingTable(Table table, int[] columnPixels) {

		GridData gdTableViewer = new GridData(GridData.FILL_BOTH);
		gdTableViewer.horizontalSpan = 7;
		gdTableViewer.verticalSpan = 5;
		gdTableViewer.grabExcessVerticalSpace = true;
		gdTableViewer.heightHint = 500;
		table.setLayoutData(gdTableViewer);
		TableLayout layout = new TableLayout();
		for (int pixel : columnPixels) {
			layout.addColumnData(new ColumnPixelData(pixel));
		}
		table.setLayout(layout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	/**
	 * 詳細説明エリアを作成する.
	 */
	private void createDetailArea() {

		GridData gdGroup = new GridData(GridData.FILL_BOTH);
		Group group = new Group(sash, SWT.SCROLL_PAGE);
		group.setLayoutData(gdGroup);
		group.setLayout(new GridLayout(1, false));
		group.setText(Messages.WT0001.getText());
		detail = new Text(group, SWT.READ_ONLY);
		GridData gdDetail = new GridData(GridData.FILL_BOTH);
		gdDetail.heightHint = 100;
		detail.setLayoutData(gdDetail);

	}

	@Override
	protected void doUpdate() {

		CheckOption[] options = (CheckOption[]) integerTableViewer.getInput();

		for (CheckOption option : options) {
			optionFile.updateOption(option);
		}
		if (listenerList == null) {
			listenerList = new ArrayList<CheckOptionChangeListener>();
		}

		for (CheckOptionChangeListener listener : listenerList) {
			listener.modify(new CheckOptionChangeEvent(optionFile));
		}

		// optionFile.saveOption();
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * com.htmlhifive.tool.jslint.view.AbstractJsLintPropertyComposite#doSetup
	 * (com.htmlhifive.tool.jslint.configure.ConfigBean)
	 */
	@Override
	protected void doSetup(ConfigBean configBean) throws JSLintPluginException {

		if (Engine.getEngine(configBean.getJsLintPath()) == null) {
			throw new JSLintPluginException(Messages.EM0009.format(Messages.DL0002.getText()));
		}
		if (configBean.getOptionFilePath() == null || StringUtils.isEmpty(configBean.getOptionFilePath())) {
			throw new JSLintPluginException(Messages.EM0009.format(Messages.DL0001.getText()));
		}
		try {
			optionFile = CheckOptionFileWrapperFactory.createCheckOptionFileWrapper(configBean.getOptionFilePath());
		} catch (CoreException e) {
			throw new JSLintPluginException(e);
		}
		CheckOption[] defaultOption = ConfigBeanUtil.getAllJsHintOptionFromDefault();
		for (CheckOption checkOption : defaultOption) {
			optionFile.addOption(checkOption);
		}

		CheckOption[] options = optionFile.getOptions(Engine.getEngine(configBean.getJsLintPath()));
		integerTableViewer.setInput(options);
		// チェック項目が合ったらチェックを行う.
		for (CheckOption option : options) {
			integerTableViewer.setChecked(option, option.isEnable());
			integerTableViewer.refresh();
		}
		// booleanTableViewer.getControl().setEnabled(!getConfigBean().isUseOtherProject());
		integerTableViewer.getControl().setEnabled(!getConfigBean().isUseOtherProject());
	}

	/**
	 * optionFileを取得する.
	 * 
	 * @return optionFile
	 */
	public CheckOptionFileWrapper getOptionFile() {

		return optionFile;
	}

	/**
	 * チェックオプションリスナを追加する.
	 * 
	 * @param listener 追加するリスナ.
	 */
	public void addCheckOptionChangeListener(CheckOptionChangeListener listener) {

		listenerList.add(listener);
	}

}
