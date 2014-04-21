package com.atlassian.applinks.api;

import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.api.auth.Anonymous;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.ImpersonatingAuthenticationProvider;
import com.atlassian.applinks.api.auth.NonImpersonatingAuthenticationProvider;
import com.atlassian.applinks.api.event.ApplicationLinksIDChangedEvent;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;

import java.net.URI;

/**
 * Represents a link to a remote application-level entity (JIRA, Confluence, Bamboo, etc).
 *
 * You can store simple data against {@link ApplicationLink}s using the methods provided by the {@link PropertySet}
 * interface. Note that these properties are shared between all plugins in the local application, so be careful to
 * namespace your property keys carefully.
 *
 * @since 3.0
 * @see PropertySet
 */
public interface ApplicationLink extends PropertySet
{
    /**
     * This {@link ApplicationId} is subject to change if an administrator:
     * <ul>
     *  <li>upgrades the remote application to use Unified Application Links (and this link was created prior to the upgrade); or</li>
     *  <li>the remote application <em>does not</em> have Unified Application Links deployed, and its RPC URL changes.</li>
     * </ul>
     * If you are storing the {@link ApplicationId} for any reason, your plugin should listen for the
     * {@link ApplicationLinksIDChangedEvent}.
     *
     * @return the globally unique ID of the server at the other end of this link.
     */
    ApplicationId getId();

    /**
     * See the {@link com.atlassian.applinks.api.application} package for a list of {@link ApplicationType}s bundled
     * with the Unified Application Links plugin. Additional types can be added via the extension APIs in the
     * <strong>applinks-spi</strong> module.
     *
     * @return the type of the application e.g. {@link JiraApplicationType}
     */
    ApplicationType getType();

    /**
     * @return a brief identifier for the linked application e.g.
     *         "My FishEye". Note that this property is never guaranteed to be unique
     *         or immutable. Do not use as primary key (use ID instead).
     */
    String getName();

    /**
     * @return the base URL to be used when constructing links that are sent to
     *         clients, e.g. web browsers. The {@link URI} returned by this
     *         method will have no trailing slash. e.g. "https://mydomain.com/jira"
     */
    URI getDisplayUrl();

    /**
     * @return the base URL to be used when constructing URIs to be used for RPC
     *         calls to this application (e.g. xml-rpc, SOAP or REST requests). The
     *         returned {@link URI} will have no trailing slash.
     *         e.g. "http://localhost:8080/jira"
     */
    URI getRpcUrl();

    /**
     * @return true if this is the primary {@link ApplicationLink} of its type, false otherwise
     */
    boolean isPrimary();

    /**
     * The {@link ApplicationLinkRequestFactory} returned by this method will choose a single
     * {@link AuthenticationProvider} for automatically authenticating created {@link Request} objects. The selection
     * algorithm is as follows:
     * <ul>
     * <li>If one or more {@link ImpersonatingAuthenticationProvider}s are configured (for example, OAuth or Trusted
     * Applications), one will be used. Trusted Applications will be returned in preference to OAuth if both are configured;</li>
     * <li><em>else</em> if one or more {@link NonImpersonatingAuthenticationProvider}s are configured (for example,
     * Basic Auth), one will be used</li>
     * <li><em>else</em> the request will be unauthenticated, and processed in the remote application anonymously.</li>
     * </ul>
     *
     * Additional {@link AuthenticationProvider}s can be implemented via the extension APIs in the
     * <strong>applinks-spi</strong> module.
     *
     * If your feature only supports a single authentication method, you should use
     * {@link #createAuthenticatedRequestFactory(Class)}}.
     *
     * @return an {@link ApplicationLinkRequestFactory} for creating requests that are authenticated for this
     * {@link ApplicationLink}.
     * @see ApplicationLinkRequestFactory
     * @see ImpersonatingAuthenticationProvider
     * @see NonImpersonatingAuthenticationProvider
     * @see #createAuthenticatedRequestFactory(Class)
     */
    ApplicationLinkRequestFactory createAuthenticatedRequestFactory();

    /**
     * This method returns a {@link ApplicationLinkRequestFactory} initialised by the specified
     * {@link AuthenticationProvider}. You should use this method only if your feature requires a specific
     * {@link AuthenticationProvider} implementation. Note that this method will return null if an administrator
     * of the local application has not configured the specified {@link AuthenticationProvider} for this
     * {@link ApplicationLink}.
     *
     * @param providerClass the {@link AuthenticationProvider} type to use for the {@link RequestFactory}
     * @return a {@link RequestFactory} for creating requests that are authenticated for this {@link ApplicationLink}
     *         using an {@link AuthenticationProvider} implementation that conforms to the supplied providerClass, or
     *         null if no {@link AuthenticationProvider} is configured for this {@link ApplicationLink}. If the supplied
     *         providerClass is {@code AuthenticationProvider.class} this method will bind the {@link RequestFactory}
     *         to <em>any</em> available {@link AuthenticationProvider}, or return null if there are no
     *         {@link AuthenticationProvider}s configured. This method will only return an unauthenticated
     *         {@link RequestFactory} if the supplied providerClass is {@link Anonymous}.
     * @see #createAuthenticatedRequestFactory()
     */
    ApplicationLinkRequestFactory createAuthenticatedRequestFactory(Class<? extends AuthenticationProvider> providerClass);

}
