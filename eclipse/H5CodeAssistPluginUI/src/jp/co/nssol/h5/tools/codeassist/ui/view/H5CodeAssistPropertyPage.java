/*
 * Copyright (C) 2012 NS Solutions Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package jp.co.nssol.h5.tools.codeassist.ui.view;

import jp.co.nssol.h5.tools.codeassist.ui.config.CodeAssistConfigManager;
import jp.co.nssol.h5.tools.codeassist.ui.messages.UIMessages;
import jp.co.nssol.h5.tools.codeassist.ui.view.bean.BeanChangeEvent;
import jp.co.nssol.h5.tools.codeassist.ui.view.bean.BeanChangedListner;
import jp.co.nssol.h5.tools.codeassist.ui.view.bean.OptionConfigureBean;
import jp.co.nssol.h5.tools.codeassist.ui.view.bean.BeanChangeEvent.CompositeBean;
import jp.co.nssol.h5.tools.codeassist.core.H5CodeAssistCorePluginConst;
import jp.co.nssol.h5.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import jp.co.nssol.h5.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Hi5コードアシストの設定ページ.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class H5CodeAssistPropertyPage extends PropertyPage {

	/**
	 * ロガー.
	 */
	private static H5CodeAssistPluginLogger logger = H5CodeAssistPluginLoggerFactory
			.getLogger(H5CodeAssistPropertyPage.class);

	/**
	 * オプションコンポジット.
	 */
	private OptionConfigureComposite composite;

	/**
	 * コンストラクタ.
	 */
	public H5CodeAssistPropertyPage() {

		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		noDefaultAndApplyButton();
		try {
			createComposite(composite);
		} catch (CoreException e) {
			logger.log(UIMessages.UIEM0001, e, composite.getClass());
			ErrorDialog.openError(getShell(), UIMessages.UIDT0002.getText(),
					UIMessages.UIEM0001.format(composite.getClass()), e.getStatus());
		}
		return composite;
	}

	/**
	 * コンポジットを生成します.
	 * 
	 * @param parent 親コンポジット.
	 * @throws CoreException 生成例外.
	 */
	private void createComposite(Composite parent) throws CoreException {

		composite = new OptionConfigureComposite(parent, getProject());
		composite.addBeanChangedListner(new BeanChangedListner() {

			@Override
			public void beanChanged(BeanChangeEvent event) {

				CompositeBean bean = event.getChangedBean();
				if (bean instanceof OptionConfigureBean) {
					OptionConfigureBean optionBean = (OptionConfigureBean) bean;
					optionFileValidate(optionBean);
					return;
				}
				throw new IllegalArgumentException();
			}
		});

	}

	/**
	 * 入力項目を検証する.<br>
	 * 
	 * 
	 * @param bean オプションファイルビーン.
	 */
	private void optionFileValidate(OptionConfigureBean bean) {

		String optionFilePath = bean.getOptionFilePath();
		if (StringUtils.isEmpty(optionFilePath)) {
			setValid(true);
			setErrorMessage(null);
			return;
		}
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(optionFilePath);
		if (resource == null || !(resource instanceof IFile)) {
			setValid(false);
			setErrorMessage(UIMessages.UIEM0004.getText());
			return;
		}
		if (!StringUtils.equals(resource.getFileExtension(), H5CodeAssistCorePluginConst.EXTENTION_XML)) {
			setValid(false);
			setErrorMessage(UIMessages.UIEM0005.getText());
			return;
		}
		setValid(true);
		setErrorMessage(null);
		return;

	}

	@Override
	public boolean performOk() {

		IProject project = getProject();
		// project.setPersistentProperty(H5CodeAssistUIPluginConst.GET_OPTION_QUALIFIED_NAME,
		// composite.getBean()
		// .getOptionFilePath());
		return CodeAssistConfigManager.saveConfig(project);

	}

	/**
	 * プロジェクトを取得する.
	 * 
	 * @return プロジェクト.
	 */
	private IProject getProject() {

		IAdaptable adapt = getElement();
		if (adapt instanceof IProject) {
			return (IProject) adapt;
		}
		if (adapt instanceof IJavaProject) {
			return (IProject) adapt.getAdapter(IProject.class);
		}
		return null;

	}
}