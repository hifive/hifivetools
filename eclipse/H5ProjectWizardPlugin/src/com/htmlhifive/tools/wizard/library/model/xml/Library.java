package com.htmlhifive.tools.wizard.library.model.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
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
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}site" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.htmlhifive.com/schema/libraries}version" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "site" })
@XmlRootElement(name = "library", namespace = "http://www.htmlhifive.com/schema/libraries")
public class Library {

	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries")
	protected List<Site> site;
	@XmlAttribute(name = "version", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String version;

	/**
	 * Gets the value of the site property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the site property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSite().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Site }
	 */
	public List<Site> getSite() {
		if (site == null) {
			site = new ArrayList<Site>();
		}
		return this.site;
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
