package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Step 4 in the JIRA setup process - mail setup.
 *
 * @since v4.4
 */
public class MailSetupPage extends AbstractJiraPage
{
    private static final String URI = "/secure/Setup2.jspa";

    @ElementBy(id = "jira-setupwizard-email-notifications-disabled")
    private PageElement disabledEmailOption;

    @ElementBy (id = "jira-setupwizard-submit")
    private PageElement submitButton;

    @Override
    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return disabledEmailOption.timed().isPresent();
    }

    public DashboardPage submitDisabledEmail()
    {
        disabledEmailOption.click();
        submitButton.click();
        // During SetupComplete, the user is automatically logged in, and are redirected to the base url, i.e. the Dashboard
        return pageBinder.bind(DashboardPage.class);
    }

}
