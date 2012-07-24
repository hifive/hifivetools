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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.MultiListProperty;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.internal.ui.JavaPluginImages;
import org.eclipse.wst.jsdt.ui.ISharedImages;
import org.eclipse.wst.jsdt.ui.JavaScriptUI;

import com.htmlhifive.tools.jslint.library.IncludePathEntryWrapper;
import com.htmlhifive.tools.jslint.library.LibraryManager;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * 
 * チェック対象ファイルとライブラリを確認するコンポジット.
 * 
 * @author NS Solutions Corporation
 */
@SuppressWarnings("restriction")
public class TargetStructureComposite extends Group {

	/**
	 * ビルドパスページID.
	 */
	private static final String BUILD_PATHS_PROPERTY_PAGE_ID = "org.eclipse.wst.jsdt.ui.propertyPages.BuildPathsPropertyPage";

	/**
	 * インクルードラベル.
	 */
	private static final String LABEL_INCLUDED = "Included";

	/**
	 * エクスクルードラベル.
	 */
	private static final String LABEL_EXCLUDED = "Excluded";

	/**
	 * Allラベル.
	 */
	private static final String LABEL_ALL = "(All)";

	/**
	 * "None"ラべル.
	 */
	private static final String LABEL_NONE = "(None)";

	/**
	 * 
	 * ライブラリのツリービューアモデル.
	 * 
	 * @author NS Solutions Corporation
	 * 
	 */
	public static class LibraryTreeModel {

		/**
		 * 実際のデータ.
		 */
		private Object data;
		/**
		 * 親モデル.
		 */
		private LibraryTreeModel parent;
		/**
		 * ラベル文字列.
		 */
		private String label;

		/**
		 * コンストラクタ.
		 * 
		 * @param parent 親モデル.
		 * @param data 実際のデータ.
		 * @param label ラベル文字列.
		 */
		public LibraryTreeModel(LibraryTreeModel parent, Object data, String label) {
			this.data = data;
			this.parent = parent;
			this.label = label;
		}

		/**
		 * 実際のデータを取得する.
		 * 
		 * @return 実際のデータ.
		 */
		public Object getData() {
			return data;
		}

		/**
		 * 子供を取得する.
		 * 
		 * @return 子供
		 */
		@SuppressWarnings("unchecked")
		public List<LibraryTreeModel> getChildren() {

			if (parent == null) {
				return (List<LibraryTreeModel>) data;
			}

			if (data instanceof IncludePathEntryWrapper) {
				IncludePathEntryWrapper wrapper = (IncludePathEntryWrapper) data;
				List<LibraryTreeModel> list = new ArrayList<TargetStructureComposite.LibraryTreeModel>();

				IPath[] includePath = wrapper.getFullInclusionPatterns();
				IPath[] excludePath = wrapper.getFullExclusionPatterns();
				list.add(new LibraryTreeModel(this, includePath, LABEL_INCLUDED));
				list.add(new LibraryTreeModel(this, excludePath, LABEL_EXCLUDED));
				return list;
			}
			if (data instanceof IPath[]) {
				IPath[] paths = (IPath[]) data;
				List<LibraryTreeModel> list = new ArrayList<TargetStructureComposite.LibraryTreeModel>();
				if (paths == null || paths.length == 0) {
					String label = null;
					if (StringUtils.equals(this.label, LABEL_INCLUDED)) {
						label = LABEL_ALL;
					} else {
						label = LABEL_NONE;
					}
					list.add(new LibraryTreeModel(this, new Object(), label));
					return list;
				}
				for (IPath iPath : paths) {
					String label = iPath.toString();
					list.add(new LibraryTreeModel(this, iPath, label));
				}
				return list;
			}
			return null;
		}

		/**
		 * 親モデルを取得する.
		 * 
		 * @return 親モデル.
		 */
		public LibraryTreeModel getParent() {
			return parent;
		}

		/**
		 * ラベル文字列を取得する.
		 * 
		 * @return ラベル文字列.
		 */
		public String getLabel() {
			return label;
		}

	}

	/**
	 * プリファレンス(プロパティ)ページのコンテナ.
	 */
	private IWorkbenchPreferenceContainer container;
	/**
	 * javascriptプロジェクト.
	 */
	private IJavaScriptProject project;
	/**
	 * チェック対象ソースビュー.
	 */
	private TreeViewer treeViewerSource;
	/**
	 * ライブラリビュー.
	 */
	private TableViewer tableViewer;

	/**
	 * コンストラクタ.
	 * 
	 * @param parent 親コンポジット.
	 * @param project javascriptプロジェクト.
	 * @param container プリファレンス(プロパティ)ページのコンテナ.
	 */
	public TargetStructureComposite(Composite parent, IJavaScriptProject project,
			IWorkbenchPreferenceContainer container) {
		super(parent, SWT.NONE);
		this.project = project;
		this.container = container;
		createContents();
	}

	@Override
	protected void checkSubclass() {
	}

