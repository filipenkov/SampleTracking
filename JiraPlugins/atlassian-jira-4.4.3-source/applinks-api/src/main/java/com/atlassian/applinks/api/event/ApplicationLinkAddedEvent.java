package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * This event is broadcast after an application link is added and the new primary optionally set.
 *
 * @since 3.0
 */
public class ApplicationLinkAddedEvent extends ApplicationLinkEvent
{
    public ApplicationLinkAddedEvent(final ApplicationLink applicationLink)
    {
        super(applicationLink);
    }

}