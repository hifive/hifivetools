package com.htmlhifive.tools.wizard.library.model.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * com.htmlhifive.tools.wizard.library.model.xml package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory methods for each of these are provided in
 * this class.
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _License_QNAME = new QName("http://www.htmlhifive.com/schema/libraries", "license");
	private final static QName _Title_QNAME = new QName("http://www.htmlhifive.com/schema/libraries", "title");
	private final static QName _Description_QNAME = new QName("http://www.htmlhifive.com/schema/libraries",
			"description");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
	 * com.htmlhifive.tools.wizard.library.model.xml
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link Libraries }
	 */
	public Libraries createLibraries() {
		return new Libraries();
	}

	/**
	 * Create an instance of {@link SiteLibraries }
	 */
	public SiteLibraries createSiteLibraries() {
		return new SiteLibraries();
	}

	/**
	 * Create an instance of {@link Category }
	 */
	public Category createCategory() {
		return new Category();
	}

	/**
	 * Create an instance of {@link Info }
	 */
	public Info createInfo() {
		return new Info();
	}

	/**
	 * Create an instance of {@link Library }
	 */
	public Library createLibrary() {
		return new Library();
	}

	/**
	 * Create an instance of {@link Site }
	 */
	public Site createSite() {
		return new Site();
	}

	/**
	 * Create an instance of {@link DefaultLibraries }
	 */
	public DefaultLibraries createDefaultLibraries() {
		return new DefaultLibraries();
	}

	/**
	 * Create an instance of {@link LibraryRef }
	 */
	public LibraryRef createLibraryRef() {
		return new LibraryRef();
	}

	/**
	 * Create an instance of {@link BaseProjects }
	 */
	public BaseProjects createBaseProjects() {
		return new BaseProjects();
	}

	/**
	 * Create an instance of {@link BaseProject }
	 */
	public BaseProject createBaseProject() {
		return new BaseProject();
	}

	/**
	 * Create an instance of {@link Replace }
	 */
	public Replace createReplace() {
		return new Replace();
	}

	/**
	 * Create an instance of {@link File }
	 */
	public File createFile() {
		return new File();
	}

	/**
	 * Create an instance of {@link Natures }
	 */
	public Natures createNatures() {
		return new Natures();
	}

	/**
	 * Create an instance of {@link Nature }
	 */
	public Nature createNature() {
		return new Nature();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
	 */
	@XmlElementDecl(namespace = "http://www.htmlhifive.com/schema/libraries", name = "license")
	public JAXBElement<String> createLicense(String value) {
		return new JAXBElement<String>(_License_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
	 */
	@XmlElementDecl(namespace = "http://www.htmlhifive.com/schema/libraries", name = "title")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	public JAXBElement<String> createTitle(String value) {
		return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
	 */
	@XmlElementDecl(namespace = "http://www.htmlhifive.com/schema/libraries", name = "description")
	public JAXBElement<String> createDescription(String value) {
		return new JAXBElement<String>(_Description_QNAME, String.class, null, value);
	}

}
