package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.plugins.rest.common.Link;
import org.apache.commons.lang.Validate;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Helper for creating URI links to resources.
 *
 * @since v2.1
 */
public class LinkUriHelper
{
    private static final String USERNAME_QUERY_PARAM = "username";
    private static final String GROUPNAME_QUERY_PARAM = "groupname";

    private LinkUriHelper()
    {
        // prevent instantiation
    }

    /**
     * Returns the Link to the User resource.
     *
     * @param baseUri base URI of the REST service
     * @param username Username
     * @return URI to the user resource.
     */
    public static Link buildUserLink(final URI baseUri, final String username)
    {
        final URI userUri = buildUserUri(baseUri, username);
        return Link.self(userUri);
    }

    /**
     * Returns the URI to the User resource.
     *
     * @param baseUri base URI of the REST service
     * @param username Username
     * @return URI to the user resource.
     */
    public static URI buildUserUri(final URI baseUri, final String username)
    {
        Validate.notNull(baseUri);
        Validate.notNull(username);
        final UriBuilder builder = UriBuilder.fromUri(baseUri);
        return builder.path("user").queryParam(USERNAME_QUERY_PARAM, "{username}").build(username);
    }

    /**
     * Returns the URI to the group direct user group resource.
     *
     * @param baseURI base URI of the REST service
     * @param groupName Group name
     * @param username username
     * @return URI to the group direct user resource.
     */
    public static URI buildDirectUserGroupUri(final URI baseURI, final String groupName, final String username)
    {
        Validate.notNull(baseURI);
        Validate.notNull(groupName);
        final UriBuilder builder = UriBuilder.fromUri(baseURI);
        return builder.path("group").path("user").path("direct").queryParam("groupname", "{groupName}").queryParam("username", "{username}").build(groupName, username);
    }

    /**
     * Returns an updated user Link with the specified username.
     *
     * @param userLink current user Link
     * @param username new username
     * @return updated user Link.
     */
    public static Link updateUserLink(final Link userLink, final String username)
    {
        final URI updatedUri = updateUserUri(userLink.getHref(), username);
        return Link.link(updatedUri, userLink.getRel());
    }

    /**
     * Returns an updated user URI with the specified username.
     *
     * @param userUri current user URI
     * @param username new username
     * @return updated user URI.
     */
    public static URI updateUserUri(final URI userUri, final String username)
    {
        final UriBuilder builder = UriBuilder.fromUri(userUri);
        return builder.replaceQueryParam(USERNAME_QUERY_PARAM, "{username}").build(username);
    }

    /**
     * Returns the Link to the Group resource.
     *
     * @param baseUri base URI of the REST service
     * @param groupName group name
     * @return URI to the user resource.
     */
    public static Link buildGroupLink(final URI baseUri, final String groupName)
    {
        final URI groupUri = buildGroupUri(baseUri, groupName);
        return Link.self(groupUri);
    }

    /**
     * Returns the URI to the Group resource.
     *
     * @param baseURI base URI of the REST service
     * @param groupName Group name
     * @return URI to the group resource.
     */
    public static URI buildGroupUri(final URI baseURI, final String groupName)
    {
        Validate.notNull(baseURI);
        Validate.notNull(groupName);
        final UriBuilder builder = UriBuilder.fromUri(baseURI);
        return builder.path("group").queryParam(GROUPNAME_QUERY_PARAM, "{groupName}").build(groupName);
    }

    /**
     * Returns the URI to the group direct child group resource.
     *
     * @param baseURI base URI of the REST service
     * @param groupName group name
     * @param childGroupName child group name
     * @return URI to the group direct child group resource.
     */
    public static URI buildDirectChildGroupUri(final URI baseURI, final String groupName, final String childGroupName)
    {
        Validate.notNull(baseURI);
        Validate.notNull(groupName);
        final UriBuilder builder = UriBuilder.fromUri(baseURI);
        return builder.path("group").path("child-group").path("direct").queryParam("groupname", "{groupName}").queryParam("child-groupname", "{childGroupName}").build(groupName, childGroupName);
    }

