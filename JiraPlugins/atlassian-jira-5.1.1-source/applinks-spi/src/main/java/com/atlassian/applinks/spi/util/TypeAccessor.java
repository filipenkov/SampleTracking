package com.atlassian.applinks.spi.util;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;

/**
 * Provides access to singleton instances of {@link EntityType} and {@link ApplicationType} and registered
 * {@link AuthenticationProvider} classes.
 *
 * @since 3.0
 */
public interface TypeAccessor
{

    /**
     * @param typeClass the interface or superclass (extending {@link EntityType} of the type to retrieve.
     * Generally this will be a class from a sub-package of {@link com.atlassian.applinks.api.application}
     * @param <T> the type to the typeClass parameter
     * @return an instance of the specified {@link EntityType}, or null if an implementation of the specified
     * {@link EntityType} is not registered via the plugin system.
     */
    <T extends EntityType> T getEntityType(Class<T> typeClass);

    /**
     * @param typeClass the interface or superclass (extending {@link ApplicationType} of the type to retrieve.
     * Generally this will be a class from {@link com.atlassian.applinks.api.application}
     * @param <T> the type to the typeClass parameter
     * @return an instance of the specified {@link ApplicationType}, or null if an implementation of the specified
     * {@link ApplicationType} is not registered via the plugin system.
     */
    <T extends ApplicationType> T getApplicationType(Class<T> typeClass);

    /**
     * @param className the full class name of a registered {@link AuthenticationProvider} (must be equal to the result
     * of a call to {@link Class#getName()}).
     * @return the {@link Class} of the specified {@link AuthenticationProvider}
     */
    Class<? extends AuthenticationProvider> getAuthenticationProviderClass(String className);

    /**
     * @return a collection of all enabled {@link EntityType} instances registered via the plugin system
     */
    Iterable<? extends EntityType> getEnabledEntityTypes();

    /**
     * @return  the collection of {@link com.atlassian.applinks.api.EntityType}s
     * that are supported by the given {@link com.atlassian.applinks.api.ApplicationType}.
     */
    Iterable<? extends EntityType> getEnabledEntityTypesForApplicationType(ApplicationType applicationType);

    /**
     * @return a collection of all enabled {@link ApplicationType} instances registered via the plugin system
     */
    Iterable<? extends ApplicationType> getEnabledApplicationTypes();

}