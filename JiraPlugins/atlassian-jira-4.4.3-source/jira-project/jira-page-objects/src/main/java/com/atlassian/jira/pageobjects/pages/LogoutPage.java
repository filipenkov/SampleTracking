package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static com.atlassian.pageobjects.elements.query.Conditions.or;

/**
 * Page object implementation for the Logout page in JIRA.
 *
 * @since 4.4
 */
public class LogoutPage extends AbstractJiraPage
{
    private static final String URI = "/secure/Logout.jspa";

    @ElementBy (id = "logged-out")
    private PageElement loggedOutElement;

    @ElementBy (id = "already-logged-out")
    private PageElement alreadyLoggedOutElement;

    @ElementBy (id = "confirm-logout-submit")
    private PageElement confirmElement;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return or(loggedOutElement.timed().isPresent(), alreadyLoggedOutElement.timed().isPresent(),
                confirmElement.timed().isPresent());
    }

    public LogoutPage confirmLogout()
    {
        if (!doLogout())
        {
            throw new IllegalStateException("Already logged out. Not at the confirm logout page.");
        }
        else
        {
            return pageBinder.bind(LogoutPage.class);
        }
    }

    public LogoutPage logout()
    {
        doLogout();
        return pageBinder.bind(LogoutPage.class);
    }

    private boolean doLogout()
    {
        if (confirmElement.isPresent())
        {
            confirmElement.click();
            return true;
        }
        else
        {
            return false;
        }
    }

}