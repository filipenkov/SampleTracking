package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateRequest;
import com.atlassian.jira.rest.api.issue.RemoteIssueLinkCreateOrUpdateResponse;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Client for the issue resource.
 *
 * @since v4.3
 */
public class IssueClient extends RestApiClient<IssueClient>
{
    /**
     * Constructs a new IssueClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public IssueClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the issue with the given key.
     *
     * @param issueKey a String containing an issue key
     * @param expand the attributes to expand
     * @return an Issue
     * @throws UniformInterfaceException if there's a problem getting the issue
     */
    public Issue get(String issueKey, Issue.Expand... expand) throws UniformInterfaceException
    {
        return issueResource(issueKey, expand).get(Issue.class);
    }

    public WebResource issueResource(String issueKey, Issue.Expand... expand)
    {
        return issueWithKey(issueKey, Collections.<StringList>emptyList(), setOf(Issue.Expand.class, expand));
    }

    /**
     * GETs the issue with the given key, returning only the request fields.
     *
     * @param issueKey a String containing an issue key
     * @param fields the list of fields to return for the issue
     * @return an Issue
     * @throws UniformInterfaceException if there's a problem getting the issue
     */
    public Issue getPartially(String issueKey, StringList... fields) throws UniformInterfaceException
    {
        return getPartially(issueKey, setOf(Issue.Expand.class), fields);
    }

    public Issue getPartially(String issueKey, EnumSet<Issue.Expand> expand, StringList... fields) throws UniformInterfaceException
    {
        return issueWithKey(issueKey, Arrays.asList(fields), expand).get(Issue.class);
    }

    /**
     * GETs the issue from the given URL.
     *
     * @param issueURL a String containing the valid URL for an issue
     * @param expand the attributes to expand
     * @return an Issue
     * @throws UniformInterfaceException if there's a problem getting the issue
     */
    public Issue getFromURL(String issueURL, Issue.Expand... expand) throws UniformInterfaceException
    {
        return expanded(resourceRoot(issueURL), setOf(Issue.Expand.class, expand)).get(Issue.class);
    }

    public IssueCreateResponse create(IssueUpdateRequest issue)
    {
        try
        {
            return createResource().type(APPLICATION_JSON_TYPE).post(IssueCreateResponse.class, issue);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to create issue: " + errorResponse(e.getResponse()), e);
        }
    }

    public void edit(final String issueKey, final IssueUpdateRequest updateRequest)
    {
        try
        {
            createResourceWithIssueKey(issueKey).type(APPLICATION_JSON_TYPE).put(updateRequest);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to edit issue: " + errorResponse(e.getResponse()), e);
        }
    }

