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
package com.htmlhifive.tools.jslint.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.htmlhifive.tools.jslint.configure.FilterBean;
import com.htmlhifive.tools.jslint.configure.FilterBean.FilterLevel;
import com.htmlhifive.tools.jslint.event.FilterBeanListChangeEvent;
import com.htmlhifive.tools.jslint.event.FilterBeanListChangeListener;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * 説明文でフィルタリング、グルーピング設定をするコンポジット.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class DescriptionGroupComposite {

	/**
	 * 正規表現を入力、設定するテーブル.
	 */
	private CheckboxTableViewer tableViewer;

	/**
	 * テーブルの内容.
	 */
	private List<FilterBean> tableElemList;

	/**
	 * 削除ボタン.
	 */
	private Button btnDel;

	/**
	 * 追加ボタン.
	 */
	private Button btnAdd;

	/**
	 * リスナリスト.
	 */
	private List<FilterBeanListChangeListener> listenerList;
	/**
	 * {@link DescriptionGroupComposite}のベースとなるコンポジット.
	 */
	private Composite baseComp;

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット.
	 * @param style スタイル.
	 */
	public DescriptionGroupComposite(Composite parent, int style) {

		tableElemList = new ArrayList<FilterBean>();
		listenerList = new ArrayList<FilterBeanListChangeListener>();
		baseComp = new Composite(parent, style);
		baseComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout glBaseComp = new GridLayout(2, false);
		glBaseComp.verticalSpacing = -1;
		baseComp.setLayout(glBaseComp);
		Label top = new Label(baseComp, SWT.None);
		top.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		top.setText(Messages.DL0007.getText());
		createTable(baseComp);
		new Label(baseComp, SWT.NONE);
		new Label(baseComp, SWT.NONE);

	}

	/**
	 * テーブルを作成する.
	 * 
	 * @param comp コンポジット
	 */
	private void createTable(Composite comp) {

		// テーブルビューアの作成
		tableViewer = CheckboxTableViewer.newCheckList(comp, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SINGLE | SWT.CHECK);
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnPixelData(50));
		layout.addColumnData(new ColumnPixelData(300));
		layout.addColumnData(new ColumnPixelData(130));
		tableViewer.getTable().setLayout(layout);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);

		TableViewerColumn columnState = new TableViewerColumn(tableViewer, SWT.CENTER);
		columnState.getColumn().setText(Messages.TC0000.getText());
		columnState.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				return "";
			}
		});

		TableViewerColumn columnRegex = new TableViewerColumn(tableViewer, SWT.LEFT);
		columnRegex.getColumn().setText(Messages.TC0004.getText());
		columnRegex.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				FilterBean bean = (FilterBean) element;
				return bean.getRegex();
			}
		});
		columnRegex.setEditingSupport(new EditingSupport(tableViewer) {

			@Override
			protected void setValue(Object element, Object value) {

				FilterBean bean = (FilterBean) element;
				bean.setRegex((String) value);
				updateVariable();
			}

			@Override
			protected Object getValue(Object element) {

				String regEx = ((FilterBean) element).getRegex();
				return regEx == null ? "" : regEx;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {

				return new TextCellEditor(tableViewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {

				return true;
			}
		});
		TableViewerColumn columnCombo = new TableViewerColumn(tableViewer, SWT.LEFT);
		columnCombo.getColumn().setText(Messages.TC0005.getText());
		columnCombo.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				FilterBean bean = (FilterBean) element;
				return bean.getRevel().getLabel();
			}
		});
		columnCombo.setEditingSupport(new EditingSupport(tableViewer) {

			@Override
			protected void setValue(Object element, Object value) {

				FilterBean bean = (FilterBean) element;
				Integer selectNum = (Integer) value;
				// TODO 本当は設定したセルエディタから取りたい。。。
				bean.setLevel(FilterLevel.getRevelFromLabel(FilterLevel.getAllLabels()[selectNum]));
				updateVariable();
			}

			@Override
			protected Object getValue(Object element) {

				FilterBean bean = (FilterBean) element;
				return ArrayUtils.indexOf(FilterLevel.getAllLabels(), bean.getRevel().getLabel());
			}

			@Override
			protected CellEditor getCellEditor(Object element) {

				CellEditor cellEditor = new ComboBoxCellEditor(tableViewer.getTable(), FilterLevel.getAllLabels(),
						SWT.READ_ONLY);
				cellEditor.getLayoutData().minimumWidth = 10;
				return cellEditor;
			}

			@Override
			protected boolean canEdit(Object element) {

				return true;
			}
		});

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {

				FilterBean bean = (FilterBean) event.getElement();
				bean.setState(event.getChecked());
				updateVariable();

			}
		});

		Composite composite = new Composite(baseComp, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		composite.setLayout(new GridLayout(1, false));
		// 追加ボタンの作成
		btnAdd = new Button(composite, SWT.None);
		GridData gdBtnAdd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdBtnAdd.widthHint = 60;
		btnAdd.setLayoutData(gdBtnAdd);
		btnAdd.setText(Messages.B0002.getText());
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				tableElemList.add(new FilterBean());
				updateVariable();
			}
		});
		// 削除ボタンの作成
		btnDel = new Button(composite, SWT.None);
		GridData gdBtnDel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdBtnDel.widthHint = 60;
		btnDel.setLayoutData(gdBtnDel);
		btnDel.setText(Messages.B0003.getText());
		btnDel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FilterBean bean = (FilterBean) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
				tableElemList.remove(bean);
				updateVariable();
			}
		});
	}

	/**
	 * リスナを追加する.
	 * 
	 * @param listener 追加するリスナ.
	 */
	public void addFilterBeanListChangeListener(FilterBeanListChangeListener listener) {

		listenerList.add(listener);
	}

	/**
	 * リスナを削除する.
	 * 
	 * @param listener 削除するリスナ.
	 */
	public void removeFilterBeanListChangeListener(FilterBeanListChangeListener listener) {

		listenerList.remove(listener);
	}

	/**
	 * テーブルに設定されているフィルタビーンを取得する.
	 * 
	 * @return 設定されているフィルタビーン.
	 */
	public FilterBean[] getFilterBeans() {

		return (FilterBean[]) tableElemList.toArray(new FilterBean[tableElemList.size()]);
	}

	/**
	 * フィルタビーンをセットする.
	 * 
	 * @param input テーブルにセットするフィルタビーン.
	 */
	public void setUpTableElement(FilterBean[] input) {

		tableElemList.clear();
		for (FilterBean filterBean : input) {
			tableElemList.add(filterBean);
		}
		tableViewer.setInput(tableElemList);
		for (FilterBean filterBean : tableElemList) {
			tableViewer.setChecked(filterBean, filterBean.isState());
		}
		updateVariable();
	}

	/**
	 * テーブルを更新する.
	 */
	private void updateVariable() {

		tableViewer.setInput(tableElemList);
		tableViewer.refresh();
		for (FilterBeanListChangeListener listener : listenerList) {
			listener.modify(new FilterBeanListChangeEvent((FilterBean[]) tableElemList
					.toArray(new FilterBean[tableElemList.size()])));
		}

	}

	/**
	 * ボタン、ビューアの活性、非活性を操作する.
	 * 
	 * @param enabled 活性するかどうか.
	 */
	public void setEnabled(boolean enabled) {

		btnAdd.setEnabled(enabled);
		btnDel.setEnabled(enabled);
		tableViewer.getTable().setEnabled(enabled);
	}

}
