package com.atlassian.applinks.core.link;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.spi.link.MutableApplicationLink;

public interface InternalApplicationLink extends MutableApplicationLink
{
    /**
     * Sets the primary flag of the {@link ApplicationLink} to true.
     *
     * NOTE: it does not mutate other stored {@link ApplicationLink}s. Setting multiple
     * {@link ApplicationLink}s as 'primary' will leave the application in an inconsistent
     * state.
     *
     * @param isPrimary the new value of the primary flag. 
     */
    void setPrimaryFlag(boolean isPrimary);
}
