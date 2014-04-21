package com.atlassian.applinks.api.event;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;

/**
 * This event is emitted by the even system when an Application Link's unique
 * ID changed. This can happen when a linked application is relocated, or
 * upgraded.
 *
 * @since   3.0
 */
public class ApplicationLinksIDChangedEvent extends ApplicationLinkEvent
{
    private final ApplicationId oldApplicationId;

    public ApplicationLinksIDChangedEvent(final ApplicationLink applicationLink,
                                        final ApplicationId oldApplicationId)
    {
        super(applicationLink);
        this.oldApplicationId = oldApplicationId;
    }

    /**
     * The {@link com.atlassian.applinks.api.ApplicationId} that this
     * {@link com.atlassian.applinks.api.ApplicationLink} was formerly known
     * as.
     *
     * @return  the old {@link com.atlassian.applinks.api.ApplicationId}.
     */
    public ApplicationId getOldApplicationId()
    {
        return oldApplicationId;
    }
}
