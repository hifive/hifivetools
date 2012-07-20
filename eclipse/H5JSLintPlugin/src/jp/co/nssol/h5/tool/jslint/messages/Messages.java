/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */
package jp.co.nssol.h5.tool.jslint.messages;

/**
 * Messages.
 * 
 * @author MessageGenerator
 */
public final class Messages extends MessagesBase {

	// 必ず定数定義より先に呼び出すこと.
	static {
		// このクラスを登録する.
		// このクラスを登録する.
		addResourceBundleString("jp.co.nssol.h5.tool.jslint.messages.messages");
	}

	// #
	// # Copyright (C) 2012 NS Solutions Corporation
	// #
	// # Licensed under the Apache License, Version 2.0 (the "License");
	// # you may not use this file except in compliance with the License.
	// # You may obtain a copy of the License at
	// #
	// # http://www.apache.org/licenses/LICENSE-2.0
	// #
	// # Unless required by applicable law or agreed to in writing, software
	// # distributed under the License is distributed on an "AS IS" BASIS,
	// # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
	// implied.
	// # See the License for the specific language governing permissions and
	// # limitations under the License.
	// #
	// #
	// #項目関連
	/** E0000=INFO,JSLintファイル. */
	public static final Message E0000 = createMessage("E0000");

	/** E0001=INFO,オプションファイル. */
	public static final Message E0001 = createMessage("E0001");

	// #ダイアログボタン
	/** B0001=INFO,選択. */
	public static final Message B0001 = createMessage("B0001");

	/** B0002=INFO,追加. */
	public static final Message B0002 = createMessage("B0002");

	/** B0003=INFO,除去. */
	public static final Message B0003 = createMessage("B0003");

	/** B0004=INFO,編集. */
	public static final Message B0004 = createMessage("B0004");

	// #ダイアログラベル
	/** DL0000=INFO,利用プロジェクト. */
	public static final Message DL0000 = createMessage("DL0000");

	/** DL0001=INFO,JSLint設定ファイルパス. */
	public static final Message DL0001 = createMessage("DL0001");

	/** DL0002=INFO,JSLintファイルパス. */
	public static final Message DL0002 = createMessage("DL0002");

	/** DL0003=INFO,他プロジェクトの設定を利用する.. */
	public static final Message DL0003 = createMessage("DL0003");

	/** DL0004=INFO,JSLintオプション設定. */
	public static final Message DL0004 = createMessage("DL0004");

	/** DL0005=INFO,JSLintオプションを設定します.. */
	public static final Message DL0005 = createMessage("DL0005");

	/** DL0006=INFO,フィルタリング. */
	public static final Message DL0006 = createMessage("DL0006");

	/** DL0007=INFO,エラーメッセｰジを正規表現で比較し、マッチしたものを分類します。. */
	public static final Message DL0007 = createMessage("DL0007");

	// #ダイアログタイトル
	/** DT0000=INFO,プロジェクトの選択. */
	public static final Message DT0000 = createMessage("DT0000");

	/** DT0001=INFO,JSLint設定ファイルの選択. */
	public static final Message DT0001 = createMessage("DT0001");

	/** DT0002=INFO,jsファイルの選択. */
	public static final Message DT0002 = createMessage("DT0002");

	/** DT0003=INFO,エラーダイアログ. */
	public static final Message DT0003 = createMessage("DT0003");

	/** DT0004=INFO,警告ダイアログ. */
	public static final Message DT0004 = createMessage("DT0004");

	/** DT0005=INFO,情報ダイアログ. */
	public static final Message DT0005 = createMessage("DT0005");

	/** DT0006=INFO,オプションの設定. */
	public static final Message DT0006 = createMessage("DT0006");

	// #ウィジェットタイトル
	/** WT0000=INFO,JSLint設定一覧. */
	public static final Message WT0000 = createMessage("WT0000");

	/** WT0001=INFO,詳細説明. */
	public static final Message WT0001 = createMessage("WT0001");

	// #テーブルカラム名
	/** TC0000=INFO,有効. */
	public static final Message TC0000 = createMessage("TC0000");

	/** TC0001=INFO,キー. */
	public static final Message TC0001 = createMessage("TC0001");

	/** TC0002=INFO,説明. */
	public static final Message TC0002 = createMessage("TC0002");

	/** TC0003=INFO,値. */
	public static final Message TC0003 = createMessage("TC0003");

	/** TC0004=INFO,正規表現. */
	public static final Message TC0004 = createMessage("TC0004");

