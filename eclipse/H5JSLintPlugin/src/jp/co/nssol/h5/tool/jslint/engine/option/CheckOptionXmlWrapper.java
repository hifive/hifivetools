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
 package jp.co.nssol.h5.tool.jslint.engine.option;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import jp.co.nssol.h5.tool.jslint.engine.option.xml.JsCheckOption;
import jp.co.nssol.h5.tool.jslint.engine.option.xml.ObjectFactory;
import jp.co.nssol.h5.tool.jslint.engine.option.xml.XmlOption;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLogger;
import jp.co.nssol.h5.tool.jslint.logger.JSLintPluginLoggerFactory;
import jp.co.nssol.h5.tool.jslint.messages.Messages;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * チェックオプションのXmlファイルのラッパークラス.
 * 
 * @author NS Solutions Corporation
 * 
 */
public class CheckOptionXmlWrapper implements CheckOptionFileWrapper {

	/**
	 * ロガー.
	 */
	private static JSLintPluginLogger logger = JSLintPluginLoggerFactory.getLogger(CheckOptionXmlWrapper.class);

	/**
	 * チェックオプション.
	 */
	private JsCheckOption checkOptions;

	/**
	 * オプションファイル(.xml).
	 */
	private IFile xmlOption;

	/**
	 * JAXBContext.
	 */
	private JAXBContext jc;

	/**
	 * コンストラクタ.
	 * 
	 * @param xmlOption .xmlのオプションファイル.
	 * @throws JAXBException xml解析例外.
	 * @throws CoreException 生成例外.
	 */
	public CheckOptionXmlWrapper(IFile xmlOption) throws CoreException, JAXBException {

		this(xmlOption.getContents());
		this.xmlOption = xmlOption;

	}

	/**
	 * コンストラクタ.
	 * 
	 * @param optionXml .xmlのオプションファイル.
	 * @throws JAXBException xml解析例外.
	 */
	public CheckOptionXmlWrapper(InputStream optionXml) throws JAXBException {

		jc = JAXBContext.newInstance(JsCheckOption.class);
		Unmarshaller um = jc.createUnmarshaller();
		checkOptions = (JsCheckOption) um.unmarshal(optionXml);
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * jp.co.nssol.h5.tool.jslint.engine.option.CheckOptionWrapper#getOption
	 * (java.lang.String)
	 */
	@Override
	public CheckOption getOption(String key, String engine) {

		for (XmlOption optiontype : checkOptions.getOption()) {
			try {
				if (StringUtils.equals(optiontype.getKey(), key) && StringUtils.equals(optiontype.getEngine(), engine)) {
					CheckOption option = new CheckOption(key, optiontype.getEngine(), Class.forName(optiontype
							.getType()), optiontype.getDescription(), optiontype.getDetail());
					option.setEnable(Boolean.valueOf(optiontype.isState()));
					option.setValue(optiontype.getValue());
					return option;
				}
			} catch (ClassNotFoundException e) {
				logger.put(Messages.EM0007, e, optiontype.getType());
			}
		}

		return null;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * jp.co.nssol.h5.tool.jslint.engine.option.CheckOptionWrapper#getOptions()
	 */
	@Override
	public CheckOption[] getOptions(Engine engine) {

		List<CheckOption> checkOptionList = new ArrayList<CheckOption>();
		for (XmlOption optionType : this.checkOptions.getOption()) {
			if (contains(engine, optionType)) {
				checkOptionList.add(getOption(optionType.getKey(), optionType.getEngine()));
			}
		}
		return (CheckOption[]) checkOptionList.toArray(new CheckOption[checkOptionList.size()]);
	}

	/**
	 * オプションが指定したエンジンかどうかチェックする.
	 * 
	 * @param engines エンジン.
	 * @param optionType オプション
	 * @return オプションが指定したエンジンに含まれればtrue,そうでない場合はfalse
	 */
	private boolean contains(Engine engines, XmlOption optionType) {

		String[] engineKeys = StringUtils.split(optionType.getEngine(), ",");
		if (engineKeys == null) {
			return false;
		}
		for (String engineKey : engineKeys) {
			if (engines.getKey().equals(engineKey)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public CheckOption[] getEnableOptions(Engine engine) {

		List<CheckOption> checkOptionList = new ArrayList<CheckOption>();
		for (XmlOption optionType : this.checkOptions.getOption()) {
			if (contains(engine, optionType) && optionType.isState()) {
				checkOptionList.add(getOption(optionType.getKey(), optionType.getEngine()));
			}
		}
		return (CheckOption[]) checkOptionList.toArray(new CheckOption[checkOptionList.size()]);
	}

	@Override
	public void saveOption() {

		try {
			Marshaller marshall = jc.createMarshaller();
			marshall.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(true));
			marshall.marshal(checkOptions, new File(xmlOption.getLocationURI()));
			xmlOption.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (JAXBException e) {
			logger.put(Messages.EM0006, e, xmlOption.getName());
			// throw new
			// RuntimeException(Messages.EM0006.format(xmlOption.getName()), e);
		} catch (CoreException e) {
			logger.put(Messages.EM0100, e);
			// throw new RuntimeException(Messages.EM0100.getText(), e);
		}
	}

	@Override
	public void addOption(CheckOption option) {

		for (XmlOption xmlOption : checkOptions.getOption()) {
			if (StringUtils.equals(xmlOption.getKey(), option.getKey())
					&& StringUtils.equals(xmlOption.getEngine(), option.getEngine())) {
				return;
			}
		}
		this.checkOptions.getOption().add(convertToXmlOption(option));

	}

	/**
	 * XmlOptionに変換する.
	 * 
	 * @param option 変換前オプション.
	 * @return XmlOption
	 */
	private XmlOption convertToXmlOption(CheckOption option) {

		ObjectFactory factory = new ObjectFactory();
		XmlOption xmlOption = factory.createXmlOption();
		updateOption(xmlOption, option);

		return xmlOption;
	}

	/**
	 * XmlOptionをCheckOptionで更新する.
	 * 
	 * @param from 更新前XmlOption
	 * @param to 更新CheckOption
	 */
	private void updateOption(XmlOption from, CheckOption to) {

		from.setDescription(to.getDescription());
		from.setDetail(to.getDetail());
		from.setKey(to.getKey());
		from.setState(to.isEnable());
		from.setType(to.getClazz().getName());
		from.setValue(to.getValue());
		from.setEngine(to.getEngine());

	}

	@Override
	public void updateOption(CheckOption option) {

		for (XmlOption xmlOption : checkOptions.getOption()) {
			if (StringUtils.equals(xmlOption.getKey(), option.getKey())
					&& StringUtils.equals(xmlOption.getEngine(), option.getEngine())) {
				updateOption(xmlOption, option);
			}
		}
	}
}