    /**
     * Returns the URI to the group direct parent group resource.
     *
     * @param baseURI base URI of the REST service
     * @param groupName group name
     * @param parentGroupName parent group name
     * @return URI to the group direct parent group resource.
     */
    public static URI buildDirectParentGroupUri(final URI baseURI, final String groupName, final String parentGroupName)
    {
        Validate.notNull(baseURI);
        Validate.notNull(groupName);
        final UriBuilder builder = UriBuilder.fromUri(baseURI);
        return builder.path("group").path("parent-group").path("direct").queryParam("groupname", "{groupName}").queryParam("parent-groupname", "{parentGroupName}").build(groupName, parentGroupName);
    }

    /**
     * Returns the URI to the direct group parent of a user resource.
     *
     * @param baseURI base URI of the REST service
     * @param userName user name
     * @param parentGroupName parent group name
     * @return URI to the direct parent group of a user resource.
     */
    public static URI buildDirectParentGroupOfUserUri(final URI baseURI, final String userName, final String parentGroupName)
    {
        Validate.notNull(baseURI);
        Validate.notNull(userName);
        final UriBuilder builder = UriBuilder.fromUri(baseURI);
        return builder.path("user").path("group").path("direct").queryParam("username", "{userName}").queryParam("groupname", "{groupName}").build(userName, parentGroupName);
    }

    /**
     * Returns an updated group Link with the specified group name.
     *
     * @param groupLink current group Link
     * @param groupName new group name
     * @return updated group Link.
     */
    public static Link updateGroupLink(final Link groupLink, final String groupName)
    {
        final URI updatedUri = updateGroupUri(groupLink.getHref(), groupName);
        return Link.link(updatedUri, groupLink.getRel());
    }

    /**
     * Returns an updated group URI with the specified group name.
     *
     * @param groupUri current group URI
     * @param groupName new group name
     * @return updated group URI.
     */
    public static URI updateGroupUri(final URI groupUri, final String groupName)
    {
        final UriBuilder builder = UriBuilder.fromUri(groupUri);
        return builder.replaceQueryParam(GROUPNAME_QUERY_PARAM, "{groupname}").build(groupName);
    }

    /**
     * Returns the URI to the entity attribute list resource.
     *
     * @param entityUri URI to the entity.
     * @return URI to the entity attributes resource.
     */
    public static URI buildEntityAttributeListUri(final URI entityUri)
    {
        Validate.notNull(entityUri);
        return UriBuilder.fromUri(entityUri).path("attribute").build();
    }

    /**
     * Returns the URI to the entity attribute resource.
     *
     * @param attributesUri URI to the entity attributes.
     * @param attributeName name of the attribute
     * @return URI to the entity attributes resource.
     */
    public static URI buildEntityAttributeUri(final URI attributesUri, final String attributeName)
    {
        Validate.notNull(attributesUri);
        Validate.notNull(attributeName);
        return UriBuilder.fromUri(attributesUri).queryParam("attributename", "{attributeName}").build(attributeName);
    }

    /**
     * Returns the URI to the user password resource.
     *
     * @param userUri URI to the user.
     * @return URI to the user password resource.
     */
    public static URI buildUserPasswordUri(final URI userUri)
    {
        Validate.notNull(userUri);
        return UriBuilder.fromUri(userUri).path("password").build();
    }

    /**
     * Returns the URI to the session resource.
     *
     * @param baseUri base URI of the REST service
     * @return URI to the session resource
     */
    public static URI buildSessionUri(final URI baseUri, final String token)
    {
        Validate.notNull(baseUri);
        Validate.notNull(token);
        return UriBuilder.fromUri(baseUri).path("session/{token}").build(token);
    }

    /**
     * Returns the link to the session resource.
     *
     * @param baseUri base URI of the REST service
     * @return link to the session resource
     */
    public static Link buildSessionLink(final URI baseUri, final String token)
    {
        return Link.self(buildSessionUri(baseUri, token));
    }
}
