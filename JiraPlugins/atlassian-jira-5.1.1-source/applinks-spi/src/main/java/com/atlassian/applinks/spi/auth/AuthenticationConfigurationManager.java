package com.atlassian.applinks.spi.auth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.auth.AuthenticationProvider;

import java.util.Map;

/**
 * Acts as a data store for authentication providers.
 *
 * @since   3.0
 */
public interface AuthenticationConfigurationManager
{
    /**
     * @param id the id of the application link for which to
     * test authentication configuration.
     * @param provider   the type of a AppLinks authentication
     * provider (e.g. {@link com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider})
     * @return  {@code true} if the specified authentication provider is
     * configured for use with the specified application link, {@code false}
     * otherwise.
     */
    boolean isConfigured(ApplicationId id, Class<? extends AuthenticationProvider> provider);

    /**
     * Registers configuration for the specified authentication provider with
     * the given application link.
     * <p>
     * If existing configuration for this applink/auth-provider combination
     * already exists, it will be replaced by the new configuration.
     *
     * @param id the id of the application link for which to
     * configure authentication information.
     * @param provider   the type of a AppLinks authentication
     * provider (e.g. {@link com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider})
     * @param config    the configuration for outbound requests to the
     * specified application with the given authentication provider. Must not
     * be {@code null}. Keys do not need to be prefixed.
     */
    void registerProvider(ApplicationId id, Class<? extends AuthenticationProvider> provider, Map<String,String> config);

    /**
     * Removes the configuration for the specified applink/auth-provider
     * combination. After this, {@link #isConfigured(com.atlassian.applinks.api.ApplicationId, Class)} will
     * return {@code false}.
     * Does nothing if this applink/auth-provider was never registered.
     *
     * @param id the id of the application link for which to
     * remove authentication information.
     * @param provider   the type of a AppLinks authentication
     * provider (e.g. {@link com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider})
     */
    void unregisterProvider(ApplicationId id, Class<? extends AuthenticationProvider> provider);

    /**
     * Returns the configuration previously stored for this application/auth
     * pair, or {@code null} when no configuration was ever stored.
     *
     * @param id the id of the application link for which to
     * retrieve authentication information.
     * @param provider   the type of a AppLinks authentication
     * provider (e.g. {@link com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider})
     * @return  the configuration previously stored for this application/auth
     * pair, or {@code null} when no configuration was ever stored.
     */
    Map<String, String> getConfiguration(ApplicationId id, Class<? extends AuthenticationProvider> provider);
}
