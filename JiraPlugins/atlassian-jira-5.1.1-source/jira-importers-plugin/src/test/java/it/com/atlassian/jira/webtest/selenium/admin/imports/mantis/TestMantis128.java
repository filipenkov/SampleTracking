/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;

public class TestMantis128 extends TestMantis124 {

	@Override
	public void setUpTest() {
        super.setUpTest();
        instance = ITUtils.MANTIS_1_2_8;
	}

}
