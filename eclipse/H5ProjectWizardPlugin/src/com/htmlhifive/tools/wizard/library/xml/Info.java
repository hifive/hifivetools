package com.htmlhifive.tools.wizard.library.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
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
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}title" minOccurs="0"/>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}description" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lang" type="{http://www.htmlhifive.com/schema/libraries}lang" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "title", "description" })
@XmlRootElement(name = "info", namespace = "http://www.htmlhifive.com/schema/libraries")
public class Info {

	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	protected String title;
	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	protected String description;
	@XmlAttribute(name = "lang")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String lang;

	/**
	 * titleプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getTitle() {

		return title;
	}

	/**
	 * titleプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setTitle(String value) {

		this.title = value;
	}

	/**
	 * descriptionプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getDescription() {

		return description;
	}

	/**
	 * descriptionプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setDescription(String value) {

		this.description = value;
	}

	/**
	 * langプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLang() {

		return lang;
	}

	/**
	 * langプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setLang(String value) {

		this.lang = value;
	}

}
