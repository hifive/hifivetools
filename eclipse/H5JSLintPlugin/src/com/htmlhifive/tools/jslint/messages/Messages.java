/*
 * Copyright (C) 2011 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.tools.jslint.messages;

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
		addResourceBundleString("com.htmlhifive.tools.jslint.messages.messages");
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

	/** B0005=INFO,新規. */
	public static final Message B0005 = createMessage("B0005");

	/** B0006=INFO,全選択. */
	public static final Message B0006 = createMessage("B0006");

	/** B0007=INFO,全解除. */
	public static final Message B0007 = createMessage("B0007");

	/** B0008=INFO,ダウンロード. */
	public static final Message B0008 = createMessage("B0008");

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

	/** DL0008=INFO,チェック対象のjsファイルパターンと指定されているライブラリ一覧.設定は<a>ここ</a>から(JavaScriptProjectのみ). */
	public static final Message DL0008 = createMessage("DL0008");

	/** DL0009=INFO,JSLintプラグインの設定を行います.. */
	public static final Message DL0009 = createMessage("DL0009");

	/** DL0010=INFO,JSLintプラグインヘルプページは<a>こちら</a>. */
	public static final Message DL0010 = createMessage("DL0010");

	/** DL0011=INFO,http://www.htmlhifive.com/conts/web/view/tools/eclipse-jslint-plugin. */
	public static final Message DL0011 = createMessage("DL0011");

	/** DL0012=INFO,JSLint(又はJSHint)のエンジンファイルを設定します.. */
	public static final Message DL0012 = createMessage("DL0012");

	/** DL0013=INFO,JSLint公式サイト:<a>http://www.jslint.com/</a>. */
	public static final Message DL0013 = createMessage("DL0013");

	/** DL0014=INFO,http://www.jslint.com/. */
	public static final Message DL0014 = createMessage("DL0014");

	/** DL0015=INFO,JSHint公式サイト:<a>http://www.jshint.com/</a>. */
	public static final Message DL0015 = createMessage("DL0015");

	/** DL0016=INFO,http://www.jshint.com/. */
	public static final Message DL0016 = createMessage("DL0016");

	/** DL0017=INFO,ライブラリの除外設定を行います.. */
	public static final Message DL0017 = createMessage("DL0017");

	/** DL0018=INFO,生成ファイルの指定. */
	public static final Message DL0018 = createMessage("DL0018");

	/** DL0019=INFO,出力ディレクトリを指定し、設定ファイルの名前を入力してください.. */
	public static final Message DL0019 = createMessage("DL0019");

	/** DL0020=INFO,出力ディレクトリ. */
	public static final Message DL0020 = createMessage("DL0020");

	/** DL0021=INFO,設定ファイル名. */
	public static final Message DL0021 = createMessage("DL0021");

	/** DL0022=INFO,出力先ディレクトリを選択してください.. */
	public static final Message DL0022 = createMessage("DL0022");

	/** DL0023=INFO,JSLint.js又はJSHint.jsの取得. */
	public static final Message DL0023 = createMessage("DL0023");

	/** DL0024=INFO,タイプ種別. */
	public static final Message DL0024 = createMessage("DL0024");

	/** DL0025=INFO,JSLint. */
	public static final Message DL0025 = createMessage("DL0025");

	/** DL0026=INFO,JSHint. */
	public static final Message DL0026 = createMessage("DL0026");

	/** DL0027=INFO,種別と出力先ディレクトリを指定しJSLintファイルを取得します.. */
	public static final Message DL0027 = createMessage("DL0027");

	/** DL0028=INFO,ライセンスの確認. */
	public static final Message DL0028 = createMessage("DL0028");

	/** DL0029=INFO,ライセンスに同意します. */
	public static final Message DL0029 = createMessage("DL0029");

	/** DL0030=INFO,ライセンスに同意しません. */
	public static final Message DL0030 = createMessage("DL0030");

	/** DL0031=INFO,JSLint取得先URL. */
	public static final Message DL0031 = createMessage("DL0031");

	/** DL0032=INFO,JSHint取得先URL. */
	public static final Message DL0032 = createMessage("DL0032");

	// #ダイアログツールチップ
	/** DTT0000=INFO,チェック時にライブラリを考慮するかどうかを設定します。読み込むライブラリが多いとパフォーマンスに影響します。. */
	public static final Message DTT0000 = createMessage("DTT0000");

	/** DTT0001=INFO,インターネット上から最新のJSLint(又はJSHint)のjsファイルを取得します。同名のファイルがある場合は上書きをします。. */
	public static final Message DTT0001 = createMessage("DTT0001");

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

	/** DT0007=INFO,設定ファイルの生成. */
	public static final Message DT0007 = createMessage("DT0007");

	/** DT0008=INFO,生成するディレクトリの選択. */
	public static final Message DT0008 = createMessage("DT0008");

	/** DT0009=INFO,ライセンスの確認. */
	public static final Message DT0009 = createMessage("DT0009");

	/** DT0010=INFO,JSLintファイルの取得. */
	public static final Message DT0010 = createMessage("DT0010");

	/** DT0011=INFO,確認ダイアログ. */
	public static final Message DT0011 = createMessage("DT0011");

	// #タブタイトル
	/** TT0000=INFO,JSLint設定. */
	public static final Message TT0000 = createMessage("TT0000");

	/** TT0001=INFO,ソース構成. */
	public static final Message TT0001 = createMessage("TT0001");

	// #ウィジェットタイトル
	/** WT0000=INFO,JSLint設定一覧. */
	public static final Message WT0000 = createMessage("WT0000");

	/** WT0001=INFO,詳細説明. */
	public static final Message WT0001 = createMessage("WT0001");

	/** WT0002=INFO,チェック対象ソース. */
	public static final Message WT0002 = createMessage("WT0002");

	/** WT0003=INFO,構成. */
	public static final Message WT0003 = createMessage("WT0003");

	/** WT0004=INFO,内部参照ライブラリ. */
	public static final Message WT0004 = createMessage("WT0004");

	/** WT0005=INFO,プロジェクト参照. */
	public static final Message WT0005 = createMessage("WT0005");

	/** WT0006=INFO,外部参照ライブラリ. */
	public static final Message WT0006 = createMessage("WT0006");

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

	/** DM0004=INFO,既にファイルが存在しています。上書きしますか？. */
	public static final Message DM0004 = createMessage("DM0004");

	// #タスクメッセージ
	/** T0000=INFO,jsファイルチェックの準備中.... */
	public static final Message T0000 = createMessage("T0000");

	/** T0001=INFO,jsファイルの取得. */
	public static final Message T0001 = createMessage("T0001");

	/** T0002=INFO,オプションファイル.JSLint.jsの読み込み.. */
	public static final Message T0002 = createMessage("T0002");

	/** T0003=INFO,jsファイルのコードチェック中.... */
	public static final Message T0003 = createMessage("T0003");

	/** T0004=INFO,終了処理中.... */
	public static final Message T0004 = createMessage("T0004");

	/** T0005=INFO,ワークスペースをビルド中.. */
	public static final Message T0005 = createMessage("T0005");

	/** T0006=INFO,コード検査中...ファイル名 : {0}. */
	public static final Message T0006 = createMessage("T0006");

	/** T0007=INFO,ビューに反映中...(この処理には時間がかかる場合があります。). */
	public static final Message T0007 = createMessage("T0007");

	/** T0008=INFO,問題ビューに反映中... {0}件 / {1}件中. */
	public static final Message T0008 = createMessage("T0008");

	/** T0009=INFO,タスクを開始しています.... */
	public static final Message T0009 = createMessage("T0009");

	/** T0010=INFO,情報を取得中です.... */
	public static final Message T0010 = createMessage("T0010");

	/** T0011=INFO,進捗 : {0} b / {1} b. */
	public static final Message T0011 = createMessage("T0011");

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

	/** EM0007=WARN,オプションに設定した型が存在しません : [{0}]. */
	public static final Message EM0007 = createMessage("EM0007");

	/** EM0008=WARN,適切なxmlファイルを選択してください.. */
	public static final Message EM0008 = createMessage("EM0008");

	/** EM0009=WARN,{0}を指定してください.. */
	public static final Message EM0009 = createMessage("EM0009");

	/** EM0010=ERROR,ビューの反映中にエラーが発生しました. : {0}. */
	public static final Message EM0010 = createMessage("EM0010");

	/** EM0011=WARN,プロジェクト情報の取得に失敗しました. : {0}. */
	public static final Message EM0011 = createMessage("EM0011");

	/** EM0012=WARN,ライブラリ情報取得中に例外が発生しました. : {0}. */
	public static final Message EM0012 = createMessage("EM0012");

	/** EM0013=WARN,指定したパスにファイルが既に存在します.. */
	public static final Message EM0013 = createMessage("EM0013");

	/** EM0014=WARN,種別のどちらかを選択してください.. */
	public static final Message EM0014 = createMessage("EM0014");

	/** EM0015=ERROR,情報取得中にエラーが発生しました.. */
	public static final Message EM0015 = createMessage("EM0015");

	/** EM0016=ERROR,ファイル出力中にエラーが発生しました.. */
	public static final Message EM0016 = createMessage("EM0016");

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

	/** DES_PROTOTYPEJS=INFO,prototype.jsでの実行を想定(prototype.jsのグローバル変数が事前に定義されているか.). */
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

	/** DES_IMMED=INFO,If true、 JSHint will require immediate invocations to be wrapped in parens.. */
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
	/** DET_NEWCAP=INFO,(注意)JSHINTは、コンストラクタの定義の所で名前のチェックをしていない。\r\n newでコンストラクタを呼ぶ所で名前をチェックしているので、小文字だった場合は\r\n その行を指摘される。. */
	public static final Message DET_NEWCAP = createMessage("DET_NEWCAP");

	/** DET_SUB=INFO,ドット演算子(dot notation): sample.aaa\r\n 括弧記法(subscript notation): sample['aaa']. */
	public static final Message DET_SUB = createMessage("DET_SUB");

	/**
	 * デフォルトコンストラクタ.
	 */
	private Messages() {
		// no create
	}
}