	/** TC0005=INFO,分類. */
	public static final Message TC0005 = createMessage("TC0005");

	// #ダイアログメッセージ
	/** DM0000=INFO,フィルター(* = 任意のストリング,? = 任意の文字). */
	public static final Message DM0000 = createMessage("DM0000");

	/** DM0001=INFO,{0}を選択してください.. */
	public static final Message DM0001 = createMessage("DM0001");

	/** DM0002=INFO,処理が完了しました.. */
	public static final Message DM0002 = createMessage("DM0002");

	/** DM0003=INFO,警告があります.. */
	public static final Message DM0003 = createMessage("DM0003");

	// #タスクメッセージ
	/** T0000=INFO,jsファイルの解析を行います.. */
	public static final Message T0000 = createMessage("T0000");

	/** T0001=INFO,jsファイルの取得. */
	public static final Message T0001 = createMessage("T0001");

	/** T0002=INFO,オプションファイル.JSLint.jsの読み込み.. */
	public static final Message T0002 = createMessage("T0002");

	/** T0003=INFO,jsファイルの解析. */
	public static final Message T0003 = createMessage("T0003");

	/** T0004=INFO,終了. */
	public static final Message T0004 = createMessage("T0004");

	/** T0005=INFO,ワークスペースをビルド中.. */
	public static final Message T0005 = createMessage("T0005");

	// #コンボボックステキスト
	/** CT0000=INFO,無視. */
	public static final Message CT0000 = createMessage("CT0000");

	/** CT0001=INFO,エラー. */
	public static final Message CT0001 = createMessage("CT0001");

	// #エラーメッセージ
	/** EM0000=WARN,{0}が存在しません.. */
	public static final Message EM0000 = createMessage("EM0000");

	/** EM0001=WARN,処理に失敗しました.. */
	public static final Message EM0001 = createMessage("EM0001");

	/** EM0002=WARN,選択アイテムが不適切です.. */
	public static final Message EM0002 = createMessage("EM0002");

	/** EM0003=WARN,スクリプトの読み込みに失敗しました.　ファイル名　[{0}]. */
	public static final Message EM0003 = createMessage("EM0003");

	/** EM0004=WARN,適切なエンジンファイル(jslint.js又はjshint.js)を設定してください.. */
	public static final Message EM0004 = createMessage("EM0004");

	/** EM0005=WARN,数字を入力してください。キー名 : [{0}]. */
	public static final Message EM0005 = createMessage("EM0005");

	/** EM0006=WARN,xmlファイルの保存に失敗しました。 : [{0}]. */
	public static final Message EM0006 = createMessage("EM0006");

	/** EM0007=WARN,オプションに設定した型が存在しません　: [{0}]. */
	public static final Message EM0007 = createMessage("EM0007");

	/** EM0008=WARN,適切なxmlファイルを選択してください.. */
	public static final Message EM0008 = createMessage("EM0008");

	/** EM0009=WARN,{0}を指定してください.. */
	public static final Message EM0009 = createMessage("EM0009");

	/** EM0100=ERROR,予期せぬエラーが発生しました.. */
	public static final Message EM0100 = createMessage("EM0100");

	// #ビューメッセージ
	/** VM0000=INFO,行　{0}. */
	public static final Message VM0000 = createMessage("VM0000");

	// ####オプション説明
	// #JSLint、JSHint共通
	/** DES_BITWISE=INFO,ビット演算子の仕様を禁止.. */
	public static final Message DES_BITWISE = createMessage("DES_BITWISE");

	/** DES_BROUSER=INFO,標準ブラウザでの実行を想定(documentやwindow等、のグローバル変数が事前に定義されているか.). */
	public static final Message DES_BROUSER = createMessage("DES_BROUSER");

	/** DES_DEBUG=INFO,debuggerキーワードを許容.. */
	public static final Message DES_DEBUG = createMessage("DES_DEBUG");

	/** DES_DEVEL=INFO,alert、confirm、console、Debug、opera、promptを使用してもよいか. */
	public static final Message DES_DEVEL = createMessage("DES_DEVEL");

	/** DES_ES5=INFO,es5での実行を想定(es5の文法を使用してもよいか.). */
	public static final Message DES_ES5 = createMessage("DES_ES5");

	/** DES_EVIL=INFO,eval関数を許容.. */
	public static final Message DES_EVIL = createMessage("DES_EVIL");

