package com.htmlhifive.tools.wizard.library.model.xml;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}nature" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "nature" })
@XmlRootElement(name = "natures", namespace = "http://www.htmlhifive.com/schema/libraries")
public class Natures {

	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries", required = true)
	protected List<Nature> nature;

	/**
	 * Gets the value of the nature property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the nature property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNature().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Nature }
	 */
	public List<Nature> getNature() {
		if (nature == null) {
			nature = new ArrayList<Nature>();
		}
		return this.nature;
	}

}
