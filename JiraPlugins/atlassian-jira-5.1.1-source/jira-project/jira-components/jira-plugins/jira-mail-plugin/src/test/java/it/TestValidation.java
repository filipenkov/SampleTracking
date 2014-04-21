/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */

package it;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pageobjects.AbstractEditMailServerPage;
import pageobjects.EditHandlerDetailsPage;
import pageobjects.EditServerDetailsPage;
import pageobjects.IncomingServersPage;
import pageobjects.OutgoingServersPage;
import pageobjects.UpdatePopMailServerPage;
import pageobjects.UpdateSmtpMailServerPage;

import static org.junit.Assert.assertEquals;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS })
public class TestValidation extends BaseJiraWebTest
{
	@Before
	public void restore() {
		backdoor.restoreData("blank.zip");
	}

	/**
     * Create or comment handler and Create Issue handler require at least one project in JIRA
     */
    @Test
    public void validateProjectRequired() {
        EditServerDetailsPage serverPage = jira.gotoLoginPage().loginAsSysAdmin(EditServerDetailsPage.class);
        serverPage.setServiceName("Test1");
        serverPage.setSelect("handler", "Create a new issue or add a comment to an existing issue");
        serverPage = serverPage.nextWithErrors();
        assertEquals(ImmutableList.of("To use this handler you need to have at least one project defined in JIRA."), serverPage.getFieldErrors());

        serverPage.setSelect("handler", "Create a new issue from each email message");
        serverPage = serverPage.nextWithErrors();
        assertEquals(ImmutableList.of("To use this handler you need to have at least one project defined in JIRA."), serverPage.getFieldErrors());
    }

    @Test
    public void validateEditServerDetailsFields() {
        EditServerDetailsPage serverPage = jira.gotoLoginPage().loginAsSysAdmin(EditServerDetailsPage.class);
        serverPage = serverPage.nextWithErrors();
        assertEquals(ImmutableList.of("Please specify a name for this service.",
                "To use this handler you need to have at least one project defined in JIRA."), serverPage.getFieldErrors());

        serverPage.setServiceName("Test2");
        serverPage.setDelay("");
        serverPage = serverPage.nextWithErrors();
        assertEquals(ImmutableList.of("Must be a number",
                "To use this handler you need to have at least one project defined in JIRA."), serverPage.getFieldErrors());
    }

    @Test
    public void validateEditHandlerDetailsFields() {

        EditServerDetailsPage serverPage = jira.gotoLoginPage().loginAsSysAdmin(EditServerDetailsPage.class);
        EditHandlerDetailsPage handlerPage = serverPage.setServiceName("Test3")
                .setHandlerByKey("com.atlassian.jira.jira-mail-plugin:nonQuotedCommentHandler").next();
        handlerPage.setSelectByValue("bulk", "ignore");

        handlerPage.setCatchemail("test");
        handlerPage = handlerPage.addWithErrors();
        assertEquals(ImmutableList.of("Invalid email address format."), handlerPage.getFieldErrors());

        handlerPage.setCatchemail("");
        handlerPage.setReporterusername("nosuchuser");
        handlerPage = handlerPage.addWithErrors();
        assertEquals(ImmutableList.of("This user does not exist please select a user from the user browser."), handlerPage.getFieldErrors());

        handlerPage.setReporterusername("");
        handlerPage.setForwardEmail("test");
        handlerPage = handlerPage.addWithErrors();
        assertEquals(ImmutableList.of("Invalid email address format."), serverPage.getFieldErrors());
    }

    @Test
    public void testValidatePasswordRequiredWhenSMTPUsernameChanged() throws Exception
    {
        backdoor.mailServers().addSmtpServer(1);

        final OutgoingServersPage outgoingServersPage = jira.gotoLoginPage().loginAsSysAdmin(OutgoingServersPage.class);
        UpdateSmtpMailServerPage updatePage = outgoingServersPage.editServer();
        verifyPasswordAppearsAfterUsernameChanged(updatePage, "new username");

        updatePage = updatePage.update().editServer();
        Assert.assertEquals("new username", updatePage.getUsername());
        verifyPasswordAppearsAfterUsernameChanged(updatePage, "newer username");
    }

    @Test
    public void testValidatePasswordRequiredWhenPOPUsernameChanged() throws Exception
    {
        final String serverName = "POP Server for Password Required After Username Change Test";
        backdoor.mailServers().addPopServer(serverName, "", "pop3", "localhost", 1, "", "");

        final IncomingServersPage incomingServersPage = jira.gotoLoginPage().loginAsSysAdmin(IncomingServersPage.class);
        final String id = incomingServersPage.getServerId(serverName);

        UpdatePopMailServerPage updatePage = incomingServersPage.editServer(id);
        verifyPasswordAppearsAfterUsernameChanged(updatePage, "new username");

        updatePage = updatePage.update().editServer(id);
        Assert.assertEquals("new username", updatePage.getUsername());
        verifyPasswordAppearsAfterUsernameChanged(updatePage, "newer username");
    }

    private void verifyPasswordAppearsAfterUsernameChanged(AbstractEditMailServerPage updatePage, String username)
    {
        Assert.assertTrue(updatePage.isChangePasswordVisible());
        Assert.assertTrue(updatePage.isChangePasswordEnabled());
        Assert.assertFalse(updatePage.isChangePasswordDescriptionVisible());
        Assert.assertFalse(updatePage.isPasswordVisible());
        Assert.assertFalse(updatePage.isChangePassword());

        updatePage.setUsername(username);

        Assert.assertTrue(updatePage.isChangePasswordVisible());
        Assert.assertFalse(updatePage.isChangePasswordEnabled());
        Assert.assertEquals("You need to provide the new password when changing the username.", updatePage.getChangePasswordDescription());
        Assert.assertTrue(updatePage.isChangePasswordDescriptionVisible());
        Assert.assertTrue(updatePage.isPasswordVisible());
        Assert.assertTrue(updatePage.isChangePassword());
        updatePage.setPassword("pass");
    }
}
