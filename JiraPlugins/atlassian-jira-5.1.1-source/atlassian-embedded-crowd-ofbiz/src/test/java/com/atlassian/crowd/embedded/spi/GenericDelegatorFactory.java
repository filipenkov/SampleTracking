package com.atlassian.crowd.embedded.spi;

import org.ofbiz.core.entity.GenericDelegator;

/**
 * Used to construct a GenericDelegator in the Spring context for the SPI integration tests.
 */
public abstract class GenericDelegatorFactory
{
    private GenericDelegatorFactory()
    {
    }

    public static GenericDelegator getGenericDelegator()
    {
        return GenericDelegator.getGenericDelegator("default");
    }
}
