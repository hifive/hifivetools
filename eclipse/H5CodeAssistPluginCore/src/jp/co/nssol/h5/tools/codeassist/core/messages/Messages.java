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
 package jp.co.nssol.h5.tools.codeassist.core.messages;

import java.util.ResourceBundle;

/**
 * Messages.
 * 
 * @author MessageGenerator
 */
public final class Messages extends MessagesBase {

	// 必ず定数定義より先に呼び出すこと.
	static {
		// このクラスを登録する.
		addResourceBundle(ResourceBundle.getBundle("jp.co.nssol.h5.tools.codeassist.core.messages.messages"));
	}

	// #エラーメッセージ
	/** EM0001=ERROR,オプションファイルの解析に失敗しました.[オプションファイル:{0}]. */
	public static final Message EM0001 = createMessage("EM0001");

	/** EM0002=ERROR,コード補完情報生成に失敗しました.. */
	public static final Message EM0002 = createMessage("EM0002");

	/** EM0003=ERROR,xmlファイルの解析に失敗しました.. */
	public static final Message EM0003 = createMessage("EM0003");

	/** EM0004=ERROR,インスタンスの生成に失敗しました.. */
	public static final Message EM0004 = createMessage("EM0004");

	/** EM0005=ERROR,コード補完呼び出しに失敗しました.. */
	public static final Message EM0005 = createMessage("EM0005");

	/** EM0006=ERROR,コード補完の振り分け処理で失敗しました.. */
	public static final Message EM0006 = createMessage("EM0006");

	/** EM0007=DEBUG,コードビルダータイプが{0}の場合はダミーコード情報はDelegateDummyCodeInfoインスタンスである必要があります.. */
	public static final Message EM0007 = createMessage("EM0007");

	// #デバッグメッセージ
	/** DB0001=DEBUG,DummyCreateflg : [{0}]. */
	public static final Message DB0001 = createMessage("DB0001");

	/** DB0002=DEBUG,DefaultAssistExistFlg : [{0}]. */
	public static final Message DB0002 = createMessage("DB0002");

	/** DB0003=DEBUG,addedMethodName : [{0}]. */
	public static final Message DB0003 = createMessage("DB0003");

	/** DB0004=DEBUG,CheckCodeAssistNodeFlg : [{0}]. */
	public static final Message DB0004 = createMessage("DB0004");

	/** DB0005=DEBUG,MemberAccessStr : [{0}]. */
	public static final Message DB0005 = createMessage("DB0005");

	/** DB0006=DEBUG,ObjType : [xxxController]. */
	public static final Message DB0006 = createMessage("DB0006");

	/** DB0007=DEBUG,ObjType : [this]. */
	public static final Message DB0007 = createMessage("DB0007");

	/** DB0008=DEBUG,ObjType : [Other]. */
	public static final Message DB0008 = createMessage("DB0008");

	// #情報メッセージ.
	/** IF0001=INFO,suffix [{0}] start.. */
	public static final Message IF0001 = createMessage("IF0001");

	/** IF0002=INFO,suffix [{0}] end.. */
	public static final Message IF0002 = createMessage("IF0002");

	// #デスクリプションメッセージ
	/** DES0001=INFO,パラメータ:. */
	public static final Message DES0001 = createMessage("DES0001");

	/** DES0002=INFO,戻り値:. */
	public static final Message DES0002 = createMessage("DES0002");

	/**
	 * デフォルトコンストラクタ.
	 */
	private Messages() {
		// no create
	}
}
