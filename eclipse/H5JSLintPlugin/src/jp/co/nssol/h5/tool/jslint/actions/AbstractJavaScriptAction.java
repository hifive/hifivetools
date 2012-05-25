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
 package jp.co.nssol.h5.tool.jslint.actions;

import jp.co.nssol.h5.tool.jslint.dialog.JSLintStatusDialog;
import jp.co.nssol.h5.tool.jslint.dialog.StatusList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * javascriptの検査、またはクリアするクラスの抽象クラス.<br>
 * エディタ、またはオブジェクト選択時のアクション.
 * 
 * @author NS Solutions Corporation
 */
public abstract class AbstractJavaScriptAction implements IObjectActionDelegate, IEditorActionDelegate {

	/**
	 * 検査対象リソース.
	 */
	private IResource resource;
	/**
	 * シェル.
	 */
	private Shell shell;

	@Override
	public void run(IAction action) {

		StatusList statusList = new StatusList();
		doRun(action, statusList);
		JSLintStatusDialog dialog = new JSLintStatusDialog(getShell(), statusList);
		dialog.open();
	}

	/**
	 * アクションメソッド.実行した時の内容を記述.<br>
	 * statusListにIStatusを加えることで情報、警告、又はエラーダイアログを表示することができる.
	 * 
	 * @param action アクション.
	 * @param statusList ステータスリスト.
	 */
	protected abstract void doRun(IAction action, StatusList statusList);

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection select = (IStructuredSelection) selection;
			if (select.getFirstElement() instanceof IResource) {
				resource = (IResource) select.getFirstElement();
			} else if (select.getFirstElement() instanceof IJavaProject) {
				IJavaProject project = (IJavaProject) select.getFirstElement();
				resource = project.getProject();
			}
		}

	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface
	 * .action.IAction, org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {

		if (targetEditor != null && targetEditor.getEditorInput() instanceof FileEditorInput) {
			FileEditorInput editor = (FileEditorInput) targetEditor.getEditorInput();
			resource = editor.getFile();
		}
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

		shell = targetPart.getSite().getShell();
	}

	/**
	 * 検査対象リソースを取得します.
	 * 
	 * @return 検査対象リソース.
	 */
	IResource getResource() {

		return resource;
	}

	/**
	 * シェルを取得する.
	 * 
	 * @return シェル.
	 */
	Shell getShell() {

		return shell;
	}

}
