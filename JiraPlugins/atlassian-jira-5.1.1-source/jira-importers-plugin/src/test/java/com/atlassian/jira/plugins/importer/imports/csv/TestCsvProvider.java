/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.google.common.collect.Multimap;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

public class TestCsvProvider extends TestCase {
	MindProdCsvProvider testObject;
	MindProdCsvProvider testObject2;

	protected void setUp() throws Exception {
		//test with standard delimiter (comma = ,)
		testObject = new MindProdCsvProvider(new File("src/test/resources/testcsvmapper.csv"), "UTF-8",
				new HeaderRowCsvMapper(), null);
		testObject.startSession();
		//test with custom delimiter (cash = $)
		testObject2 = new MindProdCsvProvider(new File("src/test/resources/testcsvdelimiter.csv"), "UTF-8",
				new HeaderRowCsvMapper(), Character.valueOf('$'));
		testObject2.startSession();
	}

	protected void tearDown() throws Exception {
		testObject.stopSession();
		testObject2.stopSession();
	}

	public void testGetDelimiter() {
		assertEquals(',', testObject.getDelimiter());
		assertEquals('$', testObject2.getDelimiter());
	}

	public void testLine() throws Exception {
		// Execute
		Multimap collection = testObject.getNextLine();
		assertNotNull(collection);
		assertEquals(10, collection.size());

		collection = testObject2.getNextLine();
		assertNotNull(collection);
		assertEquals(10, collection.size());
	}

	public void testGetRestOfFile() throws Exception {
		// Execute
		List map = testObject.getRestOfFile();
		assertNotNull(map);
		assertEquals(3, map.size());

		map = testObject2.getRestOfFile();
		assertNotNull(map);
		assertEquals(3, map.size());
	}
}

