package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * This event is broadcast after an application link is deleted and the new primary optionally set.
 *
 * @since 3.0
 */
public class ApplicationLinkDeletedEvent extends ApplicationLinkEvent
{
    public ApplicationLinkDeletedEvent(final ApplicationLink applicationLink)
    {
        super(applicationLink);
    }
}
