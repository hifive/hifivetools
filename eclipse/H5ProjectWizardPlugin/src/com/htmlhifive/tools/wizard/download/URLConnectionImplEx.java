package com.htmlhifive.tools.wizard.download;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;

import com.htmlhifive.tools.wizard.utils.H5IOUtils;

public class URLConnectionImplEx extends URLConnectionImpl {
	private Proxy proxy;
	private String proxyAuthorization;

	/**
	 * コンストラクタ.
	 * 
	 * @param urlStr URL
	 */
	public URLConnectionImplEx(String urlStr) {

		super(urlStr);
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

				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(data.getHost(), data.getPort()));
				if (data.getUserId() != null) {
					proxyAuthorization = "Basic "
							+ new String(Base64.encodeBase64(new StringBuilder(data.getUserId()).append(":")
									.append(data.getPassword()).toString().getBytes()));
				}
			}
		}
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

		if (proxy != null) {
			connection = new URL(urlStr).openConnection(proxy);
			if (proxyAuthorization != null) {
				connection.setRequestProperty("Proxy-Authorization", proxyAuthorization);
			}
		} else {
			connection = new URL(urlStr).openConnection();
		}
		connection.setConnectTimeout(connectionTimeout);
		connection.connect();
		return true;
	}
}
