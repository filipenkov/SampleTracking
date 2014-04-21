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
import pageobjects.AddSmtpMailServerPage;
import pageobjects.OutgoingServersPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS })
public class TestAddSmtpMailServer extends BaseJiraWebTest
{
	@Before
	public void restore() {
		backdoor.restoreData("noServers.zip");
	}

    @Test
    public void testTestButtonDoesntClearServiceProviders() {
        OutgoingServersPage serversPage = jira.gotoLoginPage().loginAsSysAdmin(OutgoingServersPage.class);
        AddSmtpMailServerPage addPage = serversPage.addServer();

        verifyKnownProvidersData(addPage);

        addPage.setFrom("pniewiadomski@atlassian.com").setEmailPrefix("[Test]")
                .setName("SMTP").setUsername("jiratest2@spartez.com").setPassword("Jd0n0tch@nge");

        addPage.setSelectByValue("serviceProvider", "gmail-smtp");

        addPage = addPage.test();

        assertTrue(addPage.testSucceeded());
        assertEquals(ImmutableList.of("Custom", "Google Apps Mail / Gmail", "Yahoo! Mail Plus"),
                addPage.getTextFromOptions(addPage.getSelectOptions("serviceProvider")));
    }

    static void verifyKnownProvidersData(AbstractEditMailServerPage smtpSetup) {
        assertEquals(ImmutableList.of("Custom", "Google Apps Mail / Gmail", "Yahoo! Mail Plus"),
                smtpSetup.getTextFromOptions(smtpSetup.getSelectOptions("serviceProvider")));

        smtpSetup.setServiceProviderByName("Google Apps Mail / Gmail");
        assertEquals("smtp.gmail.com", smtpSetup.getHostName());
        assertEquals("465", smtpSetup.getPort());
        assertEquals("smtps", smtpSetup.getProtocol());

        smtpSetup.setServiceProviderByName("Yahoo! Mail Plus");
        assertEquals("plus.smtp.mail.yahoo.com", smtpSetup.getHostName());
        assertEquals("465", smtpSetup.getPort());
        assertEquals("smtps", smtpSetup.getProtocol());

        smtpSetup.setServiceProviderByName("Custom");
    }
}
