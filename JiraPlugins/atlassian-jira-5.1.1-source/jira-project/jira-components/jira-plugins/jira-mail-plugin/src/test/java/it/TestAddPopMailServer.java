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
import pageobjects.AbstractEditMailServerPage;
import pageobjects.AddPopMailServerPage;
import pageobjects.IncomingServersPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS })
public class TestAddPopMailServer extends BaseJiraWebTest
{
	@Before
	public void restore() {
		backdoor.restoreData("noServers.zip");
	}

	@Test
    public void testTestButtonDoesntClearServiceProviders() {
        IncomingServersPage serversPage = jira.gotoLoginPage().loginAsSysAdmin(IncomingServersPage.class);
        AddPopMailServerPage addPage = serversPage.addServer();

        addPage.setName("IMAP")
                .setUsername("jiratest2@spartez.com").setPassword("Jd0n0tch@nge");

        addPage.setSelectByValue("serviceProvider", "gmail-imap");

        addPage = addPage.test();

        assertTrue(addPage.testSucceeded());
        verifyKnownProvidersData(addPage);
    }

    static void verifyKnownProvidersData(AbstractEditMailServerPage popSetup) {
        assertEquals(ImmutableList.of("Custom", "Google Apps Mail / Gmail (POP3)", "Google Apps Mail / Gmail (IMAP)",
                "Yahoo! Mail Plus"),
                popSetup.getTextFromOptions(popSetup.getSelectOptions("serviceProvider")));

        popSetup.setServiceProviderByName("Google Apps Mail / Gmail (POP3)");
        assertEquals("pop.gmail.com", popSetup.getHostName());
        assertEquals("995", popSetup.getPort());
        assertEquals("pop3s", popSetup.getProtocol());

        popSetup.setServiceProviderByName("Google Apps Mail / Gmail (IMAP)");
        assertEquals("imap.gmail.com", popSetup.getHostName());
        assertEquals("993", popSetup.getPort());
        assertEquals("imaps", popSetup.getProtocol());

        popSetup.setServiceProviderByName("Yahoo! Mail Plus");
        assertEquals("plus.pop.mail.yahoo.com", popSetup.getHostName());
        assertEquals("995", popSetup.getPort());
        assertEquals("pop3s", popSetup.getProtocol());

        popSetup.setServiceProviderByName("Custom");
    }
}
