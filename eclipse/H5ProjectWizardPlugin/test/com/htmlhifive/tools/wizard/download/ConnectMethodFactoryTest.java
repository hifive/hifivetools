/**
 * 
 */
package com.htmlhifive.tools.wizard.download;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;

import mockit.Expectations;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.junit.Test;

/**
 * <H3>ConnectMethodFactoryのテストクラス.</H3>
 * 
 * @author fkubo
 */
public class ConnectMethodFactoryTest {

	/**
	 * typeテストメソッド.
	 */
	@Test
	public void testType() {
		assertThat(ConnectMethodFactory.class, notNullValue());
	}

	/**
	 * {@link ConnectMethodFactory#ConnectMethodFactory()}用テストメソッド.
	 */
	@Test
	public void testInstantiation() {
		ConnectMethodFactory target = new ConnectMethodFactory(){};
		assertThat(target, notNullValue());
	}

	/**
	 * {@link ConnectMethodFactory#getMethod(String, boolean)}用テストメソッド.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetMethodStringboolean01(final IProxyService proxyService) throws IOException {

		// Arrange：正常系
		final String urlStr = "http://stackoverflow.com/";//"http://www.htmlhifive.com";
		final boolean isFirst = true;
		new Expectations() {
			{
				proxyService.select((URI) any);
				//type: HTTP host: proxy10.sysrdc.ns-sol.co.jp port: 9000 user: null password: null reqAuth: false source: WINDOWS_IE dynamic: false
				ProxyData proxyData = new ProxyData("HTTP", "localhost", 3128, false, "WINDOWS_IE"); // TODO:プロキシを立てる必要あり
				result = new ProxyData[] { proxyData };
			}
		};

		// Act
		IConnectMethod actual = ConnectMethodFactory.getMethod(urlStr, isFirst);
		System.out.println(actual.getClass()); //

		actual.setProxy(proxyService);

		System.out.println(IOUtils.toString(actual.getInputStream()));

		// Assert：結果が正しいこと
		// 現在変更中 assertThat(actual.getClass().getName(), is(HttpGetMethodImpl.class.getName()));
		assertThat(actual.getClass().getName(), is(URLConnectionImplEx.class.getName()));
		assertThat(actual, notNullValue());
	}

	/**
	 * {@link ConnectMethodFactory#getMethod(String, boolean)}用テストメソッド.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetMethodStringboolean02(final IProxyService proxyService) throws IOException {

		// Arrange：正常系
		final String urlStr = "http://www.htmlhifive.com";
		final boolean isFirst = false;
		new Expectations(){{
			proxyService.select((URI) any);
			ProxyData proxyData = new ProxyData("HTTP", "localhost", 3128, false, "WINDOWS_IE"); // TODO:プロキシを立てる必要あり
			result = new ProxyData[] { proxyData };
		}};


		// Act
		IConnectMethod actual = ConnectMethodFactory.getMethod(urlStr, isFirst);
		System.out.println(actual.getClass());

		actual.setProxy(proxyService);

		System.out.println(IOUtils.toString(actual.getInputStream()));

		// Assert：結果が正しいこと
		assertThat(actual.getClass().getName(), is(URLConnectionImpl.class.getName()));
		assertThat(actual, notNullValue());
	}

}
