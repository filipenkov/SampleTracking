package com.atlassian.applinks.spi.link;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityType;

/**
 * Factory for providing builders with which to create {@link EntityLink}s
 */
public interface EntityLinkBuilderFactory
{
    EntityLinkBuilder builder();

    public interface EntityLinkBuilder
    {
        EntityLinkBuilder key(String key);
        EntityLinkBuilder type(EntityType type);
        EntityLinkBuilder applicationLink(ApplicationLink applicationLink);
        EntityLinkBuilder primary(boolean primary);
        EntityLinkBuilder name(String name);
        EntityLink build();
    }
}
