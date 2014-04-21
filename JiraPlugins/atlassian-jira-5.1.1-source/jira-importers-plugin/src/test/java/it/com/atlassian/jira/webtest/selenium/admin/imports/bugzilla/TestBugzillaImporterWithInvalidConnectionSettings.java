/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.plugins.importer.po.bugzilla.BugzillaImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.pageobjects.TestedProductFactory;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Test;

import java.util.List;

public class TestBugzillaImporterWithInvalidConnectionSettings extends FuncTestCase {

    private static final String INVALID_MYSQL_ADDRESS = "192.168.157.161";

	private JiraTestedProduct product;

    @Override
    protected void setUpTest() {
		// no need to administration.restoreBlankInstance() IMO
		product = TestedProductFactory.create(JiraTestedProduct.class);
    }

    @Test
    public void testInvalidMySqlAddress() {
		final CommonImporterSetupPage setupPage = product.gotoLoginPage().loginAsSysAdmin(BugzillaImporterSetupPage.class);
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);
		final List<String> errors = setupPage.setJdbcHostname(INVALID_MYSQL_ADDRESS)
				.nextWithError()
				.getGlobalErrors2();
		assertEquals("Error connecting to the database: Could not create connection to database server. Attempted reconnect 3 times. Giving up.",
				Iterables.getOnlyElement(errors));
    }

}
