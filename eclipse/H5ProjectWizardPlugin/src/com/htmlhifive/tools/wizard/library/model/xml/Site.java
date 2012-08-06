package com.htmlhifive.tools.wizard.library.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="url" use="required" type="{http://www.htmlhifive.com/schema/libraries}url" />
 *       &lt;attribute name="file-pattern" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *       &lt;attribute name="extract-path" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *       &lt;attribute name="replace-file-name" type="{http://www.htmlhifive.com/schema/libraries}siteName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "site", namespace = "http://www.htmlhifive.com/schema/libraries")
public class Site {

	@XmlAttribute(name = "url", required = true)
	protected String url;
	@XmlAttribute(name = "file-pattern")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	protected String filePattern;
	@XmlAttribute(name = "extract-path")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlSchemaType(name = "normalizedString")
	protected String extractPath;
	@XmlAttribute(name = "replace-file-name")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String replaceFileName;

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
	 * filePatternプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getFilePattern() {
		return filePattern;
	}

	/**
	 * filePatternプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setFilePattern(String value) {
		this.filePattern = value;
	}

	/**
	 * extractPathプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getExtractPath() {
		return extractPath;
	}

	/**
	 * extractPathプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setExtractPath(String value) {
		this.extractPath = value;
	}

	/**
	 * replaceFileNameプロパティの値を取得します。
	 * 
	 * @return possible object is {@link String }
	 */
	public String getReplaceFileName() {
		return replaceFileName;
	}

	/**
	 * replaceFileNameプロパティの値を設定します。
	 * 
	 * @param value allowed object is {@link String }
	 */
	public void setReplaceFileName(String value) {
		this.replaceFileName = value;
	}

}
