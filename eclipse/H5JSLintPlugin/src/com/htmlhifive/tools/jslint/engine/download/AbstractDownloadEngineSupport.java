package com.htmlhifive.tools.jslint.engine.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.util.tracker.ServiceTracker;

import com.htmlhifive.tools.jslint.JSLintPlugin;

public abstract class AbstractDownloadEngineSupport implements DownloadEngineSupport {
	/**
	 * プロキシポート.
	 */
	private int proxyPort;
	/**
	 * プロキシホスト.
	 */
	private String proxyHost;
	/**
	 * ユーザID.
	 */
	private String userId;
	/**
	 * パスワード.
	 */
	private String password;
	/**
	 * プロキシを利用するかどうか.
	 */
	private boolean useProxy;

	public AbstractDownloadEngineSupport() {
		ServiceTracker<IProxyService, IProxyService> proxyTracker = new ServiceTracker<IProxyService, IProxyService>(
				JSLintPlugin.getDefault().getBundle().getBundleContext(), IProxyService.class, null);
		try {
			proxyTracker.open();
			IProxyService service = proxyTracker.getService();
			IProxyData[] datas = service.getProxyData();
			for (IProxyData proxyData : datas) {
				if (proxyData.getHost() != null) {
					useProxy = true;
					proxyHost = proxyData.getHost();
					proxyPort = proxyData.getPort();
					userId = proxyData.getUserId();
					password = proxyData.getPassword();
				}
			}
		} finally {
			proxyTracker.close();
		}
	}

	@Override
	public String getDefaultSource() {
		return null;
	}

	@Override
	public EngineInfo getEngineInfo(IProgressMonitor monitor) throws IOException {
		IProgressMonitor actualMonitor = monitor;
		if (monitor == null) {
			actualMonitor = new NullProgressMonitor();
		}
		actualMonitor.setTaskName("タスクを開始しています.");
		HttpClient client = new HttpClient();
		HttpMethod getMethod = new GetMethod(getEngineSourceUrl());
		StringBuilder licenseSb = new StringBuilder();
		StringBuilder rawSource = new StringBuilder();
		if (useProxy) {
			client.getHostConfiguration().setProxy(proxyHost, proxyPort);
			client.getState().setProxyCredentials(new AuthScope(proxyHost, proxyPort, "realm"),
					new UsernamePasswordCredentials(userId, password));
		}

		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, true));
		client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
		int result = client.executeMethod(getMethod);
		if (result != HttpStatus.SC_OK) {
			// TODO 失敗
			return null;
		}
		Header header = getMethod.getResponseHeader("Content-Length");
		int content = Integer.valueOf(header.getValue());
		actualMonitor.beginTask("情報を取得中です...", content);
		BufferedReader reader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
		String temp = reader.readLine();
		while (!isEndLicenseLine(temp)) {
			// while (!(temp = reader.readLine()).startsWith(" */")) {
			actualMonitor.worked(temp.length());
			rawSource.append(temp);
			temp = StringUtils.trim(temp);
			temp = StringUtils.substring(temp, 2);
			temp = StringUtils.trim(temp);
			licenseSb.append(temp);
			licenseSb.append(System.getProperty("line.separator"));
			rawSource.append(System.getProperty("line.separator"));
			temp = reader.readLine();
		}
		EngineInfo info = new EngineInfo();
		info.setLicenseStr(licenseSb.toString());

		while ((temp = reader.readLine()) != null) {
			actualMonitor.worked(temp.length());
			rawSource.append(temp);
			rawSource.append(System.getProperty("line.separator"));
		}
		info.setMainSource(rawSource.toString());
		monitor.done();
		return info;
	}

	protected abstract boolean isEndLicenseLine(String line);

	/**
	 * プロキシポート.を取得する.
	 * 
	 * @return プロキシポート.
	 */
	protected int getProxyPort() {
		return proxyPort;
	}

	/**
	 * プロキシホスト.を取得する.
	 * 
	 * @return プロキシホスト.
	 */
	protected String getProxyHost() {
		return proxyHost;
	}

	/**
	 * ユーザID.を取得する.
	 * 
	 * @return ユーザID.
	 */
	protected String getUserId() {
		return userId;
	}

	/**
	 * パスワード.を取得する.
	 * 
	 * @return パスワード.
	 */
	protected String getPassword() {
		return password;
	}

	/**
	 * プロキシを利用するかどうか.を取得する.
	 * 
	 * @return プロキシを利用するかどうか.
	 */
	protected boolean isUseProxy() {
		return useProxy;
	}

}
