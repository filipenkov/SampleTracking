/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.setup.AdminSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.ApplicationSetupPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import pageobjects.AddSmtpMailServerDuringSetupPage;
import pageobjects.ApplicationSetupByUrlPage;
import pageobjects.OutgoingServersPage;
import pageobjects.UpdateSmtpMailServerPage;

import java.io.PrintWriter;
import java.io.StringWriter;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.SETUP})
public class TestAddSmtpMailServerDuringSetup extends BaseJiraWebTest
{
    @Rule
    public SetupTestWatchman watchman = new SetupTestWatchman();

    @Test
    public void testAddingSmtpServerDuringSetup() throws ConfigurationException {
        AddSmtpMailServerDuringSetupPage smtpSetup = goToMailSetup();

        // expected defaults
        Assert.assertEquals("[JIRA]", smtpSetup.getEmailPrefix());
        Assert.assertTrue(smtpSetup.isSmtpServerType());
        Assert.assertFalse(smtpSetup.isTlsRequired());
        Assert.assertEquals("10000", smtpSetup.getTimeout());

        TestAddSmtpMailServer.verifyKnownProvidersData(smtpSetup);
        // yes, it's bug
        smtpSetup.setHostName("");

        // verify JNDI behaviour
        smtpSetup.setJndiServerType();
        verifyJndiLayout(smtpSetup);

        smtpSetup.setJndiLocation("invalid.location");
        smtpSetup = smtpSetup.finishWithError();

        Assert.assertTrue(smtpSetup.hasFromError());
        Assert.assertEquals("You must specify a valid email address to send notifications from", smtpSetup.getFromError());
        Assert.assertEquals("Could not find any object at the location specified", smtpSetup.getJndiError());
        verifyJndiLayout(smtpSetup);
        smtpSetup.setJndiLocation("");

        smtpSetup.setSmtpServerType();
        verifySmtpLayout(smtpSetup);
        smtpSetup.setServiceProvider("gmail-smtp");

        PropertiesConfiguration properties = new PropertiesConfiguration("it.properties");

        smtpSetup.setFrom(properties.getString("gmail.from"));
        smtpSetup.setUsername(properties.getString("gmail.username"));
        smtpSetup.setPassword(properties.getString("gmail.password"));
        smtpSetup = smtpSetup.test();
        Assert.assertTrue(smtpSetup.testSucceeded());

        smtpSetup.setFrom("");
        smtpSetup.setTimeout("123456");
        smtpSetup.setUsername("testUsername");
        smtpSetup.setPassword("testPassword");
        smtpSetup.setEmailPrefix("[NEWPREFIX]");
        smtpSetup = smtpSetup.finishWithError();

        Assert.assertTrue(smtpSetup.hasFromError());
        Assert.assertEquals("You must specify a valid email address to send notifications from", smtpSetup.getFromError());

        Assert.assertEquals("Finish (with errors) lost the service provider setting", "gmail-smtp", smtpSetup.getServiceProvider());

        Assert.assertEquals("[NEWPREFIX]", smtpSetup.getEmailPrefix());
        Assert.assertEquals("smtp.gmail.com", smtpSetup.getHostName());
        Assert.assertEquals("smtps", smtpSetup.getProtocol());
        Assert.assertEquals("465", smtpSetup.getPort());
        Assert.assertEquals("123456", smtpSetup.getTimeout());
        Assert.assertEquals("testUsername", smtpSetup.getUsername());
        Assert.assertEquals("testPassword", smtpSetup.getPassword());

        // let's move on  with local data
        smtpSetup.setFrom("admin@stuff.com.com");
        smtpSetup.setServiceProvider("custom");
        smtpSetup.setHostName("127.0.0.123"); // non obvious localhost
        smtpSetup.setPort("123");
        smtpSetup.setTlsRequired(true);

        smtpSetup.finish();
        watchman.disarm();

        final OutgoingServersPage outgoingServersPage = jira.gotoLoginPage().loginAsSysAdmin(OutgoingServersPage.class);
        final UpdateSmtpMailServerPage serverEdit = outgoingServersPage.editServer();
        
        Assert.assertEquals("[NEWPREFIX]", serverEdit.getEmailPrefix());
        Assert.assertEquals("127.0.0.123", serverEdit.getHostName());
        Assert.assertEquals("smtps", serverEdit.getProtocol());
        Assert.assertEquals("123", serverEdit.getPort());
        Assert.assertEquals("123456", serverEdit.getTimeout());
        Assert.assertEquals("testUsername", serverEdit.getUsername());
    }

    private void verifyJndiLayout(AddSmtpMailServerDuringSetupPage smtpSetup)
    {
        Assert.assertTrue(smtpSetup.isJndiServerType());
        Assert.assertFalse(smtpSetup.isSmtpServerType());
        Assert.assertTrue(smtpSetup.isSmtpPartHidden());
        Assert.assertTrue(smtpSetup.isJndiLocationVisible());
    }

    private void verifySmtpLayout(AddSmtpMailServerDuringSetupPage smtpSetup)
    {
        Assert.assertFalse(smtpSetup.isJndiServerType());
        Assert.assertTrue(smtpSetup.isSmtpServerType());
        Assert.assertFalse(smtpSetup.isSmtpPartHidden());
        Assert.assertFalse(smtpSetup.isJndiLocationVisible());
    }

    private AddSmtpMailServerDuringSetupPage goToMailSetup()
    {
        backdoor.restoreData("TestEmpty.xml");

        final ApplicationSetupPage step2 = jira.visit(ApplicationSetupByUrlPage.class);
        final AdminSetupPage step3 = step2.setTitle("Testing JIRA")
                .setLicense(LicenseKeys.V2_COMMERCIAL.getLicenseString())
                .submit();
        step3.setUsername("admin")
                .setPasswordAndConfirmation("admin")
                .setFullName("Administrator")
                .setEmail("admin@stuff.com.com")
                .submit();
        AddSmtpMailServerDuringSetupPage smtpSetup = pageBinder.bind(AddSmtpMailServerDuringSetupPage.class);
        smtpSetup.setEmailNotifications(true);
        return smtpSetup;
    }

    private class SetupTestWatchman extends TestWatchman
    {
        private boolean disarmed;

        void disarm() {
            disarmed = true;
        }

        @Override
        public void starting(FrameworkMethod method)
        {
            disarmed = false;
        }

        @Override
        public void failed(Throwable e, FrameworkMethod method)
        {
            if (!disarmed) {
                FuncTestOut.log(1, "[WARN] This test leaves tested JIRA in flux when it fails. Expect following tests to fail during setup phase. Attempting to recover.");
                FuncTestOut.log(1, "[WARN] Original failure: " + e.getMessage());
                final StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer, true));
                FuncTestOut.log(2, writer.toString());

                try {
                    final ApplicationSetupByUrlPage step2 = jira.visit(ApplicationSetupByUrlPage.class);
                    step2.setLicense(LicenseKeys.V2_COMMERCIAL.getLicenseString()).setTitle("Instance Recovered after TestAddSmtpMailServerDuringSetup");
                    step2.submitToStep4().submitDisabledEmail();
                    FuncTestOut.log(1, "[WARN] Apparently recover succeeded.");
                } catch (Exception ex) {
                    FuncTestOut.log(1, "[ERROR] Recovery failed: " + e.getMessage());
                    final StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw, true));
                    FuncTestOut.log(2, sw.toString());
                }
            }
        }
    }
}