	/**
	 * コンテンツを生成する.
	 */
	private void createContents() {
		setText(Messages.WT0003.getText());
		setLayout(new GridLayout(1, false));

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Link link = new Link(composite, SWT.NONE);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doLinkWidgetSelected(e);
			}
		});
		link.setSize(84, 18);
		link.setText(Messages.DL0008.getText());

		Group group = new Group(this, SWT.NONE);
		GridData gdGroup = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gdGroup.heightHint = 200;
		gdGroup.widthHint = 300;
		group.setLayoutData(gdGroup);
		group.setText(Messages.WT0002.getText());
		group.setLayout(new GridLayout(1, false));

		treeViewerSource = new TreeViewer(group, SWT.BORDER);
		Tree treeSource = treeViewerSource.getTree();
		GridData gdTreeSource = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gdTreeSource.heightHint = 100;
		treeSource.setLayoutData(gdTreeSource);

		IListProperty childrenProperty = new MultiListProperty(new IListProperty[] { PojoProperties.list("children") });
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				childrenProperty.listFactory(), null);
		treeViewerSource.setContentProvider(contentProvider);

		ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(PojoProperties.value("label")
				.observeDetail(contentProvider.getKnownElements())) {
			@Override
			public Image getImage(Object element) {
				LibraryTreeModel model = (LibraryTreeModel) element;
				Object data = model.getData();
				if (data instanceof IncludePathEntryWrapper) {
					return JavaScriptUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT);
				}
				if (data instanceof IPath) {
					return JavaScriptUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CUNIT);
				}
				if (StringUtils.equals(model.getLabel(), LABEL_INCLUDED)) {
					return JavaPluginImages.DESC_OBJS_INCLUSION_FILTER_ATTRIB.createImage();
				} else if (StringUtils.equals(model.getLabel(), LABEL_EXCLUDED)) {
					return JavaPluginImages.DESC_OBJS_EXCLUSION_FILTER_ATTRIB.createImage();
				}
				return null;
			}
		};
		treeViewerSource.setLabelProvider(labelProvider);
		treeViewerSource.setInput(createSourceViewInput());

		Group grpLibrary = new Group(this, SWT.NONE);
		grpLibrary.setLayout(new GridLayout(1, false));
		GridData gdGrpLibrary = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gdGrpLibrary.heightHint = 200;
		grpLibrary.setLayoutData(gdGrpLibrary);
		grpLibrary.setText(Messages.WT0004.getText());

		tableViewer = new TableViewer(grpLibrary, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gdTable.heightHint = 300;
		table.setLayoutData(gdTable);

		IObservableList observableList = createLibViewInput();
		tableViewer.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				WritableValue value = (WritableValue) element;
				return value.getValue().toString();
			}

			@Override
			public Image getImage(Object element) {
				return JavaScriptUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
			}
		});
		ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
		tableViewer.setContentProvider(listContentProvider);
		tableViewer.setInput(observableList);
	}

	/**
	 * ライブラリビューアのインプットを生成する.
	 * 
	 * @return ライブラリビューアのインプット
	 */
	private IObservableList createLibViewInput() {
		List<WritableValue> libPathList = new ArrayList<WritableValue>();
		LibraryManager manager = LibraryManager.getInstance(project);
		IObservableList observableList = new WritableList(libPathList, WritableValue.class);
		IFile[] internalLibs = manager.getInternalLibPaths();
		for (IFile iFile : internalLibs) {
			observableList.add(new WritableValue(iFile.getFullPath().toString(), String.class));
		}
		File[] externalLibs = manager.getExternalLibFiles();
		for (File file : externalLibs) {
			observableList.add(new WritableValue(file.getPath(), String.class));
		}
		return observableList;
	}

	/**
	 * ソースビューアのインプットを生成する.
	 * 
	 * @return ソースビューアのインプット.
	 */
	private Object createSourceViewInput() {
		List<LibraryTreeModel> list = new ArrayList<TargetStructureComposite.LibraryTreeModel>();
		LibraryTreeModel root = new LibraryTreeModel(null, list, "root");
		LibraryManager manager = LibraryManager.getInstance(project);
		IncludePathEntryWrapper[] entrys = manager.getIncludePathEntries();
		for (IncludePathEntryWrapper includePathEntryWrapper : entrys) {
			if (includePathEntryWrapper.getEntryKind() == IncludePathEntryWrapper.CPE_SOURCE) {
				list.add(new LibraryTreeModel(root, includePathEntryWrapper, includePathEntryWrapper.getPath()
						.toString()));
			}
		}
		return root;
	}

	/**
	 * リンク押下時の処理.
	 * 
	 * @param e イベント.
	 */
	protected void doLinkWidgetSelected(SelectionEvent e) {
		container.openPage(BUILD_PATHS_PROPERTY_PAGE_ID, null);
	}

	/**
	 * ビューワをリフレッシュする.
	 */
	public void refreshViewer() {
		treeViewerSource.setInput(createSourceViewInput());
		treeViewerSource.refresh();
		tableViewer.setInput(createLibViewInput());
		tableViewer.refresh();
	}
}
