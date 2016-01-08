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
package com.htmlhifive.tools.codeassist.core.config;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.htmlhifive.tools.codeassist.core.config.bean.AllBean;
import com.htmlhifive.tools.codeassist.core.config.xml.H5CodeAssist;
import com.htmlhifive.tools.codeassist.core.exception.ParseException;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLogger;
import com.htmlhifive.tools.codeassist.core.logger.H5CodeAssistPluginLoggerFactory;
import com.htmlhifive.tools.codeassist.core.messages.Messages;


/**
 * xml形式のコードアシストオプションファイルを解析するクラス.<br>
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class XmlConfigCodeAssistParser implements ConfigFileParser {

	/**
	 * ロガー.
	 */
	private static H5CodeAssistPluginLogger logger = H5CodeAssistPluginLoggerFactory
			.getLogger(XmlConfigCodeAssistParser.class);

	/**
	 * xmlファイルから取得した補完情報のリスト.
	 */
	private AllBean allBean;

	/**
	 * コンストラクタ.
	 * 
	 * @param option オプションファイルの入力ストリーム.
	 * @throws ParseException xml解析例外.
	 * 
	 * 
	 */
	XmlConfigCodeAssistParser(InputStream option) throws ParseException {

		try {
			JAXBContext jc = JAXBContext.newInstance(H5CodeAssist.class);
			Unmarshaller um = jc.createUnmarshaller();
			allBean = getAllBean(um, option);
		} catch (JAXBException e) {
			logger.log(Messages.EM0003, e);
			throw new ParseException(e);
		}
	}

	@Override
	public AllBean getCodeAssistBean() throws ParseException {

		return allBean;
	}

	/**
	 * 指定したストリームのコード補完情報を全て取得する.
	 * 
	 * @param um アンマーシャラー.
	 * @param option オプションの入力ストリーム
	 * @return コード補完情報
	 * @throws ParseException 解析例外.
	 */
	private AllBean getAllBean(Unmarshaller um, InputStream option) throws ParseException {

		H5CodeAssist assist;
		try {
			assist = (H5CodeAssist) um.unmarshal(option);
		} catch (JAXBException e) {
			logger.log(Messages.EM0003, e);
			throw new ParseException(e);
		}
		return new AllBean(assist);
	}

}