	/** DES_FORIN=INFO,プロパティを持っていない場合のfor inを禁止.. */
	public static final Message DES_FORIN = createMessage("DES_FORIN");

	/** DES_MAXERR=INFO,許容するエラーの最大数(指定なし：50個). */
	public static final Message DES_MAXERR = createMessage("DES_MAXERR");

	/** DES_NEWCAP=INFO,コンストラクタ名の最初の文字の大文字チェック.. */
	public static final Message DES_NEWCAP = createMessage("DES_NEWCAP");

	/** DES_NODE=INFO,node.jsでの実行を想定(node.jsのグローバル変数が事前に定義されているか.). */
	public static final Message DES_NODE = createMessage("DES_NODE");

	/** DES_NOMEN=INFO,名前の先頭または末尾のアンダーバーを禁止.. */
	public static final Message DES_NOMEN = createMessage("DES_NOMEN");

	/** DES_PASSFAIL=INFO,1個目のエラーでチェックをを止める.. */
	public static final Message DES_PASSFAIL = createMessage("DES_PASSFAIL");

	/** DES_PLUSPLUS=INFO, インクリメント(++)・デクリメント(--)を使用を禁止.. */
	public static final Message DES_PLUSPLUS = createMessage("DES_PLUSPLUS");

	/** DES_REGEXP=INFO,正規表現の . (任意の1文字)と [^...] (指定した文字以外)の使用禁止. */
	public static final Message DES_REGEXP = createMessage("DES_REGEXP");

	/** DES_RHINO=INFO,Rhinoでの実行を想定(Rhinoのグローバル変数が事前に定義されているか.). */
	public static final Message DES_RHINO = createMessage("DES_RHINO");

	/** DES_SUB=INFO,オブジェクトのプロパティへのアクセスは、ドットではなくて括弧記法を許容.. */
	public static final Message DES_SUB = createMessage("DES_SUB");

	/** DES_UNDEF=INFO,変数または関数の宣言があるかをチェックする. */
	public static final Message DES_UNDEF = createMessage("DES_UNDEF");

	/** DES_WHITE=INFO,いい加減な空白をチェック.. */
	public static final Message DES_WHITE = createMessage("DES_WHITE");

	// #JSLintオプション
	/** DES_ADSAFE=INFO,ADsafe のルールを適用するか. */
	public static final Message DES_ADSAFE = createMessage("DES_ADSAFE");

	/** DES_CAP=INFO,HTMLタグが大文字で書かれているか. */
	public static final Message DES_CAP = createMessage("DES_CAP");

	/** DES_CONFUSION=INFO,ある変数の型が途中で変わってもいいか. */
	public static final Message DES_CONFUSION = createMessage("DES_CONFUSION");

	/** DES_CONTINUE=INFO,continueを使用してもよいか. */
	public static final Message DES_CONTINUE = createMessage("DES_CONTINUE");

	/** DES_CSS=INFO,CSSの1行目に @charset 'UTF-8' が無くてもよいか. */
	public static final Message DES_CSS = createMessage("DES_CSS");

	/** DES_EQEQ=INFO,抽象比較( = または != )を使用してもよいか. */
	public static final Message DES_EQEQ = createMessage("DES_EQEQ");

	/** DES_FRAGMENT=INFO,HTMLでフラグメント識別子の使用してもよいか. */
	public static final Message DES_FRAGMENT = createMessage("DES_FRAGMENT");

	/** DES_INDENT=INFO,インデント幅が指定した幅になっているか(0の場合はチェックを行わない). */
	public static final Message DES_INDENT = createMessage("DES_INDENT");

	/** DES_MAXLEN=INFO,許容するソースコード行数(指定なし：全行). */
	public static final Message DES_MAXLEN = createMessage("DES_MAXLEN");

	/** DES_ON=INFO,HTMLでイベントハンドラーの登録をしてもよいか. */
	public static final Message DES_ON = createMessage("DES_ON");

	/** DES_PROPERTIES=INFO,\/*properties*\/コメントを利用して全プロパティのスペルミスをチェックするか. */
	public static final Message DES_PROPERTIES = createMessage("DES_PROPERTIES");

	/** DES_SAFE=INFO,ADsafeルールのうち、ウィジェット向けのルール以外を有効にするか. */
	public static final Message DES_SAFE = createMessage("DES_SAFE");

	/** DES_SLOPPY=INFO,'use strict' が無くてもよいか. */
	public static final Message DES_SLOPPY = createMessage("DES_SLOPPY");

