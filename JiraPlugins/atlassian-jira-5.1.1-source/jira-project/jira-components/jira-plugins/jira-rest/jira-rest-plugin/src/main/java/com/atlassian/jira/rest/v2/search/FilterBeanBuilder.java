package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * A builder utility to create a {@link FilterBean}
 *
 * @since v5.0
 */
public class FilterBeanBuilder
{

    private SearchRequest filter;
    private UriInfo context;
    private String canoncialBaseUrl;
    private User owner = null;
    private boolean favourite = false;

    public FilterBeanBuilder()
    {
    }

    /**
     * Sets the filter
     *
     * @param filter a filter
     * @return this
     */
    public FilterBeanBuilder filter(final SearchRequest filter)
    {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the context.
     *
     * @param context a UriInfo
     * @param canoncialBaseUrl the baseurl of this instance
     * @return this
     */
    public FilterBeanBuilder context(UriInfo context, final String canoncialBaseUrl)
    {
        this.context = context;
        this.canoncialBaseUrl = canoncialBaseUrl;
        return this;
    }


    public FilterBeanBuilder owner(User owner)
    {
        this.owner = owner;
        return this;
    }

    public FilterBeanBuilder favourite(boolean favourite)
    {
        this.favourite = favourite;
        return this;
    }

    public FilterBean build()
    {
        if (filter != null)
        {
            if (context == null || canoncialBaseUrl == null)
            {
                throw new IllegalStateException("No context set.");
            }

            final UserBean owner = new UserBeanBuilder().user(this.owner).context(context).buildShort();
            final URI issueNavUri = URI.create(canoncialBaseUrl +
                    "/secure/IssueNavigator.jspa?mode=hide&requestId=" + filter.getId());

            final URI self = context.getBaseUriBuilder().path(FilterResource.class).
                    path(Long.toString(filter.getId())).build();

            URI searchUri = null;
            //For anonymous users the query string returns null to avoid leaking data.
            if(filter.getQuery().getQueryString() != null)
            {
                searchUri = context.getBaseUriBuilder().path(SearchResource.class).queryParam("jql", "{0}").
                    build(filter.getQuery().getQueryString());
            }

            return new FilterBean(self, Long.toString(filter.getId()), filter.getName(), filter.getDescription(), owner,
                    filter.getQuery().getQueryString(), issueNavUri, searchUri, favourite);
        }
        return null;
    }
}
