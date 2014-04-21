package com.atlassian.jira.bc.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.StringUtils;

/**
 * This search provider uses the current JIRA search plus it uses Lucene query objects to wild
 * card on issue key and summary.
 */
public class LuceneCurrentSearchIssuePickerSearchProvider extends AbstractIssuePickerSearchProvider
{
    private final JiraAuthenticationContext authenticationContext;
    private final SearchService searchService;

    /**
     * @param authenticationContext The auth context used to get current User
     * @param searchProvider        the SearcProvider to do actual searching
     * @param constantsManager      ConstantsManager
     * @param searchService         The search service sued to construct a serch Query
     * @param modifier Helps rewrite the lucene query so that "NOTs" work as expected.
     * @see com.atlassian.jira.bc.issue.search.IssuePickerSearchProvider
     */
    public LuceneCurrentSearchIssuePickerSearchProvider(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider,
        ConstantsManager constantsManager, SearchService searchService, LuceneQueryModifier modifier)
    {
        super(searchProvider, constantsManager, modifier);
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
    }

    protected String getId()
    {
        return "cs";
    }

    protected String getLabelKey()
    {
        return "jira.ajax.autocomplete.current.search";
    }

    protected SearchRequest getRequest(IssuePickerSearchService.IssuePickerParameters issuePickerParams)
    {
        if (issuePickerParams.getCurrentJQL() != null)
        {
            final SearchService.ParseResult parseResult = searchService.parseQuery(authenticationContext.getLoggedInUser(), issuePickerParams.getCurrentJQL());
            if (parseResult.isValid())
            {
                return new SearchRequest(parseResult.getQuery());
            }
        }
        return null;
    }

    public boolean handlesParameters(final User searcher, final IssuePickerSearchService.IssuePickerParameters issuePickerParams)
    {
        return StringUtils.isNotBlank(issuePickerParams.getQuery()) && issuePickerParams.getCurrentJQL() != null;
    }
}
