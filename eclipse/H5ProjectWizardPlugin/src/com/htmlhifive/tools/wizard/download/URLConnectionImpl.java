package com.htmlhifive.tools.wizard.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;

import com.htmlhifive.tools.wizard.utils.H5IOUtils;

public class URLConnectionImpl implements IConnectMethod {
	protected final String urlStr;
	protected int connectionTimeout = 10000; // デフォルト10秒としておく
	protected URLConnection connection;

	/**
	 * コンストラクタ.
	 * 
	 * @param urlStr URL
	 */
	public URLConnectionImpl(String urlStr) {

		this.urlStr = urlStr;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#setConnectionTimeout(int)
	 */
	@Override
	public void setConnectionTimeout(int connectionTimeout) {

		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#setProxy(org.eclipse.core.net.proxy.IProxyService)
	 */
	@Override
	public void setProxy(IProxyService proxyService) {

		// プロキシ設定.
		IProxyData[] proxyDataForHost = proxyService.select(URI.create(urlStr));
		for (IProxyData data : proxyDataForHost) {
			if (data.getHost() != null) {
				System.setProperty("http.proxySet", "true");
				System.setProperty("http.proxyHost", data.getHost());
				System.setProperty("http.proxyPort", String.valueOf(data.getPort()));
				if (data.getUserId() != null) {
					System.setProperty("http.proxyUser", data.getUserId());
				}
				if (data.getPassword() != null) {
					System.setProperty("http.proxyPassword", data.getPassword());
				}
			} else {
				System.setProperty("http.proxySet", "false");
			}
		}
	}

	/**
	 * クラスリソースを取得する.
	 * 
	 * @return 正しく取得できたかどうか.
	 * @throws IOException IO例外
	 */
	protected boolean getClassConnection() throws IOException {

		// クラスリソースはクラスパスから取得する.
		URL url = URLConnectionImpl.class.getResource(urlStr);
		if (url == null) {
			return false;
		}
		connection = url.openConnection();
		connection.connect();
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#connect()
	 */
	@Override
	public boolean connect() throws IOException {

		if (H5IOUtils.isClassResources(urlStr)) {
			return getClassConnection();
		}

		connection = new URL(urlStr).openConnection();
		connection.setConnectTimeout(connectionTimeout);
		connection.connect();
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#getContentLength()
	 */
	@Override
	public int getContentLength() {

		if (connection != null) {
			return connection.getContentLength();
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {

		if (connection == null) {
			// 接続が無い場合はここで接続する.
			if (!connect()) {
				return null;
			}
		}
		return connection.getInputStream();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#getLastModified()
	 */
	@Override
	public Date getLastModified() {

		if (connection != null && connection.getLastModified() > 0) {
			return new Date(connection.getLastModified());
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#close()
	 */
	@Override
	public void close() {

		// 処理無し
	}
}
