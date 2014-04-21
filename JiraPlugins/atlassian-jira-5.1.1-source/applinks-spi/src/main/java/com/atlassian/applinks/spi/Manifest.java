package com.atlassian.applinks.spi;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.spi.application.TypeId;
import org.osgi.framework.Version;

import java.net.URI;
import java.util.Set;

/**
 * Describes the capabilities of a remote application. A remote application
 * can be another Atlassian app running AppLinks 3 or higher, or something
 * else entirely (a subversion repo, a Google docs domain, etc).
 *
 * @since   3.0
 */
public interface Manifest
{
    /**
     * Get the ID of this server.
     * 
     * @return  the globally unique, immutable ID of this server.
     */
    ApplicationId getId();

    /**
     * Get the name of this server.  This should be the configured name of the instance of the remote application, or
     * if the remote application is a static service, then the name of the service.  For example, "My JIRA Instance".
     *
     * @return The name of the remote application.
     */
    String getName();

    /**
     * Get the type ID of the application this manifest is for.
     *
     * @return The type ID of the application this manifest is for.
     */
    TypeId getTypeId();

    /**
     * The version of the application. Note that version strings formatting is
     * not standardised. Although Atlassian applications tend to use
     * "major.minor.micro", there can also be non-numeric components (e.g.
     * "2.4-M2", "2.4.1.beta4", etc).
     *
     * @return  the version of the application. Can return {@code null}.
     */
    String getVersion();

    /**
     * Get the build number of the remote application. Can return {@code null}.
     *
     * @return The build number of the application.
     */
    Long getBuildNumber();

    /**
     * Get the URL of the remote application
     *
     * @return The URL.
     */
    URI getUrl();

    /**
     * Get the version of AppLinks running on the remote application.
     *
     * @return the version of the applinks bundle installed in the remote application, or {@code null} if the remote
     * application is not an applinks 3.0+ container
     */
    Version getAppLinksVersion();

    /**
     * Get the inbound authentication types that this remote application supports.  The inbound authentication types are
     * the authentication types that the remote application can use to authenticate requests that it receives.
     *
     * @return The set of authentication types
     */
    Set<Class<? extends AuthenticationProvider>> getInboundAuthenticationTypes();

    /**
     * Get the outbound authentication types that this remote application supports.  The outbound authentication types
     * are the authentication types that this remote application can use to authenticate requests that it sends.
     *
     * @return The set of authentication types
     */
    Set<Class<? extends AuthenticationProvider>> getOutboundAuthenticationTypes();

    /**
     * Whether the remote application supports public sign up.
     *
     * @return True if the remote application supports public sign up.
     */
    Boolean hasPublicSignup();
}
