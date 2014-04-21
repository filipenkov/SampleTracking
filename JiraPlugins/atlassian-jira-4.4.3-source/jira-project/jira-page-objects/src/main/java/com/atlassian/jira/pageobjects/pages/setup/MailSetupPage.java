package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
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

    public SetupCompletePage submitDisabledEmail()
    {
        disabledEmailOption.click();
        submitButton.click();
        return pageBinder.bind(SetupCompletePage.class);
    }

}
