package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.hamcrest.Matchers;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.forMatcher;

/**
 * Displayed to the user when the restore data process has been completed.
 *
 * @since v4.4
 */
public class RestoreCompleted extends AbstractJiraPage
{

    @ElementBy(id = "login")
    private PageElement logInLink;

    @ElementBy(id = "importresult")
    private PageElement importResult;

    @Override
    public TimedCondition isAt()
    {
        return and(importResult.timed().isPresent(),
                forMatcher(importResult.timed().getValue(), Matchers.equalTo("success")));
    }

    @Override
    public String getUrl()
    {
        return "/secure/ImportResult.jspa";
    }

    public DashboardPage followLoginLink()
    {
        logInLink.click();
        return pageBinder.bind(DashboardPage.class);
    }
}
