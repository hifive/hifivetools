package com.htmlhifive.tools.wizard.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;

import com.htmlhifive.tools.wizard.PluginConstant;

/**
 * <H3>HttpClientを利用したURL接続.<br>
 * パス無しNTLMが通らないため、現在使用していない.</H3>
 * 
 * @author fkubo
 */
public class HttpGetMethodImpl implements IConnectMethod {

	/** urlStr. */
	protected final String urlStr;
	/** client. */
	protected final HttpClient client;
	/** method. */
	protected final HttpMethod method;
	/** connectionTimeout. */
	protected int connectionTimeout = 10000; // デフォルト10秒としておく

	/**
	 * コンストラクタ.
	 * 
	 * @param urlStr URL
	 */
	public HttpGetMethodImpl(String urlStr) {

		this.urlStr = urlStr;
		client = new HttpClient();
		method = new GetMethod(urlStr);
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
		IProxyData[] proxyDataForHost = proxyService.select(URI.create(this.urlStr));
		for (IProxyData data : proxyDataForHost) {
			if (data.getHost() != null) {
				client.getHostConfiguration().setProxy(data.getHost(), data.getPort());

				//				client.getState().setProxyCredentials(new AuthScope(data.getHost(), data.getPort()),
				//						new NTCredentials(System.getenv("USERNAME"), "", System.getenv("COMPUTERNAME"), System
				//								.getenv("USERDNSDOMAIN")));

				if (StringUtils.isNotEmpty(data.getUserId())) {
					client.getState().setProxyCredentials(new AuthScope(data.getHost(), data.getPort(), "realm"),
							new UsernamePasswordCredentials(data.getUserId(), data.getPassword()));

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

		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, true));
		client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
				PluginConstant.URL_LIBRARY_LIST_CONNECTION_TIMEOUT);

		int result = client.executeMethod(method);
		if (result != HttpStatus.SC_OK) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#getContentLength()
	 */
	@Override
	public int getContentLength() {

		Header header = method.getResponseHeader("Content-Length");
		return Integer.valueOf(header.getValue());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {

		if (!method.isRequestSent()) {
			// 接続していない場合はここで接続する.
			if (!connect()) {
				return null;
			}
		}
		return method.getResponseBodyAsStream();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.htmlhifive.tools.wizard.download.IConnectMethod#getLastModified()
	 */
	@Override
	public Date getLastModified() {

		if (method.getResponseHeader("last-modified") != null) {
			try {
				return DateUtil.parseDate(method.getResponseHeader("last-modified").getValue());
			} catch (DateParseException e) {
				// 無視.
			}
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

		// 処理無し.
	}
}
