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
 *
 */
package jp.co.nssol.h5.tools.codeassist.core.config;

import java.io.InputStream;

import jp.co.nssol.h5.tools.codeassist.core.H5CodeAssistCorePluginConst;
import jp.co.nssol.h5.tools.codeassist.core.exception.ParseException;

import org.apache.commons.lang.StringUtils;

/**
 * コードアシストパーサのファクトリクラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public final class ConfigFileParserFactory {

	/**
	 * コンストラクタ.
	 */
	private ConfigFileParserFactory() {

	}

	/**
	 * コードアシストのパーサを生成する.<br>
	 * 生成できない場合はnullを返却.
	 * 
	 * @param is オプションインプットストリーム.
	 * @param fileExtension オプションファイルの拡張子.
	 * @return パーサ.
	 * @throws ParseException 解析例外.
	 */
	public static ConfigFileParser createParser(InputStream is, String fileExtension) throws ParseException {

		if (StringUtils.equals(fileExtension, H5CodeAssistCorePluginConst.EXTENTION_XML)) {
			return new XmlConfigCodeAssistParser(is);
		}

		return null;
	}

}
