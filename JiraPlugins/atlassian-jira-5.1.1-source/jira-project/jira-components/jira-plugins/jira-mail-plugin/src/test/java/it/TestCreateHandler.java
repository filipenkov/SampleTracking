/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pageobjects.EditHandlerDetailsPage;
import pageobjects.EditServerDetailsPage;
import pageobjects.EditServicePage;
import pageobjects.IncomingServersPage;
import pageobjects.ViewServicesPage;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS })
public class TestCreateHandler extends BaseJiraWebTest
{
    @Before
    public void ensureLoggedIn() {
        if (!jira.gotoHomePage().isAdmin()) {
            jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        }
    }

	@Before
	public void restore() {
		backdoor.restoreData("noServers.zip");
	}

	@Test
    public void testCreateEditDeleteService() {
        backdoor.project().addProject("Test project", "TEST", "admin");
        backdoor.project().addProject("Another test project", "AAA", "admin");

        testNoIncomingServersMessageIsNotPresentWhenSysadmin();
        testCreateServerAndHandler();
        testSmallcaseProjectKey();
        testMessageHandlersOrder();

        testHandlerEdit();

        testObsoleteParamsWarnings();
        testDeleteService();
    }

    private void testSmallcaseProjectKey() {
        IncomingServersPage serversPage = jira.visit(IncomingServersPage.class);
        String id = serversPage.addHandler().setMailServerByName("IMAP Server")
                .setServiceName("Smallcase").next().setProjectByKey("TEST").add().getHandlerId("Smallcase");

        serversPage = jira.visit(IncomingServersPage.class);

        ViewServicesPage servicesPage = jira.visit(ViewServicesPage.class);
        EditServicePage editServicePage = servicesPage.editServiceHack(id);
        editServicePage.setHandlerParams(editServicePage.getHandlerParams().replace("project=TEST", "project=test"));
        editServicePage.update();

        serversPage = jira.visit(IncomingServersPage.class);
        Assert.assertEquals("TEST", serversPage.editHandler(id).next().getProjectKey());
    }

    private void testHandlerEdit()
    {
        backdoor.mailServers().addPopServer("POP Server", 1);
        IncomingServersPage serversPage = jira.visit(IncomingServersPage.class);

        String id = serversPage.getHandlerId("Test");
        assertEquals("Strip Quotes: false Forward Email: pniewiadomski@atlassian.com Create Users: false Notify Users: true CC Assignee: "
                + CreateIssueHandler.DEFAULT_CC_ASSIGNEE
                + " CC Watchers: false",
                serversPage.getHandlerParams(id));

        final EditServerDetailsPage editServerDetailsPage = serversPage.editHandler(id);
        Assert.assertEquals("IMAP Server", editServerDetailsPage.getMailServerName());
        Assert.assertTrue(editServerDetailsPage.isFolderVisible());
        Assert.assertEquals("NotInbox", editServerDetailsPage.getFolder());
        editServerDetailsPage
                .setServiceName("Changed Service Name")
                .setMailServerByName("POP Server")
                .setDelay("2");
        Assert.assertFalse(editServerDetailsPage.isFolderVisible());

        final EditHandlerDetailsPage editHandlerDetailsPage = editServerDetailsPage.next();
        editHandlerDetailsPage
                .setProjectByKey("AAA")
                .setIssueTypeByName("New Feature")
                .setReporterusername("wrong")
                .setCatchemail("improper")
                .setForwardEmail("improper too")
                .setStripQuotes(true)
                .setNotifyUsers(false);

        assertNotNull(editHandlerDetailsPage.getReporterError());
        assertTrue(StringUtils.contains(editHandlerDetailsPage.getReporterError(), "does not exist"));
        assertNotNull(editHandlerDetailsPage.getCatchEmailError());
        assertNotNull(editHandlerDetailsPage.getForwardEmailError());

        editHandlerDetailsPage
                .setReporterusername("admin")
                .setCatchemail("catch@email.test")
                .setCreateUsers(true)
                .setForwardEmail("forward@email.test");

        assertFalse(editHandlerDetailsPage.hasCatchEmailError());
        assertFalse(editHandlerDetailsPage.hasForwardEmailError());
        assertFalse(editHandlerDetailsPage.isReporterPresent());

        editHandlerDetailsPage.setCreateUsers(false);
        Assert.assertFalse(editHandlerDetailsPage.hasReporterError());
        assertTrue(editHandlerDetailsPage.isReporterPresent());

        editHandlerDetailsPage.add();

        String newId = serversPage.getHandlerId("Changed Service Name");
        assertEquals("Strip Quotes: true Default Reporter: admin Catch Email Address: catch@email.test Forward Email: forward@email.test Create Users: false Notify Users: false CC Assignee: "
                + CreateIssueHandler.DEFAULT_CC_ASSIGNEE
                + " CC Watchers: false",
                serversPage.getHandlerParams(newId));

        final EditServerDetailsPage edit2 = serversPage.editHandler(newId);
        assertEquals("Changed Service Name", edit2.getServiceName());
        Assert.assertEquals("POP Server", edit2.getMailServerName());
        Assert.assertEquals("2", edit2.getDelay());

        edit2.setServiceName("Test");

        edit2.next()
                .setReporterusername("")
                .setCreateUsers(true)
                .add();

        String lastId = serversPage.getHandlerId("Test");
        assertEquals("Strip Quotes: true Catch Email Address: catch@email.test Forward Email: forward@email.test Create Users: true Notify Users: false CC Assignee: "
                + CreateIssueHandler.DEFAULT_CC_ASSIGNEE
                + " CC Watchers: false",
                serversPage.getHandlerParams(lastId));
    }

