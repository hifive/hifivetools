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
package com.htmlhifive.tools.wizard.ui;

import org.eclipse.osgi.util.NLS;

public class UIMessages extends NLS {

	private static final String BUNDLE_NAME = "com.htmlhifive.tools.wizard.ui.ui_messages"; //$NON-NLS-1$
	public static String ConfirmLicenseComposite_btnAcceptButton_text;
	public static String ConfirmLicenseComposite_btnRejectButton_text;
	public static String ConfirmLicenseComposite_lblHifiveLicense_text;
	public static String ConfirmLicensePage_this_message;
	public static String Dialog_ALL_IGNORE;
	public static String Dialog_ALL_OVERWRITE;
	public static String Dialog_IGNORE;
	public static String Dialog_OVERWRITE;
	public static String Dialog_RETRY;
	public static String Dialog_STOP;
	public static String LibraryImportComposite_btnRecommended_text;
	public static String LibraryImportComposite_btnReload_text;
	public static String LibraryImportComposite_checkFilterInstalled_text;
	public static String LibraryImportComposite_checkFilterLatest_text;
	public static String LibraryImportComposite_grpDetails_text;
	public static String LibraryImportComposite_lblNewLabel_text;
	public static String LibraryImportComposite_tblclmnCategory_text;
	public static String LibraryImportComposite_tblclmnFiles_text;
	public static String LibraryImportComposite_tblclmnPath_text;
	public static String LibraryImportComposite_tblclmnStatus_text;
	public static String LibraryImportComposite_tblclmnUrl_text;
	public static String LibraryImportComposite_tblclmnVersion_text;
	public static String LibraryImportComposite_treeClmnPath_text;
	public static String LibraryImportComposite_treeClmnTitle_text;
	public static String LibraryImportComposite_treeColumn_text;
	public static String LibraryImportComposite_treeColumn2_text;
	public static String LibraryImportPage_this_message;
	public static String LibraryImportPage_this_title;
	public static String LibraryImportPageComposite_groupAll_text;
	public static String LibraryImportPageComposite_groupSelect_text;
	public static String LibraryImportPageComposite_label_text;
	public static String LibraryImportPageComposite_tree_root_text;
	public static String LibraryState_ADD;
	public static String LibraryState_ERROR;
	public static String LibraryState_EXIST;
	public static String LibraryState_INCOMPLETE;
	public static String LibraryState_REMOVE;
	public static String LibraryState2_INSTALLED;
	public static String LibraryState2_RECOMMENDED;
	public static String LicenseListPage_this_message;
	public static String LicenseListPage_this_title;
	public static String ResultComposite_labelLog_text;
	public static String ResultPage_this_message;
	public static String ResultPage_this_title;
	public static String StructureSelectComposite_buttonReload_text;
	public static String StructureSelectComposite_group_text;
	public static String StructureSelectComposite_lblInfo_text;
	public static String StructureSelectComposite_lblNewLabel_text;
	public static String StructureSelectComposite_structureGroup_text;
	public static String StructureSelectPage_this_message;
	public static String StructureSelectPage_this_title;
	public static String LibraryImportComposite_lblDefaultInstallPath_text;
	public static String StructureSelectComposite_lblLbllistinfo_text;
	public static String LibraryImportComposite_lblNewLabel_text_1;

	// //////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// //////////////////////////////////////////////////////////////////////////
	private UIMessages() {

		// do not instantiate
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Class initialization
	//
	// //////////////////////////////////////////////////////////////////////////
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, UIMessages.class);
	}
}