	/** DES_UNPARAM=INFO,未使用の変数があってもよいか. */
	public static final Message DES_UNPARAM = createMessage("DES_UNPARAM");

	/** DES_VARS=INFO,1つのfunction内にvarステートメントが2つ以上あってもよいか. */
	public static final Message DES_VARS = createMessage("DES_VARS");

	/** DES_WIDGET=INFO,Yahoo Widget環境で提供されているグローバル変数・関数があらかじめ定義されていると仮定するか. */
	public static final Message DES_WIDGET = createMessage("DES_WIDGET");

	/** DES_WINDOWS=INFO,Windows固有のグローバル変数・関数があらかじめ定義されていると仮定するか. */
	public static final Message DES_WINDOWS = createMessage("DES_WINDOWS");

	// #JSHintオプション
	/** DES_JQUERY=INFO,jQueryでの実行を想定(jQueryのグローバル変数が事前に定義されているか.). */
	public static final Message DES_JQUERY = createMessage("DES_JQUERY");

	/** DES_COUCH=INFO,CouchDBでの実行を想定(CouchDBのグローバル変数が事前に定義されているか.). */
	public static final Message DES_COUCH = createMessage("DES_COUCH");

	/**
	 * DES_PROTOTYPEJS=INFO,prototype.jsでの実行を想定(prototype.jsのグローバル変数が事前に定義されているか
	 * .).
	 */
	public static final Message DES_PROTOTYPEJS = createMessage("DES_PROTOTYPEJS");

	/** DES_MOOTOOLS=INFO,mootoolsでの実行を想定(mootoolsのグローバル変数が事前に定義されているか.). */
	public static final Message DES_MOOTOOLS = createMessage("DES_MOOTOOLS");

	/** DES_ASI=INFO,セミコロンの未挿入を許容.. */
	public static final Message DES_ASI = createMessage("DES_ASI");

	/** DES_BOSS=INFO,構造化要素の中身の割り当てを許容.. */
	public static final Message DES_BOSS = createMessage("DES_BOSS");

	/** DES_CURLY=INFO,構造化要素内の中括弧未使用を禁止.. */
	public static final Message DES_CURLY = createMessage("DES_CURLY");

	/** DES_EQEQEQ=INFO,=か!==以外の比較を禁止.. */
	public static final Message DES_EQEQEQ = createMessage("DES_EQEQEQ");

	/** DES_EQNULL=INFO,= null を許容.. */
	public static final Message DES_EQNULL = createMessage("DES_EQNULL");

	/**
	 * DES_IMMED=INFO,If true、 JSHint will require immediate invocations to be
	 * wrapped in parens..
	 */
	public static final Message DES_IMMED = createMessage("DES_IMMED");

	/** DES_LAXBREAK=INFO,改行をチェックしない.. */
	public static final Message DES_LAXBREAK = createMessage("DES_LAXBREAK");

	/** DES_NOARG=INFO,arguments.caller と　arguments.calleeの使用を禁止.. */
	public static final Message DES_NOARG = createMessage("DES_NOARG");

	/** DES_NOEMPTY=INFO,空ブロックの禁止.. */
	public static final Message DES_NOEMPTY = createMessage("DES_NOEMPTY");

	/** DES_NONEW=INFO,コンストラクタの使用を禁止.. */
	public static final Message DES_NONEW = createMessage("DES_NONEW");

	/** DES_ONEVAR=INFO,1つの関数で2つ以上のvarステートメントの使用を禁止.. */
	public static final Message DES_ONEVAR = createMessage("DES_ONEVAR");

	/** DES_STRICT=INFO,'use strict' の未使用を禁止.. */
	public static final Message DES_STRICT = createMessage("DES_STRICT");

	// #オプション詳細
	/**
	 * DET_NEWCAP=INFO,(注意)JSHINTは、コンストラクタの定義の所で名前のチェックをしていない。\r\n
	 * newでコンストラクタを呼ぶ所で名前をチェックしているので、小文字だった場合は\r\n その行を指摘される。.
	 */
	public static final Message DET_NEWCAP = createMessage("DET_NEWCAP");

	/**
	 * DET_SUB=INFO,ドット演算子(dot notation): sample.aaa\r\n 括弧記法(subscript
	 * notation): sample['aaa'].
	 */
	public static final Message DET_SUB = createMessage("DET_SUB");

	/**
	 * デフォルトコンストラクタ.
	 */
	private Messages() {
		// no create
	}
}
