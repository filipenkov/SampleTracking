package com.atlassian.jira.pageobjects.framework.util;

import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.framework.util.JiraLocators.body;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Waits until the dirty warning message disappears. If it doesn't, it will fail fast
 * and let developers know they screw up!
 *
 * @since 5.1
 */
public class DirtyWarningTerminator
{

    @Inject
    private PageElementFinder finder;

    @Inject
    private WebDriver webDriver;

    /**
     * Eat that, dirty warning!
     *
     */
    public void htfuDirtyWarnings()
    {
        removeExistingEvil();
        preventTheEvil();
    }

    private void removeExistingEvil()
    {
        try
        {
            webDriver.switchTo().alert().dismiss();
        }
        catch (NoAlertPresentException iDontReallyCare)
        {
        }
    }

    private void preventTheEvil()
    {
        // get out of any IFrame
        webDriver.switchTo().defaultContent();
        // this would work except there is another dirty form handler in play - JRADEV-12044
        waitUntilTrue("Detected active left-over dirty warning. Make sure to eliminate it!",
                finder.find(body()).javascript().executeTimed(Boolean.class, "return !JIRA.DirtyForm.getDirtyWarning();"));
        // just make it WOOOORK
        finder.find(body()).javascript().execute("window.onbeforeunload=null;");
    }


}
