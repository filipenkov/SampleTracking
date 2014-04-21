package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * This event is broadcast after the authentication configuration of an application link has changed.
 *
 * @since 3.2
 */
public class ApplicationLinkAuthConfigChangedEvent extends ApplicationLinkEvent
{
    public ApplicationLinkAuthConfigChangedEvent(final ApplicationLink applicationLink)
    {
        super(applicationLink);
    }
}