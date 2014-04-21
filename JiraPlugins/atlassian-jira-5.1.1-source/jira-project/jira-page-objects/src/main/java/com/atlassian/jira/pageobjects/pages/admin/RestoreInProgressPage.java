package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Displayed to the user when data restore process is in progress.
 *
 * @since v4.4
 */
public class RestoreInProgressPage extends AbstractJiraPage
{
    @ElementBy(id = "refresh_submit")
    private PageElement refreshButton;

    /**
     * This isn't really on the restore in progress page, but
     * can be used to detect if we've already hit the restore completed page
     */
    @ElementBy(id = "login")
    private PageElement logInLink;

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("No URL for this page :P");
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.or(
                refreshButton.timed().isPresent(),
                logInLink.timed().isPresent()
        );
    }

    public RestoreInProgressPage submitRefresh()
    {
        if(logInLink.isPresent())
        {
            throw new IllegalStateException("The restore has been completed, "
                    + "and we are no longer on the in progress page.");
        }
        refreshButton.click();
        return this;
    }

    public RestoreCompleted waitForRestoreCompleted()
    {
        DelayedBinder<RestoreCompleted> delayed = pageBinder.delayedBind(RestoreCompleted.class);
        while (!delayed.canBind())
        {
            waitForAWhile();
        }
        return delayed.bind();
    }

    private void waitForAWhile()
    {
        try
        {
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
