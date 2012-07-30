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
package com.htmlhifive.tools.wizard.library;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.htmlhifive.tools.wizard.library.model.LibraryList;
import com.htmlhifive.tools.wizard.library.model.xml.Libraries;
import com.htmlhifive.tools.wizard.log.PluginLogger;
import com.htmlhifive.tools.wizard.log.PluginLoggerFactory;
import com.htmlhifive.tools.wizard.log.messages.Messages;

/**
 * xmlファイルを解析するクラス.<br>
 * 
 * @author fkubo
 */
public final class LibraryFileParserImpl implements LibraryFileParser {

	/**
	 * ロガー.
	 */
	private static PluginLogger logger = PluginLoggerFactory.getLogger(LibraryFileParserImpl.class);

	/** DOM. */
	private LibraryList libraryList;

	/**
	 * コンストラクタ.
	 * 
	 * @param option オプションファイルの入力ストリーム.
	 * @throws ParseException xml解析例外.
	 */
	LibraryFileParserImpl(InputStream option) throws ParseException {

		try {
			JAXBContext jc = JAXBContext.newInstance(Libraries.class);
			Unmarshaller um = jc.createUnmarshaller();
			Libraries libraries = getLibraries(um, option);

			// 初期処理.
			libraryList = new LibraryList(libraries);
			libraryList.init();
		} catch (JAXBException e) {
			logger.log(Messages.SE0011, e);
			throw new ParseException(e);
		}
	}

	@Override
	public LibraryList getLibraryList() throws ParseException {

		return libraryList;
	}

	/**
	 * 指定したストリームのコード補完情報を全て取得する.
	 * 
	 * @param um アンマーシャラー.
	 * @param option オプションの入力ストリーム
	 * @return コード補完情報
	 * @throws ParseException 解析例外.
	 */
	private static Libraries getLibraries(Unmarshaller um, InputStream option) throws ParseException {

		try {
			return (Libraries) um.unmarshal(option);
		} catch (JAXBException e) {
			logger.log(Messages.SE0011, e);
			throw new ParseException(e);
		}
	}
}
