package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.plugins.rest.common.Link;
import org.apache.commons.lang.Validate;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 * Helper for creating links and URIs.
 *
 * @since 2.2
 */
public class ApplicationLinkUriHelper
{
    public final static String REMOTE_ADDRESS_QUERY_PARAM = "address";
    public final static String REMOTE_ADDRESSES_PATH_PARAM = "remote_address";
    public final static String DIRECTORY_MAPPINGS_PATH_PARAM = "directory_mapping";
    public final static String PASSWORD_PATH_PARAM = "password";

    private ApplicationLinkUriHelper()
    {
        // prevent instantiation
    }

    /**
     * Returns the Link to the Application resource.
     *
     * @param baseUri base URI of the REST service
     * @param applicationId application ID
     * @return Link to the application resource.
     */
    public static Link buildApplicationLink(final URI baseUri, final Long applicationId)
    {
        final URI applicationUri = buildApplicationUri(baseUri, applicationId);
        return Link.self(applicationUri);
    }

    /**
     * Returns the URI to the Application resource.
     *
     * @param baseUri base URI of the REST service
     * @param applicationId Application ID
     * @return URI to the application resource.
     */
    public static URI buildApplicationUri(final URI baseUri, final Long applicationId)
    {
        Validate.notNull(baseUri);
        Validate.notNull(applicationId);
        final URI applicationsUri = buildApplicationsUri(baseUri);
        final UriBuilder builder = UriBuilder.fromUri(applicationsUri);
        return builder.path("{applicationId}").build(applicationId);
    }

    /**
     * Returns the URI to the list of all Applications resource.
     *
     * @param baseUri base URI of the REST service
     * @return URI to the list of all applications resource.
     */
    public static URI buildApplicationsUri(final URI baseUri)
    {
        Validate.notNull(baseUri);
        final UriBuilder builder = UriBuilder.fromUri(baseUri);
        return builder.path("application").build();
    }

    /**
     * Returns the URI to the Application Remote Addresses resource.
     *
     * @param applicationUri URI of the application resource
     * @return URI to the application remote addresses resource.
     */
    public static URI buildRemoteAddressesUri(final URI applicationUri)
    {
        Validate.notNull(applicationUri);
        final UriBuilder builder = UriBuilder.fromUri(applicationUri);
        return builder.path(REMOTE_ADDRESSES_PATH_PARAM).build();
    }

    /**
     * Returns the URI to the Application Remote Address resource.
     *
     * @param remoteAddressesUri URI of the application remote addresses resource
     * @param remoteAddress the remote address to reference
     * @return URI to the application remote address resource.
     */
    public static URI buildRemoteAddressUri(final URI remoteAddressesUri, final String remoteAddress)
    {
        Validate.notNull(remoteAddressesUri);
        final UriBuilder builder = UriBuilder.fromUri(remoteAddressesUri);
        return builder.queryParam(REMOTE_ADDRESS_QUERY_PARAM, "{remoteAddress}").build(remoteAddress);
    }

    /**
     * Returns the URI to the Application Remote Address resource.
     *
     * @param baseUri base URI REST service
     * @param applicationId ID of the application
     * @param remoteAddress the remote address to reference
     * @return URI to the application remote address resource.
     */
    public static URI buildRemoteAddressUri(final URI baseUri, final long applicationId, final String remoteAddress)
    {
        Validate.notNull(baseUri);
        final URI applicationUri = buildApplicationUri(baseUri, applicationId);
        final URI applicationRemoteAddressesUri = buildRemoteAddressesUri(applicationUri);
        final UriBuilder builder = UriBuilder.fromUri(applicationRemoteAddressesUri);
        return builder.queryParam(REMOTE_ADDRESS_QUERY_PARAM, "{remoteAddress}").build(remoteAddress);
    }

    /**
     * Returns the URI to the Application directory mappings resource.
     *
     * @param applicationUri URI to the application resource
     * @return URI to the application directory mappings resource.
     */
    public static URI buildDirectoryMappingsUri(final URI applicationUri)
    {
        Validate.notNull(applicationUri);
        final UriBuilder builder = UriBuilder.fromUri(applicationUri);
        return builder.path(DIRECTORY_MAPPINGS_PATH_PARAM).build();
    }

    /**
     * Returns the URI to the Application directory mapping resource.
     *
     * @param directoryMappingsUri URI to the directory mappings resource
     * @param directoryId ID of the mapped directory
     * @return URI to the application directory mapping resource.
     */
    public static URI buildDirectoryMappingUri(final URI directoryMappingsUri, final long directoryId)
    {
        Validate.notNull(directoryMappingsUri);
        final UriBuilder builder = UriBuilder.fromUri(directoryMappingsUri);
        return builder.path(String.valueOf(directoryId)).build();
    }

    /**
     * Returns the URI to the application password resource.
     *
     * @param applicationUri URI to the application
     * @return URI to the application password
     */
    public static URI buildPasswordUri(final URI applicationUri)
    {
        Validate.notNull(applicationUri);
        return UriBuilder.fromUri(applicationUri).path(PASSWORD_PATH_PARAM).build();
    }
}
