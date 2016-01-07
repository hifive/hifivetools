package com.htmlhifive.tools.wizard.download;

import java.io.IOException;
import java.io.InputStream;

/**
 * <H3>コンテンツ取得用のハンドラ.</H3>
 * 
 * @author fkubo
 */
public interface IFileContentsHandler {
	/**
	 * 入力ストリームを取得する.
	 * 
	 * @return 入力ストリーム
	 * @throws IOException IO例外
	 */
	InputStream getInputStream() throws IOException;
}
