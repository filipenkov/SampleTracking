package com.atlassian.applinks.spi.application;

/**
 * @since 3.0
 */
public interface IdentifiableType
{

    /**
     * @return the {@link TypeId} identifier for this type
     */
    TypeId getId();

}
