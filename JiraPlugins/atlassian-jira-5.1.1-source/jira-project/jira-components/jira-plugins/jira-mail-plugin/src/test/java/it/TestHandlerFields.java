/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import pageobjects.EditHandlerDetailsPage;
import pageobjects.EditServerDetailsPage;
import pageobjects.IncomingServersPage;

import static org.junit.Assert.assertEquals;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS })
public class TestHandlerFields extends BaseJiraWebTest
{
	@Before
	public void restore() {
		backdoor.restoreData("blank.zip");
	}

	@Test
    public void testFieldsForEachHandler() {
        backdoor.project().addProject("Test project", "TEST", "admin");

        EditServerDetailsPage serverPage = jira.gotoLoginPage().loginAsSysAdmin(IncomingServersPage.class).addHandler();
        serverPage.setSelect("handler", "Create a new issue or add a comment to an existing issue");
        EditHandlerDetailsPage handlerPage = serverPage.setServiceName("test").next();
        assertEquals(ImmutableList.of("Project", "Issue Type", "Strip Quotes", "Catch Email Address",
                "Bulk", "Forward Email", "Create Users", "Default Reporter", "Notify Users", "CC Assignee", "CC Watchers"),
                handlerPage.getFieldNames());

        assertEquals(ImmutableList.of("Ignore the email and do nothing", "Forward the email", "Delete the email permanently",
                "Accept the email for processing"), handlerPage.getTextFromOptions(handlerPage.getSelectOptions("bulk")));

        serverPage = jira.visit(IncomingServersPage.class).addHandler();
        serverPage.setSelect("handler", "Create a new issue from each email message");
        handlerPage = serverPage.setServiceName("test").next();
        assertEquals(ImmutableList.of("Project", "Issue Type", "Catch Email Address", "Bulk", "Forward Email", "Create Users",
                "Default Reporter", "Notify Users", "CC Assignee", "CC Watchers"),
                handlerPage.getFieldNames());

        serverPage = jira.visit(IncomingServersPage.class).addHandler();
        serverPage.setSelect("handler", "Add a comment from the non quoted email body");
        handlerPage = serverPage.setServiceName("test").next();
        assertEquals(ImmutableList.of("Catch Email Address", "Bulk", "Forward Email",
                "Create Users", "Default Reporter","Notify Users"),
                handlerPage.getFieldNames());

        serverPage = jira.visit(IncomingServersPage.class).addHandler();
        serverPage.setSelect("handler", "Add a comment with the entire email body");
        handlerPage = serverPage.setServiceName("test").next();
        assertEquals(ImmutableList.of("Catch Email Address", "Bulk", "Forward Email", "Create Users",
                "Default Reporter", "Notify Users"),
                handlerPage.getFieldNames());

        serverPage = jira.visit(IncomingServersPage.class).addHandler();
        serverPage.setSelect("handler", "Add a comment before a specified marker or separator in the email body");
        handlerPage = serverPage.setServiceName("test").next();
        assertEquals(ImmutableList.of("Split Regex",
                "Catch Email Address", "Bulk", "Forward Email", "Create Users", "Default Reporter", "Notify Users"),
                handlerPage.getFieldNames());
        final String myregexp = "myregexp";
        final IncomingServersPage incomingServersPage = handlerPage.setSplitRegexp(myregexp).add();
        String handlerId = incomingServersPage.getHandlerId("test");
        assertEquals(myregexp, incomingServersPage.editHandler(handlerId).next().getSplitRegexp());
    }
}
