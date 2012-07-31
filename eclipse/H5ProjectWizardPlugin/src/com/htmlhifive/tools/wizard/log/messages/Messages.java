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
package com.htmlhifive.tools.wizard.log.messages;

import java.util.ResourceBundle;


/**
 * Messages.
 * 
 * @author MessageGenerator
 */
public abstract class Messages extends MessagesBase {

	// 必ず定数定義より先に呼び出すこと.
	static {
		// このクラスを登録する.
		addResourceBundle(ResourceBundle.getBundle("com.htmlhifive.tools.wizard.log.messages.messages"));
	}

	// #エラーメッセージ
	/** SE0001=ERROR,ロガーの初期化に失敗しました。. */
	public static final Message SE0001 = createMessage("SE0001");

	/** SE0002=ERROR,リソース({0})が見つかりません。. */
	public static final Message SE0002 = createMessage("SE0002");

	/** SE0011=ERROR,xmlファイルの解析に失敗しました。. */
	public static final Message SE0011 = createMessage("SE0011");

	/** SE0012=ERROR,インスタンスの生成に失敗しました。. */
	public static final Message SE0012 = createMessage("SE0012");

	/** SE0013=ERROR,URLが正しくありません。. */
	public static final Message SE0013 = createMessage("SE0013");

	/** SE0021=ERROR,({0})は無効なzipファイルです。. */
	public static final Message SE0021 = createMessage("SE0021");

	/** SE0022=ERROR,エラー. */
	public static final Message SE0022 = createMessage("SE0022");

	/** SE0023=ERROR,予期しない例外が発生しました。. */
	public static final Message SE0023 = createMessage("SE0023");

	/** SE0024=ERROR,({0})を操作中に入出力例外が発生しました。. */
	public static final Message SE0024 = createMessage("SE0024");

	/** SE0025=ERROR,プロジェクト作成中にエラーが発生しました。. */
	public static final Message SE0025 = createMessage("SE0025");

	/** SE0026=ERROR,プロジェクト({0})削除処理中にエラーが発生しました。. */
	public static final Message SE0026 = createMessage("SE0026");

	/** SE0031=ERROR,プラグインがインストールされていない可能性があります。name={0}, natureId={1}. */
	public static final Message SE0031 = createMessage("SE0031");

	/** SE0032=ERROR,Nature追加エラー. */
	public static final Message SE0032 = createMessage("SE0032");

	/** SE0041=ERROR,定義ファイルダウンロードエラー. */
	public static final Message SE0041 = createMessage("SE0041");

	/** SE0042=ERROR,定義ファイルのダウンロード中にエラーが発生しました。Eclipseのネットワーク接続設定を確認して下さい。. */
	public static final Message SE0042 = createMessage("SE0042");

	/** SE0043=ERROR,ダウンロードエラー. */
	public static final Message SE0043 = createMessage("SE0043");

	/** SE0044=ERROR,ライブラリのダウンロード中にエラーが発生しました。. */
	public static final Message SE0044 = createMessage("SE0044");

	/** SE0045=ERROR,ライブラリ内の({0} {1})を展開中にエラーが発生しました。. */
	public static final Message SE0045 = createMessage("SE0045");

	/** SE0046=ERROR,({0})にある定義ファイルのダウンロード中に失敗しました。. */
	public static final Message SE0046 = createMessage("SE0046");

	/** SE0047=ERROR,取得エラー. */
	public static final Message SE0047 = createMessage("SE0047");

	/** SE0048=ERROR,プロジェクト用ZIPファイルの取得に失敗しました。. */
	public static final Message SE0048 = createMessage("SE0048");

	/** SE0051=ERROR,プロジェクト名が入力されていません。. */
	public static final Message SE0051 = createMessage("SE0051");

	/** SE0052=ERROR,既にプロジェクトが存在します。. */
	public static final Message SE0052 = createMessage("SE0052");

	/** SE0053=ERROR,定義ファイルが取得できません。Eclipseのネットワーク接続設定を確認して下さい。. */
	public static final Message SE0053 = createMessage("SE0053");

	/** SE0061=INFO,プロジェクト構成を作成します。. */
	public static final Message SE0061 = createMessage("SE0061");

	/** SE0062=INFO,プロジェクト構成の作成が完了しました。. */
	public static final Message SE0062 = createMessage("SE0062");

	/** SE0063=INFO,プロジェクト用ZIPファイルをダウンロードします。. */
	public static final Message SE0063 = createMessage("SE0063");

	/** SE0064=INFO,プロジェクト用ZIPファイルをダウンロードしました。. */
	public static final Message SE0064 = createMessage("SE0064");

	/** SE0065=INFO,Nature({0})を追加します。. */
	public static final Message SE0065 = createMessage("SE0065");

	/** SE0066=INFO,Nature({0})を追加しました。. */
	public static final Message SE0066 = createMessage("SE0066");

	/** SE0067=ERROR,Nature({0})追加に失敗しました。. */
	public static final Message SE0067 = createMessage("SE0067");

	/** SE0068=ERROR,プロジェクト用ZIPファイルのダウンロードに失敗しました。. */
	public static final Message SE0068 = createMessage("SE0068");

	/** SE0069=INFO,リソース({0})内のプロジェクト名を変更しました。. */
	public static final Message SE0069 = createMessage("SE0069");

