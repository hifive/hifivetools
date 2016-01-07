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
package com.htmlhifive.tools.jslint.engine.option.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.htmlhifive.tools.jslint.logger.JSLintPluginLogger;
import com.htmlhifive.tools.jslint.logger.JSLintPluginLoggerFactory;
import com.htmlhifive.tools.jslint.messages.Messages;

/**
 * @author miyauchi
 * 
 */
public final class JaxbUtil {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(JaxbUtil.class);

	/**
	 * JAXBContext.
	 */
	private static JAXBContext jc;

	static {
		try {
			jc = JAXBContext.newInstance(JsCheckOption.class);
		} catch (JAXBException e) {
			// ignore
		}
	}

	/**
	 * JsCheckOptionsをxml形式で保存する.
	 * 
	 * 
	 * @param checkOptions オプション.
	 * @param output 出力ファイル.
	 */
	public static void saveJsCheckOption(JsCheckOption checkOptions, IFile output) {
		try {
			Marshaller marshall = jc.createMarshaller();
			marshall.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(true));
			StringWriter writer = new StringWriter();
			marshall.marshal(checkOptions, writer);
			if (output.exists()) {
				output.setContents(IOUtils.toInputStream(writer.toString(), "UTF-8"), IResource.FORCE, null);
			} else {
				output.create(IOUtils.toInputStream(writer.toString(), "UTF-8"), true, null);
			}
			output.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (JAXBException e) {
			logger.put(Messages.EM0006, e, output.getName());
		} catch (CoreException e) {
			logger.put(Messages.EM0100, e);
		} catch (IOException e) {
			logger.put(Messages.EM0006, e, output.getName());
		}
	}

	/**
	 * オプションxmlを読み込む.
	 * 
	 * @param is 読み込むinputStream
	 * @return 読み込んだオプション.
	 */
	public static JsCheckOption readJsCheckOption(InputStream is) {
		try {
			Unmarshaller um = jc.createUnmarshaller();
			return (JsCheckOption) um.unmarshal(is);
		} catch (JAXBException e) {
			logger.put(Messages.EM0100, e);
			return null;
		}
	}

	/**
	 * コンストラクタ.
	 */
	private JaxbUtil() {
		// ignore
	}

}
