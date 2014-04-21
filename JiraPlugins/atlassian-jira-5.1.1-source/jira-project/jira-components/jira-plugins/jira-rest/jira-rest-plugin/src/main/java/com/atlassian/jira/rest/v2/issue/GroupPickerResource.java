package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DelimeterInserter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static java.util.Arrays.asList;

/**
 * REST endpoint for searching groups in a group picker
 *
 * @since v4.4
 */
@Path ("groups")
@AnonymousAllowed
@Produces ( { MediaType.APPLICATION_JSON })
public class GroupPickerResource
{
    private static final Logger LOG = Logger.getLogger(GroupPickerResource.class);
    static final int DEFAULT_MAX_RESULTS = 20;
    static final String MORE_GROUP_RESULTS_I18N_KEY = "jira.ajax.autocomplete.group.more.results";

    private GroupPickerSearchService service;
    private JiraAuthenticationContext authenticationContext;
    private ApplicationProperties applicationProperties;


    @SuppressWarnings ({ "UnusedDeclaration" })
    private GroupPickerResource()
    {
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    public GroupPickerResource(final GroupPickerSearchService service,
            final JiraAuthenticationContext jiraAuthenticationContext,
            final ApplicationProperties applicationProperties)
    {
        this.service = service;
        this.authenticationContext = jiraAuthenticationContext;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Returns groups with substrings matching a given query. This is mainly for use with
     * the group picker, so the returned groups contain html to be used as picker suggestions.
     * The groups are also wrapped in a single response object that also contains a header for
     * use in the picker, specifically <i>Showing X of Y matching groups</i>.
     *
     * The number of groups returned is limited by the system property "jira.ajax.autocomplete.limit"
     *
     * The groups will be unique and sorted.
     *
     * @param query a String to match groups agains
     * @return a collection of matching groups
     *
     * @response.representation.200.qname
     *      groupsuggestions
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned even if no groups match the given substring
     * @response.representation.200.example
     *      {@link GroupSuggestionsBean#DOC_EXAMPLE}
     */
    @Path("/picker")
    @GET
    public Response findGroups(@QueryParam("query") final String query, @QueryParam("exclude") List<String> excludeGroups)
    {
        final List<GroupSuggestionBean> groupBeans = Lists.newArrayList();
        final List<Group> groups = service.findGroups(query);

        if (excludeGroups == null) {
            excludeGroups = new ArrayList<String>();
        }

        int limit = getLimit();
        int i = 0;
        int total = groups.size();

        for (final Group group : groups)
        {
            if(i < limit)
            {
                if (!excludeGroups.contains(group.getName())) {
                    final String matchingHtml = buildMatchingHtml(group.getName(), query);
                    groupBeans.add(new GroupSuggestionBean(group.getName(), matchingHtml));
                    ++i;
                } else {
                    --total;
                }
            }
            else
            {
                break;
            }
        }

        final String header = buildHeader(groupBeans, total);
        final GroupSuggestionsBean suggestions = new GroupSuggestionsBean(total, header, groupBeans);

        return Response.ok(suggestions)
            .cacheControl(never())
            .build();

    }

    // get the number of items to display.
    private int getLimit()
    {
        //Default limit to 20
        int limit = DEFAULT_MAX_RESULTS;
        try
        {
            limit = Integer.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        catch (Exception nfe)
        {
            LOG.error("jira.ajax.autocomplete.limit does not exist or is an invalid number in jira-application.properties.", nfe);
        }
        return limit;
    }


    private String buildMatchingHtml(final String group, final String query)
    {
        final DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>", false);
        final String matchingHtml = delimeterInserter.insert(TextUtils.htmlEncode(group), new String[] { query });
        return matchingHtml;
    }

    private String buildHeader(final Collection<GroupSuggestionBean> groupBeans, int total)
    {
        return authenticationContext.getI18nHelper().getText(MORE_GROUP_RESULTS_I18N_KEY,
                String.valueOf(groupBeans.size()), String.valueOf(total));
    }


}
