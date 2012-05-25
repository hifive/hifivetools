/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */
package jp.co.nssol.h5.tools.codeassist.ui.messages;

import java.util.ResourceBundle;

import jp.co.nssol.h5.tools.codeassist.core.messages.MessagesBase;

/**
 * UIMessages.
 * 
 * @author MessageGenerator
 */
public final class UIMessages extends MessagesBase {

	// 必ず定数定義より先に呼び出すこと.
	static {
		// このクラスを登録する.
		addResourceBundle(ResourceBundle.getBundle("jp.co.nssol.h5.tools.codeassist.ui.messages.messages"));
	}

	// #
	// # Copyright (C) 2012 NS Solutions Corporation
	// #
	// # Licensed under the Apache License, Version 2.0 (the "License");
	// # you may not use this file except in compliance with the License.
	// # You may obtain a copy of the License at
	// #
	// #    http://www.apache.org/licenses/LICENSE-2.0
	// #
	// # Unless required by applicable law or agreed to in writing, software
	// # distributed under the License is distributed on an "AS IS" BASIS,
	// # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	// # See the License for the specific language governing permissions and
	// # limitations under the License.
	// #
	// #
	// #エラーメッセージ
	/** UIEM0001=ERROR,コンポジットの生成に失敗しました.クラス名 : [{0}]. */
	public static final Message UIEM0001 = createMessage("UIEM0001");

	/** UIEM0002=ERROR,設定ファイルの読み込みに失敗しました.. */
	public static final Message UIEM0002 = createMessage("UIEM0002");

	/** UIEM0003=ERROR,コード補完抽出に失敗しました.. */
	public static final Message UIEM0003 = createMessage("UIEM0003");

	/** UIEM0004=WARN,指定されたパスにファイルが存在しません.. */
	public static final Message UIEM0004 = createMessage("UIEM0004");

	/** UIEM0005=WARN,"xmlファイルを指定してください.". */
	public static final Message UIEM0005 = createMessage("UIEM0005");

	// #ダイアログタイトル
	/** UIDT0001=INFO,オプションファイルの選択. */
	public static final Message UIDT0001 = createMessage("UIDT0001");

	/** UIDT0002=INFO,エラーダイアログ.. */
	public static final Message UIDT0002 = createMessage("UIDT0002");

	// #ダイアログメッセージラベル
	/** UIDM0001=INFO,オプションファイルを選択してください.. */
	public static final Message UIDM0001 = createMessage("UIDM0001");

	// #ボタン名
	/** UIBT0001=INFO,選択. */
	public static final Message UIBT0001 = createMessage("UIBT0001");

	// #コンポジットラベル.
	/** UICL0001=INFO,オプションファイルの設定(特に指定がなければ空を指定してください). */
	public static final Message UICL0001 = createMessage("UICL0001");

	/** UICL0002=INFO,オプションファイル :. */
	public static final Message UICL0002 = createMessage("UICL0002");

	/**
	 * デフォルトコンストラクタ.
	 */
	private UIMessages() {
		// no create
	}
}
