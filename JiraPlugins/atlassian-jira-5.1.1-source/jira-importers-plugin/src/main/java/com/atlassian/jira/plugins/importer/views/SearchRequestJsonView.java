package com.atlassian.jira.plugins.importer.views;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.sample.SampleData;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.jira.web.bean.PagerFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SearchRequestJsonView extends IssueJsonView implements SearchRequestView
{
    private final IssueFactory issueFactory;
    private final SearchProviderFactory searchProviderFactory;
    private final SearchProvider searchProvider;

    public SearchRequestJsonView(FieldLayoutManager fieldLayoutManager, CommentManager commentManager, WatcherManager watcherManager,
                                 VoteManager voteManager, UserUtil userUtil, IssueFactory issueFactory,
                                 SearchProviderFactory searchProviderFactory, SearchProvider searchProvider,
                                 JiraAuthenticationContext authenticationContext, PermissionManager permissionManager) {
        super(fieldLayoutManager, commentManager, watcherManager, voteManager, userUtil, permissionManager, authenticationContext);
        this.issueFactory = issueFactory;
        this.searchProviderFactory = searchProviderFactory;
        this.searchProvider = searchProvider;
    }

    @Override
    public void init(SearchRequestViewModuleDescriptor searchRequestViewModuleDescriptor) {
        super.init(null);
    }

    @Override
    public void writeSearchResults(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams, final Writer writer)
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser())) {
            try {
                writer.append(authenticationContext.getI18nHelper().getText("jira-importer-plugin.must.be.admin"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        final Map<ExternalProject, List<ExternalIssue>> projects = Maps.newLinkedHashMap();
        final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        final DocumentHitCollector hitCollector = new DocumentHitCollector(searcher)
        {
            @Override
            public void collect(Document d)
            {
                final Issue issue = issueFactory.getIssue(d);
                final ExternalProject project = convertProject(issue.getProjectObject());

                if (!projects.containsKey(project)) {
                    projects.put(project, Lists.<ExternalIssue>newArrayList());
                }

                projects.get(project).add((convertIssueToExternalIssue(issue)));
            }
        };

        try {
            searchProvider.searchAndSort((searchRequest != null) ? searchRequest.getQuery() : null, authenticationContext.getLoggedInUser(), hitCollector, PagerFilter.getUnlimitedFilter());
        } catch (SearchException e) {
            throw new RuntimeException(e);
        }

        for(Map.Entry<ExternalProject, List<ExternalIssue>> e : projects.entrySet()) {
            e.getKey().setIssues(e.getValue());
        }

        final SampleData data = new SampleData(null, projects.keySet(), convertUsers(users));

        try {
            mapper.writeValue(writer, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeHeaders(SearchRequest searchRequest, RequestHeaders requestHeaders, SearchRequestParams searchRequestParams) {
        JiraHttpUtils.setNoCacheHeaders(requestHeaders);
    }
}
