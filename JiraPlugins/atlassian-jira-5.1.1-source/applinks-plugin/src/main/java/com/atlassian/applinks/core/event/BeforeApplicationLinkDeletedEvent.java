package com.atlassian.applinks.core.event;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * This event is broad casted BEFORE an application link is deleted.
 * This gives {@link com.atlassian.applinks.api.auth.AuthenticationProvider} the chance to react, if an application link
 * gets deleted and they would like to perform operations (e.g. clean-up other configuration stores) before the application link and its configuration
 * is deleted.
 *
 * @since 3.0
 */
public class BeforeApplicationLinkDeletedEvent
{
    private final ApplicationLink applicationLink;

    public BeforeApplicationLinkDeletedEvent(ApplicationLink applicationLink)
    {
        this.applicationLink = applicationLink;
    }

    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }
}
