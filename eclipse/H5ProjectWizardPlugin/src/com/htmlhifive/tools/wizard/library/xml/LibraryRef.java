package com.htmlhifive.tools.wizard.library.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
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
 *       &lt;attribute name="org" use="required" type="{http://www.htmlhifive.com/schema/libraries}org" />
 *       &lt;attribute name="name" use="required" type="{http://www.htmlhifive.com/schema/libraries}categoryName" />
 *       &lt;attribute name="version" use="required" type="{http://www.htmlhifive.com/schema/libraries}version" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "library-ref", namespace = "http://www.htmlhifive.com/schema/libraries")
public class LibraryRef {

	@XmlAttribute(name = "org", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String org;
	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String name;
	@XmlAttribute(name = "version", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String version;

	/**
	 * orgプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getOrg() {

		return org;
	}

	/**
	 * orgプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setOrg(String value) {

		this.org = value;
	}

	/**
	 * nameプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getName() {

		return name;
	}

	/**
	 * nameプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setName(String value) {

		this.name = value;
	}

	/**
	 * versionプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getVersion() {

		return version;
	}

	/**
	 * versionプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setVersion(String value) {

		this.version = value;
	}

}
