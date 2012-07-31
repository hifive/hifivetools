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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.htmlhifive.tools.wizard.H5WizardPlugin;
import com.htmlhifive.tools.wizard.RemoteContentManager;
import com.htmlhifive.tools.wizard.library.model.LibraryList;
import com.htmlhifive.tools.wizard.library.model.xml.Category;
import com.htmlhifive.tools.wizard.log.messages.Messages;
import com.htmlhifive.tools.wizard.ui.UIMessages;
import com.htmlhifive.tools.wizard.ui.page.tree.CategoryNode;
import com.htmlhifive.tools.wizard.ui.page.tree.LibraryNode;
import com.htmlhifive.tools.wizard.utils.H5LogUtils;

/**
 * <H3>ライセンス確認コンポジット.</H3>
 * 
 * @author fkubo
 */
public class ConfirmLicenseComposite extends Composite {

	private final TabFolder tabFolder;
	private Button btnRadioAccept = null;
	final Set<Category> categorySet = new LinkedHashSet<Category>();

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ConfirmLicenseComposite(Composite parent, int style) {

		super(parent, style);
		setLayout(new GridLayout(1, false));

		Label lblHifiveLicense = new Label(this, SWT.NONE);
		lblHifiveLicense.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblHifiveLicense.setText(UIMessages.ConfirmLicenseComposite_lblHifiveLicense_text);

		tabFolder = new TabFolder(this, SWT.NONE);
		GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_tabFolder.widthHint = 400;
		tabFolder.setLayoutData(gd_tabFolder);

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		composite.setLayout(new FillLayout(SWT.VERTICAL));

		btnRadioAccept = new Button(composite, SWT.RADIO);
		btnRadioAccept.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				do_btnRadioAccept_widgetSelected(e);
			}
		});
		btnRadioAccept.setText(UIMessages.ConfirmLicenseComposite_btnAcceptButton_text);

		Button btnRadioReject = new Button(composite, SWT.RADIO);
		btnRadioReject.setSelection(true);
		btnRadioReject.setText(UIMessages.ConfirmLicenseComposite_btnRejectButton_text);

		// 初期設定.
		setLiceseContents();
	}

	/**
	 * ライセンス表示更新処理.
	 */
	void setLiceseContents() {

		LibraryList libraryList = RemoteContentManager.getLibraryList();

		for (Category category : libraryList.getLibraries().getSiteLibraries().getCategory()) {
			boolean isShow = false;
			for (LibraryNode libraryNode : H5WizardPlugin.getInstance().getSelectedLibrarySet()) {
				if (category == libraryNode.getParent().getValue()){
					// 今までに表示してなくて、追加するもののみが表示する対象.
					isShow = true;
					if (!categorySet.contains(category) && libraryNode.isAddable()) {
						CategoryNode categoryNode = libraryNode.getParent();

						// 無い場合に追加.
						TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
						tabItem.setText(categoryNode.getLabel());
						tabItem.setData(category);

						if (category.getLicenseUrl() != null) {
							Composite composite = new Composite(tabFolder, SWT.NONE);
							composite.setLayout(new GridLayout(1, false));
							tabItem.setControl(composite);

							ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL);
							// new ScrolledComposite(tabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
							scrolledComposite.setExpandHorizontal(true);
							scrolledComposite.setExpandVertical(true);
							scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

							Link link = new Link(composite, SWT.NONE);
							link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							link.setText("URL: <a>" + category.getLicenseUrl() + "</a>");
							link.addSelectionListener(new SelectionAdapter() {

								@Override
								public void widgetSelected(SelectionEvent e) {

									if (e.text.startsWith("http://") || e.text.startsWith("https://")) {
										// 安全のためとりあえずは、http, httpsだけ
										Program.launch(e.text);
									}
								}
							});

							// Browser browser = new Browser(scrolledComposite, SWT.BORDER);
							Browser browser = new Browser(scrolledComposite, SWT.NONE);
							scrolledComposite.setContent(browser);
							scrolledComposite.setMinSize(browser.computeSize(SWT.DEFAULT, SWT.DEFAULT));

							boolean result = browser.setUrl(category.getLicenseUrl());
							//H5LogUtils.putLog(null, Messages.PI0136,category.getLicenseUrl(), result);
							browser.setVisible(result);
						} else {
							Link link = new Link(tabFolder, SWT.NONE);
							// link.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
							tabItem.setControl(link);
							if (category.getLicense() != null) {
								link.setText(category.getLicense());
							}
							link.addSelectionListener(new SelectionAdapter() {

								@Override
								public void widgetSelected(SelectionEvent e) {

									if (e.text.startsWith("http://") || e.text.startsWith("https://")) {
										// 安全のためとりあえずは、http, httpsだけ
										Program.launch(e.text);
									}
								}
							});
						}
						categorySet.add(category);
					}
				}
			}
			if (!isShow) {
				categorySet.remove(category);
			}
		}

		// 不要なものを削除.
		for (TabItem item : tabFolder.getItems()) {
			if (!categorySet.contains(item.getData())) {
				// 不要.
				categorySet.remove(item.getData());
				item.dispose();
			}
		}
	}

	void clearCategory() {

		categorySet.clear();
		// 不要なものを削除.
		for (TabItem item : tabFolder.getItems()) {
			// 不要.
			item.dispose();
		}
	}

	/**
	 * チェックボックス変更イベント.
	 * 
	 * @param e イベント
	 */
	protected void do_btnRadioAccept_widgetSelected(SelectionEvent e) {

		changePageStatus(((Button) e.widget).getSelection());
	}

	/**
	 * pageへのメッセージ追加イベント発生用.
	 * 
	 * @param status 状態.
	 */
	public void changePageStatus(boolean status) {

		Event event = new Event();
		event.type = SWT.ERROR_UNSPECIFIED;
		event.doit = status;
		notifyListeners(event.type, event);
	}

	/**
	 * 選択されているかどうか.バグ対策.
	 * 
	 * @return 選択されているかどうか.
	 */
	public boolean isAccepted() {

		return btnRadioAccept.getSelection();
	}
}
