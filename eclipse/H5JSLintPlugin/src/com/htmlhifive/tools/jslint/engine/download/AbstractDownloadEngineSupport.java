/*
 * Copyright (C) 2012-2016 NS Solutions Corporation
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
package com.htmlhifive.tools.jslint.engine.download;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

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
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * ダウンロード支援の抽象クラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public abstract class AbstractDownloadEngineSupport implements DownloadEngineSupport {

	/**
	 * コネクションタイムアウト時間.
	 */
	private static final int CONNECTION_TIMEOUT = 10000;

	/**
	 * リトライ回数.
	 */
	private static final int RETRY_TIMES = 0;

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
		actualMonitor.setTaskName(Messages.T0009.getText());
		HttpClient client = createHttpClient(getEngineSourceUrl());
		HttpMethod getMethod = new GetMethod(getEngineSourceUrl());
		int result = client.executeMethod(getMethod);
		if (result != HttpStatus.SC_OK) {
			// TODO 失敗
			return null;
		}
		StringBuilder licenseSb = new StringBuilder();
		StringBuilder rawSource = new StringBuilder();
		Header header = getMethod.getResponseHeader("Content-Length");
		int content = Integer.valueOf(header.getValue());
		actualMonitor.beginTask(Messages.T0010.getText(), content);
		BufferedReader reader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
		String temp = reader.readLine();
		int progress = 0;
		while (!isEndLicenseLine(temp)) {
			progress += temp.length();
			actualMonitor.subTask(Messages.T0011.format(progress, content));
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
			progress += temp.length();
			actualMonitor.subTask(Messages.T0011.format(progress, content));
			actualMonitor.worked(temp.length());
			rawSource.append(temp);
			rawSource.append(System.getProperty("line.separator"));
		}
		info.setMainSource(rawSource.toString());
		monitor.done();
		return info;
	}

	/**
	 * 引数のurlに対して、eclipseのネットワーク設定を適用したHttpClientオブジェクトを生成する.<br>
	 * コネクションタイムアウトは
	 * {@link AbstractDownloadEngineSupport#getConnectionTimeout()}.<br>
	 * リトライ回数は {@link AbstractDownloadEngineSupport#getRetryTimes()}
	 * 
	 * @param url URL
	 * @return HttpClient
	 */
	private HttpClient createHttpClient(String url) {
		ServiceTracker<IProxyService, IProxyService> proxyTracker = new ServiceTracker<IProxyService, IProxyService>(
				JSLintPlugin.getDefault().getBundle().getBundleContext(), IProxyService.class, null);
		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = 0;
		String userId = null;
		String password = null;
		try {
			proxyTracker.open();
			IProxyService service = proxyTracker.getService();
			IProxyData[] datas = service.select(new URI(url));
			for (IProxyData proxyData : datas) {
				if (proxyData.getHost() != null) {
					useProxy = true;
					proxyHost = proxyData.getHost();
					proxyPort = proxyData.getPort();
					userId = proxyData.getUserId();
					password = proxyData.getPassword();
				}
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(Messages.EM0100.getText(), e);
		} finally {
			proxyTracker.close();
		}
		HttpClient client = new HttpClient();
		if (useProxy) {
			client.getHostConfiguration().setProxy(proxyHost, proxyPort);
			if (StringUtils.isNotEmpty(userId)) {
				// 認証が必要な場合
				client.getState().setProxyCredentials(new AuthScope(proxyHost, proxyPort, "realm"),
						new UsernamePasswordCredentials(userId, password));
			}
		}
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(getRetryTimes(), true));
		client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, getConnectionTimeout());
		return client;
	}

	/**
	 * 接続アウト時間を取得する.デフォルトは10000ms.
	 * 
	 * @return 接続アウト時間.
	 */
	protected int getConnectionTimeout() {
		return CONNECTION_TIMEOUT;
	}

	/**
	 * リトライ回数を取得する.デフォルトは0.
	 * 
	 * @return リトライ回数.
	 */
	protected int getRetryTimes() {
		return RETRY_TIMES;
	}

	/**
	 * 引数の文字列がライセンステキスト終了の行かどうかを判定する.<br>
	 * JSLint派生のライセンスはファイル先頭に全てのライセンス条文が書いてある.
	 * 
	 * @param line 検査対象の一行.
	 * @return ライセンス終了の行かどうか.
	 */
	protected abstract boolean isEndLicenseLine(String line);

}
