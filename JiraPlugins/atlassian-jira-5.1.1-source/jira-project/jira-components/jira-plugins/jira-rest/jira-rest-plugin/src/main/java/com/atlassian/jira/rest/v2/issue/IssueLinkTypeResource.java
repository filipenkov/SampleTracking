package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.NoSuchElementException;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Rest resource to retrieve a list of issue link types.
 * @link {com.atlassian.jira.issue.link.IssueLinkType}
 *
 * @since v4.3
 */
@Path ("issueLinkType")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueLinkTypeResource
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final ApplicationProperties applicationProperties;
    private final ContextI18n i18n;
    private ContextUriInfo contextUriInfo;
    private RestUrlBuilder restUrlBuilder;


    public IssueLinkTypeResource(IssueLinkTypeManager issueLinkTypeManager, ApplicationProperties applicationProperties,
            ContextI18n i18n, ContextUriInfo contextUriInfo, RestUrlBuilder restUrlBuilder)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.applicationProperties = applicationProperties;
        this.i18n = i18n;
        this.contextUriInfo = contextUriInfo;
        this.restUrlBuilder = restUrlBuilder;
    }


    /**
     * Returns a list of available issue link types, if issue linking is enabled.
     * Each issue link type has an id, a name and a label for the outward and inward link relationship.
     *
     * @return a list of available issue link types.
     *
     * @response.representation.200.qname
     *      issueLinkTypes
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of all available issue link types.
     *
     * @response.representation.200.example
     *      {@link IssueLinkTypesBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if issue linking is disabled.
     */
    @GET
    public Response getIssueLinkTypes()
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled"))));
        }
        final Collection<IssueLinkType> linkTypes = issueLinkTypeManager.getIssueLinkTypes();
        final Iterable<IssueLinkTypeJsonBean> iterable = Iterables.transform(linkTypes, new Function<IssueLinkType, IssueLinkTypeJsonBean>()
        {
            public IssueLinkTypeJsonBean apply(@Nullable IssueLinkType from)
            {
                final URI uri = restUrlBuilder.getURI(restUrlBuilder.getUrlFor(contextUriInfo.getBaseUri(), IssueLinkTypeResource.class).getIssueLinkType(from.getId().toString()));
                return new IssueLinkTypeJsonBean(from.getId(), from.getName(), from.getInward(), from.getOutward(), uri);
            }
        });
        return Response.ok(IssueLinkTypesBean.create(Lists.newArrayList(iterable))).cacheControl(never()).build();
    }

    /**
     * Returns for a given issue link type id all information about this issue link type.
     *
     * @response.representation.200.qname
     *      issueLinkType
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns the issue link type with the given id.
     *
     * @response.representation.200.example
     *      {@link ResourceExamples#ISSUE_LINK_TYPE_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if issue linking is disabled or no issue link type with the given id exists.
     *
     * @response.represenation.400.doc
     *     Returned if the supplied id is invalid.
     *
     * @return returns information about an issue link type. Containing the id, name and inward and outward description for
     *         this link.
     */
    @GET
    @Path ("/{issueLinkTypeId}")
    public Response getIssueLinkType(@PathParam ("issueLinkTypeId") final String issueLinkTypeIdString)
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled"))));
        }
        final Long issueLinkTypeId;
        try
        {
            issueLinkTypeId = Long.parseLong(issueLinkTypeIdString);
        }
        catch (NumberFormatException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(i18n.getText("rest.issue.link.type.invalid.id", issueLinkTypeIdString)));
        }
        final IssueLinkType linkType;
        try
        {
            linkType = Iterables.find(issueLinkTypeManager.getIssueLinkTypes(), new Predicate<IssueLinkType>()
            {

                public boolean apply(@Nullable IssueLinkType input)
                {
                    return input.getId().equals(issueLinkTypeId);
                }
            });
        }
        catch (NoSuchElementException e)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.issue.link.type.with.id.not.found", issueLinkTypeIdString)));
        }
        final URI uri = restUrlBuilder.getURI(restUrlBuilder.getUrlFor(contextUriInfo.getBaseUri(), IssueLinkTypeResource.class).getIssueLinkType(issueLinkTypeIdString));
        return Response.ok(IssueLinkTypeJsonBean.create(linkType, uri)).cacheControl(never()).build();
    }

}
