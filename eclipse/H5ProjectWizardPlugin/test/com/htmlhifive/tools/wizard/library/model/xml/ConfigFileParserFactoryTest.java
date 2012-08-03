/*
 * Copyright (C) 2012 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.htmlhifive.tools.wizard.library.model.xml;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.htmlhifive.tools.wizard.library.LibraryFileParser;
import com.htmlhifive.tools.wizard.library.LibraryFileParserFactory;
import com.htmlhifive.tools.wizard.library.model.LibraryList;

/**
 * <H3>ConfigFileParserFactoryTest</H3>
 * 
 * @author fkubo
 */
public class ConfigFileParserFactoryTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void testCreateParser() throws Exception {

		InputStream is = null;
		is = ConfigFileParserFactoryTest.class.getResourceAsStream("default-libraries.xml");
		LibraryFileParser parser = LibraryFileParserFactory.createParser(is);
		try {
			LibraryList list = parser.getLibraryList();
			for (Category category : list.getLibraries().getSiteLibraries().getCategory()) {
				System.out.println("category: " + category.getName());
				for (Library library : category.getLibrary()) {
					System.out.println("\tlibrary: " + library.getVersion());
					for (Site site : library.getSite()) {
						System.out.println("\t\tsite: " + site.getUrl());
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

}
