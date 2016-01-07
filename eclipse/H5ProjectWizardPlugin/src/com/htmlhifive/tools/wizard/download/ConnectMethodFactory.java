package com.htmlhifive.tools.wizard.download;

import com.htmlhifive.tools.wizard.utils.H5IOUtils;

/**
 * <H3>URLに応じたHttpMethodを返す.</H3>
 * 
 * @author fkubo
 */
public abstract class ConnectMethodFactory {
	/**
	 * URLに応じたHttpMethodを取得する.
	 * 
	 * @param urlStr　URL
	 * @param isFirst 最初の接続かどうか.
	 * @return
	 */
	public static IConnectMethod getMethod(String urlStr, boolean isFirst) {

		if (H5IOUtils.isClassResources(urlStr)) {
			// クラスパスから取得する.
			return new URLConnectionImpl(urlStr);
		}

		if (isFirst) {
			//return new HttpGetMethodImpl(urlStr);
			return new URLConnectionImplEx(urlStr);
		}

		return new URLConnectionImpl(urlStr);

	}
}
