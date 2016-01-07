package com.htmlhifive.tools.wizard.library.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}site-libraries" minOccurs="0"/>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}default-libraries" minOccurs="0"/>
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}base-projects" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "siteLibraries", "defaultLibraries", "baseProjects" })
@XmlRootElement(name = "libraries", namespace = "http://www.htmlhifive.com/schema/libraries")
public class Libraries {

	@XmlElement(name = "site-libraries", namespace = "http://www.htmlhifive.com/schema/libraries")
	protected SiteLibraries siteLibraries;
	@XmlElement(name = "default-libraries", namespace = "http://www.htmlhifive.com/schema/libraries")
	protected DefaultLibraries defaultLibraries;
	@XmlElement(name = "base-projects", namespace = "http://www.htmlhifive.com/schema/libraries")
	protected BaseProjects baseProjects;

	/**
	 * siteLibrariesプロパティの値を取得します。
	 * 
	 * @return possible object is {@link SiteLibraries }
	 */
	public SiteLibraries getSiteLibraries() {
		return siteLibraries;
	}

	/**
	 * siteLibrariesプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link SiteLibraries }
	 */
	public void setSiteLibraries(SiteLibraries value) {
		this.siteLibraries = value;
	}

	/**
	 * defaultLibrariesプロパティの値を取得します。
	 * 
	 * @return possible object is {@link DefaultLibraries }
	 */
	public DefaultLibraries getDefaultLibraries() {
		return defaultLibraries;
	}

	/**
	 * defaultLibrariesプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link DefaultLibraries }
	 */
	public void setDefaultLibraries(DefaultLibraries value) {
		this.defaultLibraries = value;
	}

	/**
	 * baseProjectsプロパティの値を取得します。
	 * 
	 * @return possible object is {@link BaseProjects }
	 */
	public BaseProjects getBaseProjects() {
		return baseProjects;
	}

	/**
	 * baseProjectsプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link BaseProjects }
	 */
	public void setBaseProjects(BaseProjects value) {
		this.baseProjects = value;
	}

}
