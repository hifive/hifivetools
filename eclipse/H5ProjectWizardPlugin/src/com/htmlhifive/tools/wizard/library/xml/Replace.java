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
 *         &lt;element ref="{http://www.htmlhifive.com/schema/libraries}file" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="target" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "file" })
@XmlRootElement(name = "replace", namespace = "http://www.htmlhifive.com/schema/libraries")
public class Replace {

	@XmlElement(namespace = "http://www.htmlhifive.com/schema/libraries", required = true)
	protected List<File> file;
	@XmlAttribute(name = "target")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	protected String target;

	/**
	 * Gets the value of the file property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the file property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFile().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link File }
	 */
	public List<File> getFile() {

		if (file == null) {
			file = new ArrayList<File>();
		}
		return this.file;
	}

	/**
	 * targetプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getTarget() {

		return target;
	}

	/**
	 * targetプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setTarget(String value) {

		this.target = value;
	}

}
