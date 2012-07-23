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
package jp.co.nssol.h5.tool.jslint.engine.option.xml;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLoggerFactory;
import jp.co.nssol.h5.tool.jslint.messages.Messages;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

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
	private static JAXBContext JC;

	static {
		try {
			JC = JAXBContext.newInstance(JsCheckOption.class);
		} catch (JAXBException e) {
			// ignore
		}
	}

	public static void saveJsCheckOption(JsCheckOption checkOptions, IFile output) {
		try {
			Marshaller marshall = JC.createMarshaller();
			marshall.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(true));
			StringWriter writer = new StringWriter();
			marshall.marshal(checkOptions, writer);
			if (output.exists()) {
				output.setContents(IOUtils.toInputStream(writer.toString()), IResource.FORCE, null);
			} else {
				output.create(IOUtils.toInputStream(writer.toString()), true, null);
			}
			output.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (JAXBException e) {
			logger.put(Messages.EM0006, e, output.getName());
			// throw new
			// RuntimeException(Messages.EM0006.format(xmlOption.getName()), e);
		} catch (CoreException e) {
			logger.put(Messages.EM0100, e);
			// throw new RuntimeException(Messages.EM0100.getText(), e);
		}
	}

	public static JsCheckOption readJsCheckOption(InputStream is) {
		try {
			Unmarshaller um = JC.createUnmarshaller();
			return (JsCheckOption) um.unmarshal(is);
		} catch (JAXBException e) {
			logger.put(Messages.EM0100, e);
			return null;
		}
	}

	private JaxbUtil() {
		// ignore
	}

}
