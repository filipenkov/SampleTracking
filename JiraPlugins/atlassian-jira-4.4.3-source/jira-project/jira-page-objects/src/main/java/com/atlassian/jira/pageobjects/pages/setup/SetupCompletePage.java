package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static org.hamcrest.Matchers.equalTo;

/**
 * Page with information about completed JIRA setup.
 *
 * @since v4.4
 */
public class SetupCompletePage extends AbstractJiraPage
{
    // TODO we might have common annotations for various well known elements and postprocessor to inject them
    @ElementBy(cssSelector = "#jira-message-container h1")
    private PageElement title;


    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Can't get here via URI");
    }

    @Override
    public TimedCondition isAt()
    {
        return formTitlePresent();
    }

    private TimedCondition formTitlePresent()
    {
        return Conditions.forMatcher(title.timed().getText(), equalTo("Setup Complete"));
    }

    public JiraLoginPage goToLogin()
    {
        // TODO: link has to have ID
        return null;
    }

}