	/** SE0070=INFO,プロジェクト用ZIPファイルの展開が全て終了しました。. */
	public static final Message SE0070 = createMessage("SE0070");

	/** SE0071=INFO,ライブラリ更新処理を開始します。. */
	public static final Message SE0071 = createMessage("SE0071");

	/** SE0072=INFO,ライブラリ更新処理が完了しました。. */
	public static final Message SE0072 = createMessage("SE0072");

	/** SE0073=INFO,ライブラリ({0} {1})の更新処理を開始します。. */
	public static final Message SE0073 = createMessage("SE0073");

	/** SE0074=INFO,ライブラリ({0} {1})の更新処理が完了しました。. */
	public static final Message SE0074 = createMessage("SE0074");

	/** SE0081=ERROR,リソース({0})が見つかりません。. */
	public static final Message SE0081 = createMessage("SE0081");

	/** SE0082=ERROR,リソース({0})は正しいURLではありません。. */
	public static final Message SE0082 = createMessage("SE0082");

	// #ファイル操作(ResultStatus)
	/** SE0091=INFO,リソース({0})を作成します。. */
	public static final Message SE0091 = createMessage("SE0091");

	/** SE0092=INFO,リソース({0})を作成しました。. */
	public static final Message SE0092 = createMessage("SE0092");

	/** SE0093=INFO,リソース({0})をダウンロードします。. */
	public static final Message SE0093 = createMessage("SE0093");

	/** SE0094=INFO,リソース({0})をダウンロードしました。. */
	public static final Message SE0094 = createMessage("SE0094");

	/** SE0095=INFO,リソース({0})を削除します。. */
	public static final Message SE0095 = createMessage("SE0095");

	/** SE0096=INFO,リソース({0})を削除しました。. */
	public static final Message SE0096 = createMessage("SE0096");

	/** SE0097=INFO,リソース({0})を更新します。. */
	public static final Message SE0097 = createMessage("SE0097");

	/** SE0098=INFO,リソース({0})を更新しました。. */
	public static final Message SE0098 = createMessage("SE0098");

	/** SE0099=ERROR,ファイルの作成に失敗しました。URL={1}, File={2}. */
	public static final Message SE0099 = createMessage("SE0099");

	/** SE0100=ERROR,プロジェクト({0})削除処理中にエラーが発生しました。. */
	public static final Message SE0100 = createMessage("SE0100");

	/** SE0101=ERROR,リソース({0})のダウンロードに失敗しました。URL={1}, File={2}. */
	public static final Message SE0101 = createMessage("SE0101");

	/** SE0102=WARN,リソース({0})は他で利用されているため削除しませんでした。. */
	public static final Message SE0102 = createMessage("SE0102");

	// #その他
	/** SE0103=INFO,ライブラリの状態を最新化しました。. */
	public static final Message SE0103 = createMessage("SE0103");

	/** SE0104=INFO,ワークスペースを更新しました。. */
	public static final Message SE0104 = createMessage("SE0104");

	// #ダイアログ
	/** SE0111=INFO,確認. */
	public static final Message SE0111 = createMessage("SE0111");

	/** SE0112=INFO,変更がありますが、終了してもよろしいですか？. */
	public static final Message SE0112 = createMessage("SE0112");

	/** SE0113=INFO,上書き確認. */
	public static final Message SE0113 = createMessage("SE0113");

	/** SE0114=WARN,{0}は既に存在しています。. */
	public static final Message SE0114 = createMessage("SE0114");

	/** SE0115=ERROR,ダウンロードエラー. */
	public static final Message SE0115 = createMessage("SE0115");

	/** SE0116=ERROR,{0}のダウンロードに失敗しました。. */
	public static final Message SE0116 = createMessage("SE0116");

	// #taskName
	/** PI0101=INFO,プロジェクトのディレクトリ構成を作成中.... */
	public static final Message PI0101 = createMessage("PI0101");

	/** PI0102=INFO,ライブラリをダウンロード中.... */
	public static final Message PI0102 = createMessage("PI0102");

	/** PI0103=INFO,ライブラリ構成を更新中.... */
	public static final Message PI0103 = createMessage("PI0103");

	// #subTask(ResultStatus)
	/** PI0111=INFO,リソース({0})をダウンロード中.... */
	public static final Message PI0111 = createMessage("PI0111");

	/** PI0112=INFO,リソース({0})を更新中.... */
	public static final Message PI0112 = createMessage("PI0112");

	/** PI0113=INFO,リソース({0})を展開中.... */
	public static final Message PI0113 = createMessage("PI0113");

	/** PI0114=INFO,ライブラリ({0} {1})を更新中.... */
	public static final Message PI0114 = createMessage("PI0114");

	// #result
	/** PI0131=INFO,処理結果. */
	public static final Message PI0131 = createMessage("PI0131");

	/** PI0132=INFO,ライブラリ更新処理が完了しました。. */
	public static final Message PI0132 = createMessage("PI0132");

	/** PI0133=WARN,ライブラリ更新処理が完了しましたが、\n一部でエラーが発生しています。. */
	public static final Message PI0133 = createMessage("PI0133");

	/** PI0135=INFO,{0}. */
	public static final Message PI0135 = createMessage("PI0135");

	/** PI0136=INFO,URL{0}の結果は{1}でした。. */
	public static final Message PI0136 = createMessage("PI0136");

}
