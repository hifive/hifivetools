package com.htmlhifive.tools.wizard.library.model.xml;

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
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}info" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}license" minOccurs="0"/>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}library" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="org" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.htmlhifive.com/schema/libraries}org">
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="name" use="required" type="{http://www.htmlhifive.com/schema/libraries}categoryName" />
 *       &lt;attribute name="path" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *       &lt;attribute name="licenseUrl" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "info", "license", "library" })
@XmlRootElement(name = "category", namespace = "http://www.htmlhifive.com/schema/libraries")
public class Category {

	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	protected List<Info> info;
	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	protected String license;
	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	protected List<Library> library;
	@XmlAttribute(name = "org", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String org;
	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String name;
	@XmlAttribute(name = "path", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	protected String path;
	@XmlAttribute(name = "licenseUrl")
	@XmlSchemaType(name = "anyURI")
	protected String licenseUrl;

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
	 * licenseプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * licenseプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setLicense(String value) {
		this.license = value;
	}

	/**
	 * Gets the value of the library property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the library property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getLibrary().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Library }
	 */
	public List<Library> getLibrary() {
		if (library == null) {
			library = new ArrayList<Library>();
		}
		return this.library;
	}

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
	 * pathプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getPath() {
		return path;
	}

	/**
	 * pathプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setPath(String value) {
		this.path = value;
	}

	/**
	 * licenseUrlプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLicenseUrl() {
		return licenseUrl;
	}

	/**
	 * licenseUrlプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setLicenseUrl(String value) {
		this.licenseUrl = value;
	}

}
