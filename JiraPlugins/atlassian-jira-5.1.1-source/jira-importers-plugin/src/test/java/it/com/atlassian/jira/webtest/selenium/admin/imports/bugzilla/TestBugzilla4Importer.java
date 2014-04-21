/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;

public class TestBugzilla4Importer extends TestBugzillaImporter {

	@Override
	public void setUpTest() {
		super.setUpTest();
		instance = ITUtils.BUGZILLA_4_0;
	}

}