    private void testObsoleteParamsWarnings()
    {
        IncomingServersPage serversPage = jira.visit(IncomingServersPage.class);
        assertFalse(serversPage.hasWarningAboutObsoleteSettings());
        String id1 = serversPage.getHandlerId("Test");
        // test for https://jdog.atlassian.com/browse/JRADEV-7729
        ViewServicesPage servicesPage = jira.visit(ViewServicesPage.class);
        assertFalse(servicesPage.hasWarningAboutObsoleteSettings());
        assertFalse(servicesPage.hasWarningAboutObsoleteSettings(id1));

        EditServicePage editPage = servicesPage.editServiceHack(id1);
        servicesPage = editPage.setHandlerParams(editPage.getHandlerParams() + ",port=34").update();
        assertTrue(servicesPage.hasWarningAboutObsoleteSettings());
        assertTrue(servicesPage.hasWarningAboutObsoleteSettings(id1));

        serversPage = jira.visit(IncomingServersPage.class);
        assertTrue(serversPage.hasWarningAboutObsoleteSettings());
        assertTrue(serversPage.hasWarningAboutObsoleteSettings(id1));

        serversPage.editHandler(id1).next().add();
        assertFalse(serversPage.hasWarningAboutObsoleteSettings());
        assertFalse(serversPage.hasWarningAboutObsoleteSettings(id1));

    }

    private void testCreateServerAndHandler()
    {
        backdoor.mailServers().addImapServer("IMAP Server", 1);
        final IncomingServersPage incomingServersPage = jira.visit(IncomingServersPage.class);

        Assert.assertFalse(incomingServersPage.hasNoServersHint());
        Assert.assertTrue(incomingServersPage.isAddHandlerEnabled());

        EditServerDetailsPage serverPage = incomingServersPage.addHandler();
        Assert.assertTrue(serverPage.isFolderVisible());
        serverPage.setFolder("NotInbox");

        final EditServerDetailsPage editServerDetailsPage = serverPage
                .setServiceName("Test")
                .setMailServerByName("IMAP Server")
                .setHandlerByKey("com.atlassian.jira.jira-mail-plugin:createOrCommentHandler");
        final EditHandlerDetailsPage handlerPage = editServerDetailsPage.next()
                .setProjectByKey("TEST")
                .setIssueTypeByName("Task")
                .setForwardEmail("pniewiadomski@atlassian.com");

        IncomingServersPage serversPage = handlerPage.add();

        String id = serversPage.getHandlerId("Test");
        assertEquals("IMAP Server\nlocalhost", serversPage.getHandlerServer(id));

        assertEquals("Strip Quotes: false Forward Email: pniewiadomski@atlassian.com Create Users: false Notify Users: true CC Assignee: "
                + CreateIssueHandler.DEFAULT_CC_ASSIGNEE
                + " CC Watchers: false",
                serversPage.getHandlerParams(id));
    }

    private void testNoIncomingServersMessageIsNotPresentWhenSysadmin()
    {
        IncomingServersPage incomingServersPage = jira.visit(IncomingServersPage.class);
        Assert.assertFalse(incomingServersPage.hasNoServersHint());
        Assert.assertTrue(incomingServersPage.isAddHandlerEnabled());
    }

    private void testDeleteService() {
        IncomingServersPage serversPage = jira.visit(IncomingServersPage.class);

        String id = serversPage.getHandlerId("Test");
        serversPage = serversPage.deleteHandler(id).cancel();

        assertTrue(serversPage.isHandlerPresent("Test"));
        serversPage = serversPage.deleteHandler(id).delete();
        assertFalse(serversPage.isHandlerPresent("Test"));
    }

    /**
     * Test case for https://jdog.atlassian.com/browse/JRADEV-8180
     */
    private void testMessageHandlersOrder() {
        IncomingServersPage serversPage = jira.visit(IncomingServersPage.class);
        EditServerDetailsPage editPage = serversPage.editHandler(serversPage.getHandlerId("Test"));
        assertEquals(ImmutableList.of(
                "Create a new issue or add a comment to an existing issue",
                "Add a comment from the non quoted email body",
                "Add a comment with the entire email body",
                "Create a new issue from each email message",
                "Add a comment before a specified marker or separator in the email body"), editPage.getMessageHandlerNames());
    }
}
