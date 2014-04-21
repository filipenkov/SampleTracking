package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;

/**
 * Base class for events emitted when an {@link ApplicationLink} is modified.
 *
 * @since 3.0
 */
public abstract class ApplicationLinkEvent implements LinkEvent
{
    protected final ApplicationLink applicationLink;

    protected ApplicationLinkEvent(final ApplicationLink applicationLink)
    {
        this.applicationLink = applicationLink;
    }

    /**
     * @return the globally unique, immutable ID of the server at the other
     *         end of this link.
     */
    public ApplicationId getApplicationId()
    {
        return applicationLink.getId();
    }

    /**
     * @return The {@link ApplicationLink} that is the subject of this event
     */
    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }

    /**
     * @return the type of the application e.g. "fecru"
     */
    public ApplicationType getApplicationType()
    {
        return applicationLink.getType();
    }

}
