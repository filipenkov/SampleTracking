package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * This event is broadcast after an application link's details (e.g., name or display URL)
 * have changed.
 *
 * @since 3.2
 */
public class ApplicationLinkDetailsChangedEvent extends ApplicationLinkEvent
{
    public ApplicationLinkDetailsChangedEvent(final ApplicationLink applicationLink)
    {
        super(applicationLink);
    }

}