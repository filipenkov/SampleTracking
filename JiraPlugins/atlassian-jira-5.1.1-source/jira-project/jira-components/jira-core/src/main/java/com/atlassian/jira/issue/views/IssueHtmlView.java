package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.views.util.DefaultSearchRequestPreviousView;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.SearchRequestPreviousView;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.FileIconBean;

public class IssueHtmlView extends AbstractIssueHtmlView
{
    SearchRequestPreviousView searchRequestPreviousView;

    public IssueHtmlView(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, CommentManager commentManager, FileIconBean fileIconBean, FieldScreenRendererFactory fieldScreenRendererFactory, IssueViewUtil issueViewUtil)
    {
        super(authenticationContext, applicationProperties, commentManager, fileIconBean, fieldScreenRendererFactory, issueViewUtil);
        searchRequestPreviousView = new DefaultSearchRequestPreviousView(authenticationContext, applicationProperties);
    }

    protected String getLinkToPrevious(Issue issue)
    {
        return searchRequestPreviousView.getLinkToPrevious(issue, descriptor);
    }

    protected boolean printCssLinks()
    {
        return true;
    }
}