    /**
     * Creates an issue as per the request, and returns the Response.
     *
     * @param issue an IssueCreateRequest
     * @return a Response
     */
    public Response getResponse(final IssueUpdateRequest issue)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().type(APPLICATION_JSON_TYPE).post(ClientResponse.class, issue);
            }
        });
    }

    public Response operationalUpdateResponse(final String issueKey, final OperationalUpdateRequest update)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path(issueKey).type(APPLICATION_JSON_TYPE).put(ClientResponse.class, update);
            }
        });
    }

    public void operationalUpdate(final String issueKey, OperationalUpdateRequest updateRequest)
    {
        try
        {
            createResource().path(issueKey).type(APPLICATION_JSON_TYPE).put(updateRequest);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to update issue: " + errorResponse(e.getResponse()), e);
        }
    }

    public void update(String issueKey, IssueUpdateRequest issue)
    {
        try
        {
            createResource().path(issueKey).type(APPLICATION_JSON_TYPE).put(issue);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to update issue: " + errorResponse(e.getResponse()), e);
        }
    }

    public Response updateResponse(final String issueKey, final IssueUpdateRequest update)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path(issueKey).type(APPLICATION_JSON_TYPE).put(ClientResponse.class, update);
            }
        });
    }

    public Response update(final String issueKey, final Map update)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path(issueKey).type(APPLICATION_JSON_TYPE).put(ClientResponse.class, update);
            }
        });
    }

    /**
     * GETs the issue with the given key.
     *
     * @param issueKey a String containing an issue key or id
     * @param deleteSubtasks the attributes to expand
     * @return a Response
     * @throws UniformInterfaceException if there's a problem deleting the issue
     */
    public Response delete(final String issueKey, final String deleteSubtasks) throws UniformInterfaceException
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                WebResource deleteResource = createResource().path(issueKey);
                if (deleteSubtasks != null)
                {
                    deleteResource = deleteResource.queryParam("deleteSubtasks", deleteSubtasks);
                }
                return deleteResource.delete(ClientResponse.class);
            }
        });
    }

    /**
     * GETs the issue with the given key.
     *
     * @param issueKey a String containing an issue key or id
     * @param assignee user object
     * @return a Response
     * @throws UniformInterfaceException if there's a problem deleting the issue
     */
    public Response assign(final String issueKey, final User assignee) throws UniformInterfaceException
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                WebResource assignResource = createResource().path(issueKey).path("assignee");
                return assignResource.type(APPLICATION_JSON_TYPE).put(ClientResponse.class, assignee);
            }
        });
    }

    /**
     * GETs the issue with the given key, returning a Response.
     *
     * @param issueKey a String containing an issue key
     * @return a Response
     */
    public Response getResponse(final String issueKey)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return issueWithKey(issueKey, null, setOf(Issue.Expand.class)).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the issue with the given key.
     *
     * @param issueKey a String containing an issue key
     * @param fields the list of fields to return for the issue
     * @param expand what to expand
     * @return a WebResource
     */
    protected WebResource issueWithKey(String issueKey, @Nullable List<StringList> fields, EnumSet<Issue.Expand> expand)
    {
        WebResource resource = createResource().path(issueKey);
        resource = addStringListsToQueryParams(resource, "fields", fields);

        return expanded(resource, expand);
    }

    /**
     * Returns the meta data for creating issues.
     *
     * @param projectIds the list of projects to filter on
     * @param projectKeys the list of projects to filter on
     * @param issueTypeIds the list of issue types to filter on
     * @param expand what to expand
     * @return an IssueCreateMeta
     */
    public IssueCreateMeta getCreateMeta(@Nullable final List<StringList> projectIds, @Nullable final List<StringList> projectKeys,
            @Nullable final List<StringList> issueTypeIds, @Nullable final List<String> issueTypeNames,
            final IssueCreateMeta.Expand... expand)
    {
        return getCreateMetaResource(projectIds, projectKeys, issueTypeIds, issueTypeNames, setOf(IssueCreateMeta.Expand.class, expand))
                .get(IssueCreateMeta.class);
    }

    /**
     * Gets the meta data for creating issues, returning a Response.
     *
     * @param projectIds the list of projects to filter on
     * @param projectKeys the list of projects to filter on
     * @param issueTypeIds the list of issue types to filter on
     * @param expand what to expand
     * @return a Response
     */
    public Response getCreateMetaResponse(@Nullable final List<StringList> projectIds, @Nullable final List<StringList> projectKeys,
            @Nullable final List<StringList> issueTypeIds, @Nullable final List<String> issueTypeNames,
            final IssueCreateMeta.Expand... expand)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return getCreateMetaResource(projectIds, projectKeys, issueTypeIds, issueTypeNames, setOf(IssueCreateMeta.Expand.class, expand))
                        .get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns WebResource, containing the meta data for creating issues.
     *
     * @param projectIds the list of projects to filter on
     * @param projectKeys the list of projects to filter on
     * @param issueTypeIds the list of issue types to filter on
     * @param expand what to expand
     * @return a WebResource
     */
    private WebResource getCreateMetaResource(@Nullable final List<StringList> projectIds, @Nullable final List<StringList> projectKeys,
            @Nullable final List<StringList> issueTypeIds, @Nullable final List<String> issueTypeNames,
            final EnumSet<IssueCreateMeta.Expand> expand)
    {
        WebResource resource = createResource().path("createmeta");
        resource = addStringListsToQueryParams(resource, "projectIds", projectIds);
        resource = addStringListsToQueryParams(resource, "projectKeys", projectKeys);
        resource = addStringListsToQueryParams(resource, "issuetypeIds", issueTypeIds);
        resource = addStringsToQueryParams(resource, "issuetypeNames", issueTypeNames);
        resource = expanded(resource, expand);

        return resource;
    }

    /**
     * Create a remote link.
     *
     * @param issueKey
     * @param remoteIssueLink
     * @return a RemoteIssueLinkCreateOrUpdateResponse
     */
    public RemoteIssueLinkCreateOrUpdateResponse createOrUpdateRemoteIssueLink(final String issueKey, final RemoteIssueLinkCreateOrUpdateRequest remoteIssueLink)
    {
        try
        {
            return createResource().path(issueKey).path("remotelink")
                    .type(APPLICATION_JSON_TYPE)
                    .post(RemoteIssueLinkCreateOrUpdateResponse.class, remoteIssueLink);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to create remote link: " + errorResponse(e.getResponse()), e);
        }
    }

    /**
     * Create a remote link, and return a Response. This is useful for checking error conditions.
     *
     * @param issueKey
     * @param remoteIssueLink
     * @return a Response
     */
    public Response createOrUpdateRemoteIssueLinkAndGetResponse(final String issueKey, final RemoteIssueLinkCreateOrUpdateRequest remoteIssueLink)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path(issueKey).path("remotelink")
                        .type(APPLICATION_JSON_TYPE)
                        .post(ClientResponse.class, remoteIssueLink);
            }
        });
    }

    /**
     * Create a remote link, and return a ClientResponse.
     *
     * @param issueKey
     * @param remoteIssueLink
     * @return a Response
     */
    public ClientResponse createOrUpdateRemoteIssueLinkAndGetClientResponse(final String issueKey, final RemoteIssueLinkCreateOrUpdateRequest remoteIssueLink)
    {
        return createResource().path(issueKey).path("remotelink")
                        .type(APPLICATION_JSON_TYPE)
                        .post(ClientResponse.class, remoteIssueLink);
    }

    /**
     * Update a remote link, and return a Response. This is useful for checking error conditions.
     *
     * @param issueKey
     * @param remoteIssueLink
     * @return a Response
     */
    public Response updateRemoteIssueLink(final String issueKey, final String linkId, final RemoteIssueLinkCreateOrUpdateRequest remoteIssueLink)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path(issueKey).path("remotelink").path(linkId)
                        .type(APPLICATION_JSON_TYPE)
                        .put(ClientResponse.class, remoteIssueLink);
            }
        });
    }


    /**
     * Delete a remote link, and return a Response.
     *
     * @param issueKey
     * @param remoteIssueLinkId
     * @return a Response
     */
    public Response deleteRemoteIssueLink(final String issueKey, final String remoteIssueLinkId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path(issueKey).path("remotelink").path(remoteIssueLinkId)
                        .type(APPLICATION_JSON_TYPE)
                        .delete(ClientResponse.class);
            }
        });
    }

    /**
     * Delete a remote link by global id, and return a Response.
     *
     * @param issueKey
     * @param globalId
     * @return a Response
     */
    public Response deleteRemoteIssueLinkByGlobalId(final String issueKey, final String globalId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path(issueKey).path("remotelink").queryParam("globalId", globalId)
                        .delete(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a remote link.
     *
     * @param issueKey
     * @param remoteIssueLinkId
     * @return a RemoteIssueLink
     */
    public RemoteIssueLink getRemoteIssueLink(final String issueKey, final String remoteIssueLinkId)
    {
        return getRemoteIssueLinkResource(issueKey, remoteIssueLinkId, null).get(RemoteIssueLink.class);
    }

    /**
     * Returns the remote links for an issue.
     *
     * @param issueKey
     * @return a List of RemoteIssueLinks
     */
    public List<RemoteIssueLink> getRemoteIssueLinks(final String issueKey)
    {
        return getRemoteIssueLinkResource(issueKey, null, null).get(RemoteIssueLink.REMOTE_ISSUE_LINKS_TYPE);
    }

    /**
     * Returns the remote link for an issue with the given globalId.
     *
     * @param issueKey
     * @param globalId
     * @return a RemoteIssueLink
     */
    public RemoteIssueLink getRemoteIssueLinkByGlobalId(final String issueKey, final String globalId)
    {
        final Map<String, String> params = MapBuilder.<String, String>newBuilder()
                .add("globalId", globalId)
                .toMap();
        return getRemoteIssueLinkResource(issueKey, null, params).get(RemoteIssueLink.class);
    }

    /**
     * Gets a remote link, returning a Response.
     *
     * @param issueKey
     * @param remoteIssueLinkId
     * @return a Response
     */
    public Response getRemoteIssueLinkResponse(final String issueKey, final String remoteIssueLinkId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return getRemoteIssueLinkResource(issueKey, remoteIssueLinkId, null).get(ClientResponse.class);
            }
        });
    }

    /**
     * Gets the remote links for an issue, returning a Response.
     *
     * @param issueKey
     * @return a Response
     */
    public Response getRemoteIssueLinksResponse(final String issueKey)
    {
        return getRemoteIssueLinksResponse(issueKey, null);
    }

    public Response getRemoteIssueLinksResponse(final String issueKey, @Nullable final Map<String, String> queryParams)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return getRemoteIssueLinkResource(issueKey, null, queryParams).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns WebResource, containing remote link info.
     *
     * @param issueKey
     * @param remoteIssueLinkId the id of the remote link, if null get all links
     * @return a WebResource
     */
    private WebResource getRemoteIssueLinkResource(final String issueKey, @Nullable final String remoteIssueLinkId, @Nullable final Map<String, String> queryParams)
    {
        WebResource resource = createResource().path(issueKey).path("remotelink");

        if (remoteIssueLinkId != null)
        {
            resource = resource.path(remoteIssueLinkId);
        }

        if (queryParams != null)
        {
            for (final Map.Entry<String, String> entry : queryParams.entrySet())
            {
                resource = resource.queryParam(entry.getKey(), entry.getValue());
            }
        }

        return resource;
    }

    private WebResource addStringListsToQueryParams(WebResource resource, final String paramName, final Iterable<StringList> stringLists)
    {
        if (stringLists != null)
        {
            for (StringList stringList : stringLists)
            {
                resource = resource.queryParam(paramName, stringList.toQueryParam());
            }
        }

        return resource;
    }

    private WebResource addStringsToQueryParams(WebResource resource, final String paramName, final Iterable<String> strings)
    {
        if (strings != null)
        {
            for (String string: strings)
            {
                resource = resource.queryParam(paramName, string);
            }
        }

        return resource;
    }

    private WebResource createResourceWithIssueKey(String issueKey)
    {
        return createResource().path(issueKey);
    }


    @Override
    protected WebResource createResource()
    {
        return super.createResource().path("issue");
    }
}
