package it.com.atlassian.jira.tzdetect.pageobject;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;

/**
 * Page object for the client-side timezone detection banner.
 */
public class TzDetectBanner
{
    @ElementBy (id = "timezoneDiffBanner")
    private PageElement banner;

    public TzDetectBanner()
    {
    }

    @WaitUntil
    public void waitUntil()
    {
        Poller.waitUntilTrue(banner.timed().isPresent());
    }
}
