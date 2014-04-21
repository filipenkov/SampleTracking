package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.util.OutlookDate;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A common base class for fragments which display issues on project tab panels.
 *
 * @since v4.0
 */
public abstract class AbstractIssuesFragment extends AbstractMultiFragment
{
    private static final Logger log = Logger.getLogger(AbstractIssuesFragment.class);

    protected static final String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/summary/";
    private static final Integer DEFAULT_DISPLAY_ISSUE_COUNT = 3;

    protected final SearchProvider searchProvider;
    private final ApplicationProperties applicationProperties;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    public AbstractIssuesFragment(final JiraAuthenticationContext jiraAuthenticationContext,
            final VelocityTemplatingEngine templatingEngine, final SearchProvider searchProvider,
            final ApplicationProperties applicationProperties, final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(templatingEngine, jiraAuthenticationContext);
        this.searchProvider = searchProvider;
        this.applicationProperties = applicationProperties;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("issues", getIssues(ctx));
        velocityParams.put("srUrl", createSearchRequestUrl(getSearchRequest(ctx)));
        velocityParams.put("relativeDateTimeFormatter", dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.RELATIVE_WITH_TIME_ONLY).forLoggedInUser());
        velocityParams.put("SFM_HIDE", OutlookDate.SmartFormatterModes.HIDE_TIME);
        return velocityParams;
    }

    /**
     * @param ctx the browse context
     * @return True if the display issue count is greater than zero, and there are issues returned by the search request.
     */
    public boolean showFragment(final BrowseContext ctx)
    {
        return getDisplayIssueCount() > 0 && getIssueCountInSearch(ctx) > 0;
    }

    /**
     * Attempts to resolve the number of issues to display from the application property
     * {@link APKeys#JIRA_PROJECT_SUMMARY_MAX_ISSUES}. Failing that, returns a hard-coded default
     * {@link #DEFAULT_DISPLAY_ISSUE_COUNT}.
     *
     * @return the number of issues to display in this fragment.
     */
    Integer getDisplayIssueCount()
    {
        String displayIssueCount = applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECT_SUMMARY_MAX_ISSUES);
        try
        {
            if (displayIssueCount == null)
            {
                return DEFAULT_DISPLAY_ISSUE_COUNT;
            }
            else
            {
                final Integer intDisplayIssueCount = Integer.valueOf(displayIssueCount);
                if (intDisplayIssueCount < 0)
                {
                    return DEFAULT_DISPLAY_ISSUE_COUNT;
                }
                return intDisplayIssueCount;
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Invalid value for application property '" +APKeys.JIRA_PROJECT_SUMMARY_MAX_ISSUES +"': " + displayIssueCount);
            return DEFAULT_DISPLAY_ISSUE_COUNT;
        }
    }
    
    private long getIssueCountInSearch(final BrowseContext ctx)
    {
        try
        {
            final SearchRequest sr = getSearchRequest(ctx);
            return searchProvider.searchCount(sr.getQuery(), authenticationContext.getLoggedInUser());

        }
        catch (SearchException e)
        {
            log.warn("Could not complete the search request", e);
            return 0;
        }
    }

    /**
     * @param ctx the browse context
     * @return the issues returned by the {@link SearchRequest} specified by the implementation fragment.
     */
    List<Issue> getIssues(final BrowseContext ctx)
    {
        try
        {
            final SearchRequest sr = getSearchRequest(ctx);
            final PagerFilter pagerFilter = new PagerFilter(getDisplayIssueCount());
            SearchResults results = searchProvider.search(sr.getQuery(), authenticationContext.getLoggedInUser(), pagerFilter);
            if (results != null && results.getIssues() != null)
            {
                return results.getIssues();
            }
        }
        catch (SearchException e)
        {
            log.warn("Could not complete the search request", e);
        }

        return new ArrayList<Issue>();
    }

    /**
     * @param ctx the {@link BrowseContext} for this project tab panel
     * @return the {@link SearchRequest} that query required to return the issues of interest
     */
    abstract SearchRequest getSearchRequest(final BrowseContext ctx);

    private String createSearchRequestUrl(SearchRequest sr)
    {
        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        final StringBuilder link = new StringBuilder();
        link.append(SearchRequestViewUtils.getLink(sr, velocityRequestContext.getBaseUrl(), authenticationContext.getLoggedInUser()))
                .append("&mode=hide");
        return link.toString();
    }
}
