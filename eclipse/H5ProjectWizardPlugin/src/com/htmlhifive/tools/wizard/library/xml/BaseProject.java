package com.htmlhifive.tools.wizard.library.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>
 * anonymous complex typeのJavaクラス。
 * <p>
 * 次のスキーマ・フラグメントは、このクラス内に含まれる予期されるコンテンツを指定します。
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}info" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}replace" minOccurs="0"/>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}natures" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="url" use="required" type="{http://www.htmlhifive.com/schema/libraries}url" />
 *       &lt;attribute name="default-js-lib-path" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "info", "replace", "natures" })
@XmlRootElement(name = "base-project", namespace = "http://www.htmlhifive.com/schema/libraries")
public class BaseProject {

	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries", required = true)
	protected List<Info> info;
	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	protected Replace replace;
	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	protected Natures natures;
	@XmlAttribute(name = "url", required = true)
	protected String url;
	@XmlAttribute(name = "default-js-lib-path")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	protected String defaultJsLibPath;

	/**
	 * Gets the value of the info property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the info property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getInfo().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Info }
	 */
	public List<Info> getInfo() {

		if (info == null) {
			info = new ArrayList<Info>();
		}
		return this.info;
	}

	/**
	 * replaceプロパティの値を取得します。
	 * 
	 * @return possible object is {@link Replace }
	 */
	public Replace getReplace() {

		return replace;
	}

	/**
	 * replaceプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link Replace }
	 */
	public void setReplace(Replace value) {

		this.replace = value;
	}

	/**
	 * naturesプロパティの値を取得します。
	 * 
	 * @return possible object is {@link Natures }
	 */
	public Natures getNatures() {

		return natures;
	}

	/**
	 * naturesプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link Natures }
	 */
	public void setNatures(Natures value) {

		this.natures = value;
	}

	/**
	 * urlプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getUrl() {

		return url;
	}

	/**
	 * urlプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setUrl(String value) {

		this.url = value;
	}

	/**
	 * defaultJsLibPathプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getDefaultJsLibPath() {

		return defaultJsLibPath;
	}

	/**
	 * defaultJsLibPathプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setDefaultJsLibPath(String value) {

		this.defaultJsLibPath = value;
	}

}
