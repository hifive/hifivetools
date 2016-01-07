package com.htmlhifive.tools.wizard.download;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.eclipse.core.net.proxy.IProxyService;

/**
 * <H3>外部Http接続用ラッパ.</H3>
 * 
 * @author fkubo
 */
public interface IConnectMethod extends IFileContentsHandler {

	// 以下、前処理.

	/**
	 * タイムアウト時間を設定する.
	 * 
	 * @param connectionTimeout タイムアウト時間
	 */
	void setConnectionTimeout(int connectionTimeout);

	/**
	 * プロキシを設定する.
	 * 
	 * @param proxyService プロキシサービス
	 */
	void setProxy(IProxyService proxyService);

	// 以下、接続処理.

	/**
	 * 接続する.
	 * 
	 * @return 成功したかどうか
	 * @throws IOException IO例外
	 */
	boolean connect() throws IOException;

	// 以下、取得処理.

	/**
	 * コンテンツの長さを取得する.
	 * 
	 * @return コンテンツの長さ
	 */
	int getContentLength();

	/**
	 * IOストリームを取得する.
	 * 
	 * @return IOストリーム
	 * @throws IOException IO例外.
	 */
	@Override
	InputStream getInputStream() throws IOException;

	/**
	 * 最終更新日を取得する.
	 * 
	 * @return 最終更新日
	 */
	Date getLastModified();

	// 以下、後処理.

	/**
	 * 終了処理.
	 */
	void close();
}
