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

	/** TR0001=INFO,Composite: {0}.{1}が呼ばれました。. */
	public static final Message TR0001 = createMessage("TR0001");

	/** TR0011=INFO,WizardPage: {0}.{1}が呼ばれました。. */
	public static final Message TR0011 = createMessage("TR0011");

	/** TR0021=INFO,PropertyPage: {0}.{1}が呼ばれました。. */
	public static final Message TR0021 = createMessage("TR0021");

	/** TR0031=INFO,Wizard: {0}.{1}が呼ばれました。. */
	public static final Message TR0031 = createMessage("TR0031");

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

	/** SE0023=ERROR,予期しない例外が発生しました。原因:{0}. */
	public static final Message SE0023 = createMessage("SE0023");

	/** SE0024=ERROR,({0})を操作中に入出力例外が発生しました。. */
	public static final Message SE0024 = createMessage("SE0024");

	/** SE0025=ERROR,プロジェクト作成中にエラーが発生しました。. */
	public static final Message SE0025 = createMessage("SE0025");

	/** SE0031=ERROR,プラグインがインストールされていない可能性があります。name={0}, natureId={1}. */
	public static final Message SE0031 = createMessage("SE0031");

	/** SE0032=ERROR,Nature追加エラー. */
	public static final Message SE0032 = createMessage("SE0032");

	/** SE0041=ERROR,ライブラリ定義情報ダウンロードエラー. */
	public static final Message SE0041 = createMessage("SE0041");

	/** SE0042=ERROR,ライブラリ定義情報のダウンロード中にエラーが発生しました。Eclipseのネットワーク接続設定を確認して下さい。. */
	public static final Message SE0042 = createMessage("SE0042");

	/** SE0043=ERROR,ダウンロードエラー. */
	public static final Message SE0043 = createMessage("SE0043");

	/** SE0044=ERROR,ライブラリ定義情報のダウンロード中にエラーが発生しました。. */
	public static final Message SE0044 = createMessage("SE0044");

	/** SE0045=ERROR,ライブラリ({0} {1})の展開中にエラーが発生しました。. */
	public static final Message SE0045 = createMessage("SE0045");

	/** SE0046=ERROR,ライブラリ定義情報({0})のダウンロードに失敗しました。. */
	public static final Message SE0046 = createMessage("SE0046");

	/** SE0047=ERROR,取得エラー. */
	public static final Message SE0047 = createMessage("SE0047");

	/** SE0048=ERROR,プロジェクト用ZIPファイルの取得に失敗しました。. */
	public static final Message SE0048 = createMessage("SE0048");

	/** SE0051=ERROR,プロジェクト名が入力されていません。. */
	public static final Message SE0051 = createMessage("SE0051");

	// #A project with this name already exists.
	/** SE0052=ERROR,この名前のプロジェクトは既に存在します。. */
	public static final Message SE0052 = createMessage("SE0052");

	/** SE0053=ERROR,ライブラリ定義情報が取得できません。Eclipseのネットワーク接続設定を確認して下さい。. */
	public static final Message SE0053 = createMessage("SE0053");

	/** SE0054=ERROR,{0}はファイル名として正しくありません。. */
	public static final Message SE0054 = createMessage("SE0054");

	/** SE0061=INFO,プロジェクト構成の作成開始. */
	public static final Message SE0061 = createMessage("SE0061");

	/** SE0062=INFO,プロジェクト構成の作成完了. */
	public static final Message SE0062 = createMessage("SE0062");

	/** SE0063=INFO,プロジェクト用ZIPファイルのダウンロード開始. */
	public static final Message SE0063 = createMessage("SE0063");

	/** SE0064=INFO,プロジェクト用ZIPファイルのダウンロード完了. */
	public static final Message SE0064 = createMessage("SE0064");

	/** SE0065=INFO,Nature({0})の追加開始. */
	public static final Message SE0065 = createMessage("SE0065");

	/** SE0066=INFO,Nature({0})の追加完了. */
	public static final Message SE0066 = createMessage("SE0066");

	/** SE0067=ERROR,Nature({0})の追加に失敗しました。. */
	public static final Message SE0067 = createMessage("SE0067");

	/** SE0068=ERROR,プロジェクト用ZIPファイルのダウンロードに失敗しました。. */
	public static final Message SE0068 = createMessage("SE0068");

	/** SE0069=INFO,リソース({0})内のプロジェクト名を変更しました。. */
	public static final Message SE0069 = createMessage("SE0069");

	/** SE0070=INFO,プロジェクト用ZIPファイルの展開が全て終了しました。. */
	public static final Message SE0070 = createMessage("SE0070");

	/** SE0073=INFO,ライブラリ({0} {1})の更新開始. */
	public static final Message SE0073 = createMessage("SE0073");

	/** SE0074=INFO,ライブラリ({0} {1})の更新完了. */
	public static final Message SE0074 = createMessage("SE0074");

	/** SE0081=ERROR,リソース({0})が見つかりません。. */
	public static final Message SE0081 = createMessage("SE0081");

	/** SE0082=ERROR,リソース({0})は正しいURLではありません。. */
	public static final Message SE0082 = createMessage("SE0082");

	// #ファイル操作(ResultStatus)
	/** SE0091=INFO,リソース({0})の作成開始. */
	public static final Message SE0091 = createMessage("SE0091");

	/** SE0092=INFO,リソース({0})の作成完了. */
	public static final Message SE0092 = createMessage("SE0092");

	/** SE0093=INFO,リソース({0})のダウンロード開始. */
	public static final Message SE0093 = createMessage("SE0093");

	/** SE0094=INFO,リソース({0})のダウンロード完了. */
	public static final Message SE0094 = createMessage("SE0094");

	/** SE0095=INFO,リソース({0})の削除開始. */
	public static final Message SE0095 = createMessage("SE0095");

	/** SE0096=INFO,リソース({0})の削除完了. */
	public static final Message SE0096 = createMessage("SE0096");

	/** SE0097=INFO,リソース({0})の更新開始. */
	public static final Message SE0097 = createMessage("SE0097");

	/** SE0098=INFO,リソース({0})の更新完了. */
	public static final Message SE0098 = createMessage("SE0098");

	/** SE0099=ERROR,ファイルの作成に失敗しました。URL={1}, File={2}. */
	public static final Message SE0099 = createMessage("SE0099");

	/** SE0100=ERROR,プロジェクト({0})削除処理中にエラーが発生しました。. */
	public static final Message SE0100 = createMessage("SE0100");

	/** SE0101=ERROR,リソース({0})のダウンロードに失敗しました。URL={1}, File={2}. */
	public static final Message SE0101 = createMessage("SE0101");

	/** SE0102=WARN,リソース({0})は他で利用されているため削除を中止しました。. */
	public static final Message SE0102 = createMessage("SE0102");

	// #その他
	/** SE0103=INFO,ライブラリ一覧を更新. */
	public static final Message SE0103 = createMessage("SE0103");

	/** SE0104=INFO,ワークスペースを更新. */
	public static final Message SE0104 = createMessage("SE0104");

	/** SE0105=INFO,エラー発生によるプロジェクト({0})の削除開始. */
	public static final Message SE0105 = createMessage("SE0105");

	/** SE0106=INFO,エラー発生によるプロジェクト({0})の削除完了. */
	public static final Message SE0106 = createMessage("SE0106");

	// #ダイアログ
	/** SE0111=INFO,確認. */
	public static final Message SE0111 = createMessage("SE0111");

	/** SE0112=INFO,変更が破棄されますが、よろしいですか？. */
	public static final Message SE0112 = createMessage("SE0112");

	/** SE0113=INFO,上書き確認. */
	public static final Message SE0113 = createMessage("SE0113");

	/** SE0114=WARN,{0}は既に存在しています。. */
	public static final Message SE0114 = createMessage("SE0114");

	/** SE0115=ERROR,ダウンロードエラー. */
	public static final Message SE0115 = createMessage("SE0115");

	/** SE0116=ERROR,{0}のダウンロードに失敗しました。. */
	public static final Message SE0116 = createMessage("SE0116");

	/** SE0117=INFO,確認. */
	public static final Message SE0117 = createMessage("SE0117");

	/** SE0118=INFO,ここでは遷移できません。. */
	public static final Message SE0118 = createMessage("SE0118");

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

	/** PI0115=INFO,[{0}/{1}bytes] {2}. */
	public static final Message PI0115 = createMessage("PI0115");

	// #result
	/** PI0131=INFO,処理結果. */
	public static final Message PI0131 = createMessage("PI0131");

	/** PI0132=INFO,{0}が終了しました。. */
	public static final Message PI0132 = createMessage("PI0132");

	/** PI0133=WARN,{0}が終了しましたが、\nエラーが発生しています。. */
	public static final Message PI0133 = createMessage("PI0133");

	/** PI0134=WARN,{0}処理が中断されました。. */
	public static final Message PI0134 = createMessage("PI0134");

	/** PI0135=INFO,{0}. */
	public static final Message PI0135 = createMessage("PI0135");

	/** PI0136=INFO,URL{0}の結果は{1}でした。. */
	public static final Message PI0136 = createMessage("PI0136");

	/** PI0137=INFO,プロジェクトの作成. */
	public static final Message PI0137 = createMessage("PI0137");

	/** PI0138=INFO,ライブラリの更新. */
	public static final Message PI0138 = createMessage("PI0138");

	/** PI0139=ERROR,ライブラリの取得エラー. */
	public static final Message PI0139 = createMessage("PI0139");

	/** PI0140=ERROR,ライブラリの取得に失敗しました。続行できません。. */
	public static final Message PI0140 = createMessage("PI0140");

	/** PI0141=INFO,ライブラリ定義情報取得の準備中.... */
	public static final Message PI0141 = createMessage("PI0141");

	/** PI0142=INFO,ライブラリ定義情報をダウンロード中.... */
	public static final Message PI0142 = createMessage("PI0142");

	/** PI0143=INFO,[{0}/{1}bytes] {2}. */
	public static final Message PI0143 = createMessage("PI0143");

	/** PI0151=INFO,ライブラリ定義情報: 未取得. */
	public static final Message PI0151 = createMessage("PI0151");

	/** PI0152=INFO,ライブラリ定義情報: 外部アクセス不可のため内部データを利用中. */
	public static final Message PI0152 = createMessage("PI0152");

	/** PI0153=INFO,ライブラリ定義情報: {0,date,yyyy/MM/dd HH:mm:ss}版. */
	public static final Message PI0153 = createMessage("PI0153");

	/** PI0154=INFO,ライブラリ定義情報の取得. */
	public static final Message PI0154 = createMessage("PI0154");

}
