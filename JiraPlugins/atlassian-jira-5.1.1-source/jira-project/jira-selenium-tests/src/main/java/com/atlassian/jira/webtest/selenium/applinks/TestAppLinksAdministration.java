package com.atlassian.jira.webtest.selenium.applinks;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.page.AdministrationPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.webtest.ui.keys.Sequences.chars;

/**
 * @since v4.3
 */
@WebTest({Category.SELENIUM_TEST })
public class TestAppLinksAdministration extends JiraSeleniumTest
{
    private AdministrationPage administration;

    public void testAddingLinkToSelfShouldNotBePermitted() throws Exception
    {
        NewAppLinkWizard wizard = administration.goTo().goToPage(AppLinksAdminPage.class).clickAddApplicationLink();
        assertThat(wizard.isOpen(), byDefaultTimeout());

        // type into page 1 of the wizard
        assertThat(wizard.step1().isReady(), byDefaultTimeout());
        wizard.step1().serverURL().type(chars(config.getBaseUrl()));
        wizard.clickNext();
        assertThat(wizard.step1().isReady(), byDefaultTimeout());
        assertThat(wizard.hasErrorMessage("This application has the same ID as myself. Can't create link to myself."),
                byDefaultTimeout());

        // cancel out of the dialog
        wizard.clickCancel();
        assertThat(wizard.isClosed(), byDefaultTimeout());
    }

    public void testUsingABogusServerUrlShouldShowWarningOnPage2() throws Exception
    {
        NewAppLinkWizard wizard = administration.goTo().goToPage(AppLinksAdminPage.class).clickAddApplicationLink();
        assertThat(wizard.isOpen(), byDefaultTimeout());

        // type into page 1 of the wizard
        assertThat(wizard.step1().isReady(), byDefaultTimeout());
        wizard.step1().serverURL().type(chars("hohoho"));
        wizard.clickNext();

        // make sure a warning is displayed
        assertThat(wizard.step2().isReady(), byDefaultTimeout());
        assertThat(wizard.hasErrorMessage("The host doesn't respond. Change the URL or click Next to confirm."), byDefaultTimeout());

        // cancel out of the dialog
        wizard.clickCancel();
        assertThat(wizard.isClosed(), byDefaultTimeout());
    }

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreBlankInstance();
        administration = globalPages().goToAdministration();
    }
}
