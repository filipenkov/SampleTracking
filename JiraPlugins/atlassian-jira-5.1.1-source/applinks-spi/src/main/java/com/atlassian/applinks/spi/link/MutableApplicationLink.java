package com.atlassian.applinks.spi.link;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * Interface adding mutating behaviour for ApplicationLinks
 *
 * @since 3.0
 */
public interface MutableApplicationLink extends ApplicationLink
{
    /**
     * Update the ApplicationLink's details
     * @param details the details of the {@link ApplicationLink}. Note that all field values will be set to
     * the values of the provided {@link ApplicationLinkDetails} object, including null values.
     * Note that this method has no protection against setting two application links to the same name,
     * which must be avoided.
     */
    void update(ApplicationLinkDetails details);
}
