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
package com.htmlhifive.tools.wizard.ui.page;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.RemoteContentManager;
import com.htmlhifive.tools.wizard.library.model.LibraryList;
import com.htmlhifive.tools.wizard.library.model.LibraryState;
import com.htmlhifive.tools.wizard.library.model.xml.Category;
import com.htmlhifive.tools.wizard.library.model.xml.Library;
import com.htmlhifive.tools.wizard.library.model.xml.Site;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.UIMessages;
import com.htmlhifive.tools.wizard.ui.page.tree.CategoryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryTreeLabelProvider;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryTreeLatestViewerFilter;
import com.htmlhifive.tools.wizard.ui.page.tree.RootNode;

/**
 * <H3>ライブラリインポート用コンポジット.</H3>
 * 
 * @author fkubo
 */
public class LibraryImportComposite extends Composite {

	private final Tree treeLibrary;
	private final CheckboxTreeViewer treeViewerLibrary;
	private final Table tableSelection;
	// private final TableViewer tableViewerSelection;
	private final Link linkDetail;
	private IJavaScriptProject jsProject = null;
	private String wizardProjectName = null;
	private String defaultJsLibPath = null;
	private ScrolledComposite scrolledComposite = null;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public LibraryImportComposite(Composite parent, int style) {

		super(parent, style);
		setLayout(new GridLayout(1, false));

		Label lblInfo = new Label(this, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblInfo.setText(UIMessages.LibraryImportComposite_lblNewLabel_text);

		Group groupAll = new Group(this, SWT.NONE);
		groupAll.setLayout(new GridLayout(3, false));
		groupAll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		groupAll.setText(UIMessages.LibraryImportPageComposite_groupAll_text);

		Button checkFilterLatest = new Button(groupAll, SWT.CHECK);
		checkFilterLatest.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		checkFilterLatest.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				do_checkFilterLatest_widgetSelected(e);
			}
		});
		checkFilterLatest.setText(UIMessages.LibraryImportComposite_checkFilterLatest_text);
		checkFilterLatest.setSelection(true);

		Button btnRecommended = new Button(groupAll, SWT.NONE);
		btnRecommended.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnRecommended.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				do_btnRecommended_widgetSelected(e);
			}
		});
		btnRecommended.setText(UIMessages.LibraryImportComposite_btnRecommended_text);

		Button btnReload = new Button(groupAll, SWT.NONE);
		btnReload.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnReload.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				do_btnReload_widgetSelected(e);
			}
		});
		btnReload.setText(UIMessages.LibraryImportComposite_btnReload_text);

		treeViewerLibrary = new CheckboxTreeViewer(groupAll, SWT.BORDER | SWT.CHECK);
		treeLibrary = treeViewerLibrary.getTree();

		TreeColumn treeClmnName = new TreeColumn(treeLibrary, SWT.NONE);
		treeClmnName.setWidth(320);
		treeClmnName.setText(UIMessages.LibraryImportComposite_treeColumn_text);

		treeLibrary.setHeaderVisible(true);
		treeLibrary.setLinesVisible(true);
		treeLibrary.setEnabled(false);

		treeViewerLibrary.setAutoExpandLevel(3);
		Tree treeLibrary = treeViewerLibrary.getTree();
		GridData gd_treeLibrary = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2);
		gd_treeLibrary.heightHint = 160;
		treeLibrary.setLayoutData(gd_treeLibrary);
		treeLibrary.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				do_treeLibrary_widgetSelected(e);
			}
		});

		treeViewerLibrary.setContentProvider(new TreeNodeContentProvider());
		treeViewerLibrary.setLabelProvider(new LibraryTreeLabelProvider());
		treeViewerLibrary.setFilters(new ViewerFilter[] { new LibraryTreeLatestViewerFilter() });

		TreeColumn treeClmnPath = new TreeColumn(treeLibrary, SWT.NONE);
		treeClmnPath.setWidth(180);
		treeClmnPath.setText(UIMessages.LibraryImportComposite_treeClmnPath_text);

		Group grpDetails = new Group(groupAll, SWT.NONE);
		FillLayout fl_grpDetails = new FillLayout(SWT.HORIZONTAL);
		fl_grpDetails.marginWidth = 5;
		fl_grpDetails.marginHeight = 5;
		grpDetails.setLayout(fl_grpDetails);
		GridData gd_grpDetails = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
		gd_grpDetails.heightHint = 60;
		grpDetails.setLayoutData(gd_grpDetails);
		grpDetails.setText(UIMessages.LibraryImportComposite_grpDetails_text);

		scrolledComposite = new ScrolledComposite(grpDetails, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		linkDetail = new Link(scrolledComposite, SWT.NONE);
		linkDetail.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent selectionevent) {

				if (selectionevent.text.startsWith("http://") || selectionevent.text.startsWith("https://")) {
					// 安全のためとりあえずは、http, httpsだけ
					Program.launch(selectionevent.text);
				}
			}
		});
		scrolledComposite.setContent(linkDetail);
		scrolledComposite.setMinSize(linkDetail.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Group groupSelect = new Group(this, SWT.NONE);
		groupSelect.setLayout(new GridLayout(2, false));
		groupSelect.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
		groupSelect.setText(UIMessages.LibraryImportPageComposite_groupSelect_text);

		TableViewer tableSelectionViewer =
				new TableViewer(groupSelect, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableSelection = tableSelectionViewer.getTable();
		GridData gd_tableSelection = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_tableSelection.heightHint = 60;
		tableSelection.setLayoutData(gd_tableSelection);
		tableSelection.setHeaderVisible(true);
		tableSelection.setLinesVisible(true);

		TableColumn tblclmnStatus = new TableColumn(tableSelection, SWT.NONE);
		tblclmnStatus.setText(UIMessages.LibraryImportComposite_tblclmnStatus_text);
		tblclmnStatus.setWidth(80);

		TableColumn tblclmnCategory = new TableColumn(tableSelection, SWT.NONE);
		tblclmnCategory.setWidth(100);
		tblclmnCategory.setText(UIMessages.LibraryImportComposite_tblclmnCategory_text);

		TableColumn tblclmnVersion = new TableColumn(tableSelection, SWT.NONE);
		tblclmnVersion.setWidth(80);
		tblclmnVersion.setText(UIMessages.LibraryImportComposite_tblclmnVersion_text);

		TableColumn tblclmnPath = new TableColumn(tableSelection, SWT.NONE);
		tblclmnPath.setWidth(80);
		tblclmnPath.setText(UIMessages.LibraryImportComposite_tblclmnPath_text);

		TableColumn tblclmnFiles = new TableColumn(tableSelection, SWT.NONE);
		tblclmnFiles.setWidth(80);
		tblclmnFiles.setText(UIMessages.LibraryImportComposite_tblclmnFiles_text);

		TableColumn tblclmnUrl = new TableColumn(tableSelection, SWT.NONE);
		tblclmnUrl.setWidth(160);
		tblclmnUrl.setText(UIMessages.LibraryImportComposite_tblclmnUrl_text);
	}

	/**
	 * 初期化.
	 * 
	 * @param jsProject プロジェクト
	 * @param wizardProjectName プロジェクト名
	 * @param defaultJsLibPath JSパス
	 * @return 変更あり
	 */
	public boolean initialize(IJavaScriptProject jsProject, String wizardProjectName, String defaultJsLibPath) {

		if (jsProject == null && StringUtils.equals(this.wizardProjectName, wizardProjectName)
				&& StringUtils.equals(this.defaultJsLibPath, defaultJsLibPath)) {
			// 変更不可.
			return false;
		}

		this.jsProject = jsProject;
		this.wizardProjectName = wizardProjectName;
		this.defaultJsLibPath = defaultJsLibPath;

		// 選択を除去.
		H5WizardPlugin.getInstance().getSelectedLibrarySet().clear();

		// // 一覧をダウンロード.
		refreshTreeLibrary(jsProject == null, true);
		return true;
	}

	/**
	 * ツリー最新化.
	 * 
	 * @param checkedRecommanded 推奨設定をチェック済みにするかどうか
	 * @param forceDefault 強制的にデフォルトに戻す
	 */
	public void refreshTreeLibrary(boolean checkedRecommanded, boolean forceDefault) {

		LibraryList libraryList = RemoteContentManager.getLibraryList();

		// テーブルも初期化.
		if (!checkedRecommanded) {
			getSelectedLibrarySet().clear();
		}

		tableSelection.removeAll();
		tableSelection.layout();

		// libraryListのnull対応.
		if (libraryList == null) {
			treeViewerLibrary.setInput(null);
			treeLibrary.setEnabled(false);
			return;
		}
		treeLibrary.setEnabled(true);

		// チェック.
		RootNode rootNode = new RootNode(libraryList);
		libraryList.checkLibrary(jsProject, wizardProjectName, defaultJsLibPath, rootNode);
		treeViewerLibrary.setInput(rootNode.getChildren());

		treeViewerLibrary.refresh(true);

		// Treeに反映.
		for (TreeItem item2 : treeLibrary.getItems()) {
			for (TreeItem item3 : item2.getItems()) {
				if (item3.getData() instanceof LibraryNode) {
					LibraryNode libraryNode = (LibraryNode) item3.getData();
					if (checkedRecommanded && libraryNode.isRecommended() || libraryNode.isExists()) {
						item3.setChecked(true);
						addTableItem(libraryNode);
					} else if (forceDefault) {
						item3.setChecked(false);
					}
					effectTreeViewParentItem(item3);
				}
			}
		}

		changeTableSelection();
	}

	/**
	 * 選択ライブラリ取得.
	 * 
	 * @return 選択ライブラリ.
	 */
	private Set<LibraryNode> getSelectedLibrarySet() {

		return H5WizardPlugin.getInstance().getSelectedLibrarySet();
	}

	/**
	 * ツリー選択イベント.
	 * 
	 * @param e イベント
	 */
	protected void do_treeLibrary_widgetSelected(SelectionEvent e) {

		// 詳細更新.

		TreeItem treeItem = (TreeItem) e.item;
		CategoryNode categoryNode = null;
		if (treeItem.getData() instanceof CategoryNode) {
			categoryNode = (CategoryNode) treeItem.getData();
		} else if (treeItem.getData() instanceof LibraryNode) {
			LibraryNode libraryNode = (LibraryNode) treeItem.getData();
			treeLibrary.setToolTipText(Messages.PI0135.format(libraryNode.getState().getText()));
			treeItem.getParent().setToolTipText(Messages.PI0135.format(libraryNode.getState().getText()));
			categoryNode = libraryNode.getParent();
		}

		StringBuilder detail = new StringBuilder();
		if (categoryNode != null) {
			if (StringUtils.isNotEmpty(categoryNode.getDescription())) {
				detail.append(categoryNode.getDescription());
			} else {
				detail.append(categoryNode.getLabel());
			}
		}
		linkDetail.setText(detail.toString());
		scrolledComposite.setMinSize(linkDetail.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		if (e.detail == SWT.CHECK) {
			if (treeLibrary.getSelection().length == 1 && treeLibrary.getSelection()[0] != treeItem) {
				treeLibrary.setSelection(treeItem); // 選択状態にしておく.
			}
			if (treeItem.getData() instanceof LibraryNode) {
				LibraryNode libraryNode = (LibraryNode) treeItem.getData();
				if (treeItem.getChecked()) {
					// 追加処理.
					// System.out.println("Check");
					addTableItem(libraryNode);
				} else {
					// 削除処理.
					// System.out.println("Remove");
					// 削除.
					removeTableItem(libraryNode);
				}

				// 親処理.
				effectTreeViewParentItem(treeItem);

				tableSelection.layout();
			} else if (treeItem.getData() instanceof CategoryNode) {
				for (TreeItem childItem : treeItem.getItems()) {
					LibraryNode libraryNode = (LibraryNode) childItem.getData();

					childItem.setChecked(treeItem.getChecked());
					if (childItem.getChecked()) {
						// 追加.
						addTableItem(libraryNode);
					} else {
						// 削除.
						removeTableItem(libraryNode);
					}
				}
				treeItem.setGrayed(false);
				tableSelection.layout();
			}

			changeTableSelection();
			treeViewerLibrary.refresh(true);
			// } else {
			//
			// buttonAdd.setEnabled(false);
			//
			// TreeItem item = (TreeItem) e.item;
			// if (item.getData() instanceof LibraryNode) {
			// LibraryNode libraryNode = (LibraryNode) item.getData();
			// // 全て含まれている時はfalseのまま.
			// if (!getSelectedLibrarySet().contains(libraryNode) && !existsLibrary(libraryNode)) {
			// buttonAdd.setEnabled(true);
			// }
			// }
		}
	}

	/**
	 * 親Itemの状態を設定する.
	 * 
	 * @param treeItem 対象Item
	 */
	private void effectTreeViewParentItem(TreeItem treeItem) {

		// 親処理.
		boolean allChecked = true;
		boolean anyChecked = false;
		for (TreeItem childItem : treeItem.getParentItem().getItems()) {
			if (childItem.getChecked()) {
				anyChecked = true;
			} else {
				allChecked = false;
			}
		}
		treeItem.getParentItem().setChecked(anyChecked || allChecked);
		treeItem.getParentItem().setGrayed(anyChecked && !allChecked);

	}

	/**
	 * ライブラリツリーを取得する.
	 * 
	 * @param e イベント
	 * @return ライブラリツリー
	 */
	public Tree getTreeLibrary() {

		return treeLibrary;
	}

	/**
	 * 変更が必要かどうか.
	 * 
	 * @return 変更が必要かどうか
	 */
	public boolean isChanged() {

		return tableSelection.getItemCount() > 0;
	}

	/**
	 * 推奨設定ボタン.
	 * 
	 * @param e イベント
	 */
	protected void do_btnRecommended_widgetSelected(SelectionEvent e) {

		// 一旦フィルターを解除する.
		ViewerFilter[] viewerFilter = treeViewerLibrary.getFilters();
		treeViewerLibrary.resetFilters();

		// getSelectedLibrarySet().clear();
		// tableSelection.removeAll();

		// RemoteContentManager.getLibraryList();

		// for (TreeItem item : treeLibrary.getItems()) {
		for (TreeItem item2 : treeLibrary.getItems()) {
			for (TreeItem item3 : item2.getItems()) {
				if (item3.getData() instanceof LibraryNode) {
					LibraryNode libraryNode = (LibraryNode) item3.getData();
					if (libraryNode.isRecommended()) {
						if (!libraryNode.isExists()) {
							// 追加.
							item3.setChecked(true);
						}
						addTableItem(libraryNode);

						// 既に古いライブラリが存在している時は削除する.
						for (TreeItem item3a : item2.getItems()) {
							if (item3 != item3a) {
								LibraryNode checkLibraryNode = (LibraryNode) item3a.getData();
								if (item3a.getChecked()) {
									item3a.setChecked(false);
									removeTableItem(checkLibraryNode);
								}
							}
						}
						effectTreeViewParentItem(item3);
						// } else if (libraryNode.isExists()) {
						// getTableItem(libraryNode, LibraryState.REMOVE);
					}
				}
			}
		}
		// }
		tableSelection.layout();

		treeViewerLibrary.setFilters(viewerFilter);
		refreshTreeViewerFilter();

		changeTableSelection();
	}

	/**
	 * 再読込処理.
	 * 
	 * @param e イベント
	 */
	protected void do_btnReload_widgetSelected(SelectionEvent e) {

		// リスト再取得.
		RemoteContentManager.getLibraryList(true);

		// 一覧をダウンロード.
		refreshTreeLibrary(false, true);

	}

	/**
	 * フィルターの更新.
	 * 
	 * @param e イベント
	 */
	protected void do_checkFilterLatest_widgetSelected(SelectionEvent e) {

		// 変更.
		if (e == null || ((Button) e.widget).getSelection()) {
			treeViewerLibrary.setFilters(new ViewerFilter[] { new LibraryTreeLatestViewerFilter() });
		} else {
			treeViewerLibrary.resetFilters();
		}
		refreshTreeViewerFilter();

	}

	/**
	 * フィルター更新に伴うチェックの復元.
	 */
	private void refreshTreeViewerFilter() {

		treeViewerLibrary.refresh(true);

		// チェック復元.
		for (TreeItem treeItem : treeViewerLibrary.getTree().getItems()) {
			for (TreeItem treeItem2 : treeItem.getItems()) {
				if (treeItem2.getData() instanceof LibraryNode) {
					LibraryNode libraryNode = (LibraryNode) treeItem2.getData();
					if (getSelectedLibrarySet().contains(libraryNode)) {
						// 追加.
						if (libraryNode.isAddable()) {
							treeItem2.setChecked(true);
						}
						libraryNode.setState(libraryNode.getState());
					}
					effectTreeViewParentItem(treeItem2);
				}
			}
		}
		treeLibrary.layout();
	}

	/**
	 * LibraryData から TableItem を追加.
	 * 
	 * @param libraryData
	 */
	private void removeTableItem(LibraryNode libraryNode) {

		if (libraryNode.isExists() && !getSelectedLibrarySet().contains(libraryNode)) {
			setTableItem(libraryNode, LibraryState.REMOVE);
		} else {
			if (libraryNode.isExists()) {
				libraryNode.setState(LibraryState.EXISTS);
			} else {
				libraryNode.setState(LibraryState.DEFAULT);
			}
			// 本当に削除.
			getSelectedLibrarySet().remove(libraryNode); // URL
			if (tableSelection.getItemCount() > 0) {
				for (int i = 0; i < tableSelection.getItemCount(); i++) {
					if (tableSelection.getItem(i).getData() == libraryNode) {
						tableSelection.remove(i);
						break;
					}
				}
			}
		}
		changeTableSelection();
	}

	/**
	 * TableItemを追加する.
	 * 
	 * @param libraryData ライブラリデータ
	 * @param libraryState 操作状態
	 */
	private void addTableItem(LibraryNode libraryNode) {

		if (libraryNode.isExists() && getSelectedLibrarySet().contains(libraryNode)) {
			removeTableItem(libraryNode);
		} else if (libraryNode.isExists()) {
			setTableItem(libraryNode, LibraryState.EXISTS);
		} else {
			setTableItem(libraryNode, LibraryState.ADD);
		}
	}

	/**
	 * TableItemを追加または更新する.
	 * 
	 * @param libraryData ライブラリデータ
	 * @param libraryState 操作状態
	 * @return 追加したテーブルItem
	 */
	private TableItem setTableItem(LibraryNode libraryNode, LibraryState state) {

		// 状態を変更.
		libraryNode.setState(state);

		// ファイルが存在する場合.
		if (getSelectedLibrarySet().contains(libraryNode)) { // && state != LibraryState.EXISTS
			// テーブルにもあるので状態変更のみ.
			if (tableSelection.getItemCount() > 0) {
				for (int i = 0; i < tableSelection.getItemCount(); i++) {
					if (tableSelection.getItem(i).getData() == libraryNode) {
						tableSelection.getItem(i).setText(0, state.getText());
						break;
					}
				}
			}
			return null;
		}

		// 色替え.
		// treeItem.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		// treeItem.setFont(new Font(treeItem.getFont().getDevice(), treeItem.getFont().getFontData()[0].getName(),
		// treeItem.getFont().getFontData()[0].getHeight(), SWT.BOLD));
		// Image addImage = H5WizardPlugin.getImage("step_done.gif");
		// treeItem.setImage(addImage);

		if (state == LibraryState.EXISTS) {
			// 存在していたら追加しない
			return null;
		}

		Library library = libraryNode.getValue();
		CategoryNode categoryNode = libraryNode.getParent();
		Category category = categoryNode.getValue();

		// LibraryNOde → Library にする
		// TODO:LabelProvider化
		TableItem tableItem = new TableItem(tableSelection, SWT.NONE);
		tableItem.setText(0, state.getText());
		tableItem.setText(1, category.getName());
		tableItem.setText(2, library.getVersion());
		tableItem.setText(3, categoryNode.getPathLable());
		List<String> files = new ArrayList<String>();
		for (Site site : library.getSite()) {
			if (site.getReplaceFileName() != null) {
				files.add(site.getReplaceFileName());
			} else if (site.getFilePattern() != null) {
				files.add(site.getFilePattern());
			} else {
				try {
					files.add(StringUtils.substringAfterLast(new URL(site.getUrl()).getPath(), "/"));
				} catch (MalformedURLException e) {
					// 無視.
					files.add("---");
				}
			}
		}
		tableItem.setText(4, StringUtils.join(files, ","));
		if (!library.getSite().isEmpty()) {
			tableItem.setText(5, library.getSite().get(0).getUrl() + " ...");
		}
		tableItem.setData(libraryNode);

		getSelectedLibrarySet().add(libraryNode);
		return tableItem;
	}

	protected void changeTableSelection() {

		// 変更を親に通知する
		Event event = new Event();
		event.item = tableSelection;
		event.detail = tableSelection.getItemCount();
		event.type = SWT.CHANGED;
		notifyListeners(event.type, event);
	}

}
