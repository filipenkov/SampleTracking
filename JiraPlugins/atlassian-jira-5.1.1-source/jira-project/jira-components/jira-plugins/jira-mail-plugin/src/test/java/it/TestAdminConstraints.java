/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pageobjects.EmptyOutgoingMailPage;
import pageobjects.IncomingServersPage;

import static org.junit.Assert.assertFalse;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS })
public class TestAdminConstraints extends BaseJiraWebTest
{
	@Before
	public void restore() {
		backdoor.restoreData("noServers.zip");
	}

	@Test
    public void test() {
        backdoor.usersAndGroups().addUser("nonsystemadmin", "nonsystemadmin", "Non system admin", "nonsystemadmin@localhost");
        backdoor.permissions().addGlobalPermission(FunctTestConstants.ADMINISTER, "jira-users");
        jira.gotoLoginPage().login("nonsystemadmin", "nonsystemadmin", IncomingServersPage.class);

        testOutgoingMailTab();
        testNoIncomingServersMessageIsPresentWhenNotSysadmin();
    }

    private void testNoIncomingServersMessageIsPresentWhenNotSysadmin()
    {
        IncomingServersPage incomingServersPage = jira.visit(IncomingServersPage.class);
        Assert.assertTrue(incomingServersPage.hasNoServersHint());
        Assert.assertFalse(incomingServersPage.isAddHandlerEnabled());
    }

    private void testOutgoingMailTab() {
        IncomingServersPage serversPage = jira.visit(IncomingServersPage.class);

        assertFalse(serversPage.isOutgoingMailTabVisible());

        // it will bind login page (expected) or throw an exception
        jira.visit(EmptyOutgoingMailPage.class);

        // check if we can visit incoming servers page, throws an exception if we can't
        jira.visit(IncomingServersPage.class);
    }
}
